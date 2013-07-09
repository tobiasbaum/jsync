/* vim:set softtabstop=3 shiftwidth=3 tabstop=3 expandtab tw=72:
   $Id: MD4.java,v 1.9 2003/03/30 15:18:46 rsdio Exp $

   This version is derived from the version in GNU Crypto.

   MD4: The MD4 message digest algorithm.
   Copyright (C) 2002 The Free Software Foundation, Inc.
   Copyright (C) 2003  Casey Marshall <rsdio@metastatic.org>

   This file is a part of Jarsync.

   Jarsync is free software; you can redistribute it and/or modify it
   under the terms of the GNU General Public License as published by the
   Free Software Foundation; either version 2 of the License, or (at
   your option) any later version.

   Jarsync is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Jarsync; if not, write to the

      Free Software Foundation, Inc.,
      59 Temple Place, Suite 330,
      Boston, MA  02111-1307
      USA

   Linking Jarsync statically or dynamically with other modules is
   making a combined work based on Jarsync.  Thus, the terms and
   conditions of the GNU General Public License cover the whole
   combination.

   As a special exception, the copyright holders of Jarsync give you
   permission to link Jarsync with independent modules to produce an
   executable, regardless of the license terms of these independent
   modules, and to copy and distribute the resulting executable under
   terms of your choice, provided that you also meet, for each linked
   independent module, the terms and conditions of the license of that
   module.  An independent module is a module which is not derived from
   or based on Jarsync.  If you modify Jarsync, you may extend this
   exception to your version of it, but you are not obligated to do so.
   If you do not wish to do so, delete this exception statement from
   your version.  */

package de.tntinteractive.jsync;

import java.security.DigestException;
import java.security.MessageDigestSpi;

/**
 * <p>An implementation of Ron Rivest's MD4 message digest algorithm.
 * MD4 was the precursor to the stronger MD5
 * algorithm, and while not considered cryptograpically secure itself,
 * MD4 is in use in various applications. It is slightly faster than
 * MD5.</p>
 *
 * <p>This implementation is derived from the version of MD4 in <a
 * href="http://www.gnu.org/software/gnu-crypto/">GNU Crypto.</p>
 *
 * <p>References:</p>
 *
 * <ol>
 *    <li>The <a href="http://www.ietf.org/rfc/rfc1320.txt">MD4</a> Message-
 *    Digest Algorithm.<br>
 *    R. Rivest.</li>
 * </ol>
 *
 * @version $Revision: 1.9 $
 */
public class MD4 extends MessageDigestSpi implements Cloneable {

    // Constants and variables.
    // -----------------------------------------------------------------

    /**
     * An MD4 message digest is always 128-bits long, or 16 bytes.
     */
    public static final int DIGEST_LENGTH = 16;

    /**
     * The MD4 algorithm operates on 512-bit blocks, or 64 bytes.
     */
    public static final int BLOCK_LENGTH = 64;

    protected static final int A = 0x67452301;
    protected static final int B = 0xefcdab89;
    protected static final int C = 0x98badcfe;
    protected static final int D = 0x10325476;

    /* The four chaining variables. */
    protected int a, b, c, d;

    protected long count;

    protected final byte[] buffer;

    /** Word buffer for transforming. */
    private final int[] X = new int[16];

    // Constructors.
    // -----------------------------------------------------------------

    /**
     * Trivial zero-argument constructor.
     */
    public MD4() {
        this.buffer = new byte[BLOCK_LENGTH];
        this.engineReset();
    }

    /**
     * Private constructor for cloning.
     */
    private MD4(MD4 that) {
        this();

        this.a = that.a;
        this.b = that.b;
        this.c = that.c;
        this.d = that.d;
        this.count = that.count;
        System.arraycopy(that.buffer, 0, this.buffer, 0, BLOCK_LENGTH);
    }

    public static byte[] determineFor(byte[] block, int wantedSize) {
        final MD4 md4 = new MD4();
        md4.engineUpdate(block, 0, block.length);
        return md4.shortenedDigest(wantedSize);
    }

    // java.lang.Cloneable interface implementation --------------------

    @Override
    public Object clone() {
        return new MD4(this);
    }

    // SPI instance methods.
    // -----------------------------------------------------------------

    @Override
    protected int engineGetDigestLength() {
        return DIGEST_LENGTH;
    }

    @Override
    public void engineUpdate(byte b) {
        // compute number of bytes still unhashed; ie. present in buffer
        final int i = (int)(this.count % BLOCK_LENGTH);
        this.count++;
        this.buffer[i] = b;
        if (i == (BLOCK_LENGTH - 1)) {
            this.transform(this.buffer, 0);
        }
    }

    @Override
    protected void engineUpdate(byte[] b, int offset, int len) {
        int n = (int)(this.count % BLOCK_LENGTH);
        this.count += len;
        final int partLen = BLOCK_LENGTH - n;
        int i = 0;

        if (len >= partLen) {
            System.arraycopy(b, offset, this.buffer, n, partLen);
            this.transform(this.buffer, 0);
            for (i = partLen; i + BLOCK_LENGTH - 1 < len; i+= BLOCK_LENGTH) {
                this.transform(b, offset + i);
            }
            n = 0;
        }

        if (i < len) {
            System.arraycopy(b, offset + i, this.buffer, n, len - i);
        }
    }

    /**
     * Pack the four chaining variables into a byte array.
     */
    @Override
    public byte[] engineDigest() {
        final byte[] tail = this.padBuffer();
        this.engineUpdate(tail, 0, tail.length);
        final byte[] digest = {
                (byte) this.a, (byte) (this.a >>> 8), (byte) (this.a >>> 16), (byte) (this.a >>> 24),
                (byte) this.b, (byte) (this.b >>> 8), (byte) (this.b >>> 16), (byte) (this.b >>> 24),
                (byte) this.c, (byte) (this.c >>> 8), (byte) (this.c >>> 16), (byte) (this.c >>> 24),
                (byte) this.d, (byte) (this.d >>> 8), (byte) (this.d >>> 16), (byte) (this.d >>> 24)
        };

        this.engineReset();

        return digest;
    }

    public byte[] shortenedDigest(int size) {
        final byte[] fullDigest = this.engineDigest();
        final byte[] shortened = new byte[size];
        for (int i = 0; i < fullDigest.length; i++) {
            shortened[i % size] ^= fullDigest[i];
        }
        return shortened;
    }

    @Override
    protected int engineDigest(byte[] out, int off, int len) throws DigestException {
        if (off < 0 || off + len >= out.length) {
            throw new DigestException();
        }
        System.arraycopy(this.engineDigest(), 0, out, off,
                Math.min(len, DIGEST_LENGTH));
        return Math.min(len, DIGEST_LENGTH);
    }

    /** Reset the four chaining variables. */
    @Override
    protected void engineReset() {
        this.a = A; this.b = B;
        this.c = C; this.d = D;
        this.count = 0;
    }

    /**
     * Pad the buffer by appending the byte 0x80, then as many zero bytes
     * to fill the buffer 8 bytes shy of being a multiple of 64 bytes, then
     * append the length of the buffer, in bits, before padding.
     */
    protected byte[] padBuffer() {
        final int n = (int) (this.count % BLOCK_LENGTH);
        int padding = (n < 56) ? (56 - n) : (120 - n);
        final byte[] pad = new byte[padding + 8];

        pad[0] = (byte) 0x80;
        final long bits = this.count << 3;
        pad[padding++] = (byte)  bits;
        pad[padding++] = (byte) (bits >>>  8);
        pad[padding++] = (byte) (bits >>> 16);
        pad[padding++] = (byte) (bits >>> 24);
        pad[padding++] = (byte) (bits >>> 32);
        pad[padding++] = (byte) (bits >>> 40);
        pad[padding++] = (byte) (bits >>> 48);
        pad[padding  ] = (byte) (bits >>> 56);

        return pad;
    }

    /** Transform a 64-byte block. */
    protected void transform(byte[] in, int offset) {
        int aa, bb, cc, dd;

        for (int i = 0; i < 16; i++) {
            this.X[i] = (in[offset++] & 0xff)       |
                    (in[offset++] & 0xff) <<  8 |
                    (in[offset++] & 0xff) << 16 |
                    (in[offset++] & 0xff) << 24;
        }

        aa = this.a;  bb = this.b;  cc = this.c;  dd = this.d;

        // Round 1
        this.a += ((this.b & this.c) | ((~this.b) & this.d)) + this.X[ 0];
        this.a = this.a <<  3 | this.a >>> (32 -  3);
        this.d += ((this.a & this.b) | ((~this.a) & this.c)) + this.X[ 1];
        this.d = this.d <<  7 | this.d >>> (32 -  7);
        this.c += ((this.d & this.a) | ((~this.d) & this.b)) + this.X[ 2];
        this.c = this.c << 11 | this.c >>> (32 - 11);
        this.b += ((this.c & this.d) | ((~this.c) & this.a)) + this.X[ 3];
        this.b = this.b << 19 | this.b >>> (32 - 19);
        this.a += ((this.b & this.c) | ((~this.b) & this.d)) + this.X[ 4];
        this.a = this.a <<  3 | this.a >>> (32 -  3);
        this.d += ((this.a & this.b) | ((~this.a) & this.c)) + this.X[ 5];
        this.d = this.d <<  7 | this.d >>> (32 -  7);
        this.c += ((this.d & this.a) | ((~this.d) & this.b)) + this.X[ 6];
        this.c = this.c << 11 | this.c >>> (32 - 11);
        this.b += ((this.c & this.d) | ((~this.c) & this.a)) + this.X[ 7];
        this.b = this.b << 19 | this.b >>> (32 - 19);
        this.a += ((this.b & this.c) | ((~this.b) & this.d)) + this.X[ 8];
        this.a = this.a <<  3 | this.a >>> (32 -  3);
        this.d += ((this.a & this.b) | ((~this.a) & this.c)) + this.X[ 9];
        this.d = this.d <<  7 | this.d >>> (32 -  7);
        this.c += ((this.d & this.a) | ((~this.d) & this.b)) + this.X[10];
        this.c = this.c << 11 | this.c >>> (32 - 11);
        this.b += ((this.c & this.d) | ((~this.c) & this.a)) + this.X[11];
        this.b = this.b << 19 | this.b >>> (32 - 19);
        this.a += ((this.b & this.c) | ((~this.b) & this.d)) + this.X[12];
        this.a = this.a <<  3 | this.a >>> (32 -  3);
        this.d += ((this.a & this.b) | ((~this.a) & this.c)) + this.X[13];
        this.d = this.d <<  7 | this.d >>> (32 -  7);
        this.c += ((this.d & this.a) | ((~this.d) & this.b)) + this.X[14];
        this.c = this.c << 11 | this.c >>> (32 - 11);
        this.b += ((this.c & this.d) | ((~this.c) & this.a)) + this.X[15];
        this.b = this.b << 19 | this.b >>> (32 - 19);

        // Round 2.
        this.a += ((this.b & (this.c | this.d)) | (this.c & this.d)) + this.X[ 0] + 0x5a827999;
        this.a = this.a <<  3 | this.a >>> (32 -  3);
        this.d += ((this.a & (this.b | this.c)) | (this.b & this.c)) + this.X[ 4] + 0x5a827999;
        this.d = this.d <<  5 | this.d >>> (32 -  5);
        this.c += ((this.d & (this.a | this.b)) | (this.a & this.b)) + this.X[ 8] + 0x5a827999;
        this.c = this.c <<  9 | this.c >>> (32 -  9);
        this.b += ((this.c & (this.d | this.a)) | (this.d & this.a)) + this.X[12] + 0x5a827999;
        this.b = this.b << 13 | this.b >>> (32 - 13);
        this.a += ((this.b & (this.c | this.d)) | (this.c & this.d)) + this.X[ 1] + 0x5a827999;
        this.a = this.a <<  3 | this.a >>> (32 -  3);
        this.d += ((this.a & (this.b | this.c)) | (this.b & this.c)) + this.X[ 5] + 0x5a827999;
        this.d = this.d <<  5 | this.d >>> (32 -  5);
        this.c += ((this.d & (this.a | this.b)) | (this.a & this.b)) + this.X[ 9] + 0x5a827999;
        this.c = this.c <<  9 | this.c >>> (32 -  9);
        this.b += ((this.c & (this.d | this.a)) | (this.d & this.a)) + this.X[13] + 0x5a827999;
        this.b = this.b << 13 | this.b >>> (32 - 13);
        this.a += ((this.b & (this.c | this.d)) | (this.c & this.d)) + this.X[ 2] + 0x5a827999;
        this.a = this.a <<  3 | this.a >>> (32 -  3);
        this.d += ((this.a & (this.b | this.c)) | (this.b & this.c)) + this.X[ 6] + 0x5a827999;
        this.d = this.d <<  5 | this.d >>> (32 -  5);
        this.c += ((this.d & (this.a | this.b)) | (this.a & this.b)) + this.X[10] + 0x5a827999;
        this.c = this.c <<  9 | this.c >>> (32 -  9);
        this.b += ((this.c & (this.d | this.a)) | (this.d & this.a)) + this.X[14] + 0x5a827999;
        this.b = this.b << 13 | this.b >>> (32 - 13);
        this.a += ((this.b & (this.c | this.d)) | (this.c & this.d)) + this.X[ 3] + 0x5a827999;
        this.a = this.a <<  3 | this.a >>> (32 -  3);
        this.d += ((this.a & (this.b | this.c)) | (this.b & this.c)) + this.X[ 7] + 0x5a827999;
        this.d = this.d <<  5 | this.d >>> (32 -  5);
        this.c += ((this.d & (this.a | this.b)) | (this.a & this.b)) + this.X[11] + 0x5a827999;
        this.c = this.c <<  9 | this.c >>> (32 -  9);
        this.b += ((this.c & (this.d | this.a)) | (this.d & this.a)) + this.X[15] + 0x5a827999;
        this.b = this.b << 13 | this.b >>> (32 - 13);

        // Round 3.
        this.a += (this.b ^ this.c ^ this.d) + this.X[ 0] + 0x6ed9eba1;
        this.a = this.a <<  3 | this.a >>> (32 -  3);
        this.d += (this.a ^ this.b ^ this.c) + this.X[ 8] + 0x6ed9eba1;
        this.d = this.d <<  9 | this.d >>> (32 -  9);
        this.c += (this.d ^ this.a ^ this.b) + this.X[ 4] + 0x6ed9eba1;
        this.c = this.c << 11 | this.c >>> (32 - 11);
        this.b += (this.c ^ this.d ^ this.a) + this.X[12] + 0x6ed9eba1;
        this.b = this.b << 15 | this.b >>> (32 - 15);
        this.a += (this.b ^ this.c ^ this.d) + this.X[ 2] + 0x6ed9eba1;
        this.a = this.a <<  3 | this.a >>> (32 -  3);
        this.d += (this.a ^ this.b ^ this.c) + this.X[10] + 0x6ed9eba1;
        this.d = this.d <<  9 | this.d >>> (32 -  9);
        this.c += (this.d ^ this.a ^ this.b) + this.X[ 6] + 0x6ed9eba1;
        this.c = this.c << 11 | this.c >>> (32 - 11);
        this.b += (this.c ^ this.d ^ this.a) + this.X[14] + 0x6ed9eba1;
        this.b = this.b << 15 | this.b >>> (32 - 15);
        this.a += (this.b ^ this.c ^ this.d) + this.X[ 1] + 0x6ed9eba1;
        this.a = this.a <<  3 | this.a >>> (32 -  3);
        this.d += (this.a ^ this.b ^ this.c) + this.X[ 9] + 0x6ed9eba1;
        this.d = this.d <<  9 | this.d >>> (32 -  9);
        this.c += (this.d ^ this.a ^ this.b) + this.X[ 5] + 0x6ed9eba1;
        this.c = this.c << 11 | this.c >>> (32 - 11);
        this.b += (this.c ^ this.d ^ this.a) + this.X[13] + 0x6ed9eba1;
        this.b = this.b << 15 | this.b >>> (32 - 15);
        this.a += (this.b ^ this.c ^ this.d) + this.X[ 3] + 0x6ed9eba1;
        this.a = this.a <<  3 | this.a >>> (32 -  3);
        this.d += (this.a ^ this.b ^ this.c) + this.X[11] + 0x6ed9eba1;
        this.d = this.d <<  9 | this.d >>> (32 -  9);
        this.c += (this.d ^ this.a ^ this.b) + this.X[ 7] + 0x6ed9eba1;
        this.c = this.c << 11 | this.c >>> (32 - 11);
        this.b += (this.c ^ this.d ^ this.a) + this.X[15] + 0x6ed9eba1;
        this.b = this.b << 15 | this.b >>> (32 - 15);

        this.a += aa; this.b += bb; this.c += cc; this.d += dd;
    }

}
