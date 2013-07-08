package de.tntinteractive.jsync;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;

/**
 * Empfängt das Diff vom {@link Sender} und baut daraufhin die Zieldatei auf.
 */
public class Receiver implements Runnable {

    private static final String TMP_SUFFIX = ".jstmp";

    private final DataInputStream input;
    private final FilePathBuffer filePaths;
    private final BlockingQueue<Integer> toResend;

    private boolean enumeratorDone;

    private int openResends;

    public Receiver(InputStream source, FilePathBuffer b, BlockingQueue<Integer> toResend) {
        this.input = new DataInputStream(source);
        this.filePaths = b;
        this.toResend = toResend;
    }

    @Override
    public void run() {
        try {
            int index = -2;
            FilePath tmpFile = null;
            OutputStream tmpFileStream = null;
            MD4StreamFilter digestStream = null;

            while (!Thread.interrupted()) {
                final int command = this.input.read();
                if (command < 0) {
                    break;
                }
                if (command == ReceiverCommand.FILE_START.getCode()) {
                    //Anfang einer neuen Datei => Tempdatei erzeugen
                    index = this.input.readInt();
                    tmpFile = this.createTempFileFor(index);
                    tmpFileStream = tmpFile.openOutputStream();
                    digestStream = new MD4StreamFilter(this.input);
                } else if (command == ReceiverCommand.RAW_DATA.getCode()) {
                    //Rohdaten => in Tempdatei schreiben
                    final int length = this.input.readInt();
                    StreamHelper.copy(digestStream, tmpFileStream, length);
                } else if (command == ReceiverCommand.FILE_END.getCode()) {
                    //Ende der Datei => Prüfsumme prüfen
                    final byte[] expectedDigest = new byte[MD4.DIGEST_LENGTH];
                    this.input.readFully(expectedDigest);

                    if (Arrays.equals(expectedDigest, digestStream.getDigest())) {
                        //Prüfsumme OK => echte Datei mit Tempdatei überschreiben
                        tmpFileStream.close();
                        this.renameToRealName(tmpFile);
                        if (this.enumeratorDone) {
                            this.openResends--;
                            assert this.openResends >= 0;
                            if (this.openResends == 0) {
                                break;
                            }
                        }
                    } else {
                        //Prüfsumme nicht OK => Datei in die Resend-Queue stecken
                        this.toResend.add(index);
                        if (!this.enumeratorDone) {
                            this.openResends++;
                        }
                    }
                } else if (command == ReceiverCommand.ENUMERATOR_DONE.getCode()) {
                    this.enumeratorDone = true;
                    if (this.openResends == 0) {
                        break;
                    }
                } else {
                    throw new IOException("unknown command " + command);
                }
            }

            //dem Generator sagen, dass nichts mehr kommt
            this.toResend.add(-1);
        } catch (final Exception e) {
            Logger.LOGGER.log(Level.SEVERE, "exception in receiver", e);
            try {
                this.input.close();
            } catch (final IOException e1) {
                Logger.LOGGER.log(Level.WARNING, "exception while closing", e1);
            }
        }
    }

    private FilePath createTempFileFor(int index) {
        final FilePath orig = this.filePaths.get(index);
        return orig.getParent().getChild(orig.getName() + TMP_SUFFIX);
    }

    private void renameToRealName(FilePath tmpFile) throws IOException {
        tmpFile.renameTo(tmpFile.getName().substring(0, tmpFile.getName().length() - TMP_SUFFIX.length()));
    }

}
