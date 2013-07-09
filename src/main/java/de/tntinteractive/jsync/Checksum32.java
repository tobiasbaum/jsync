package de.tntinteractive.jsync;

/**
 * A simple 32-bit "rolling" checksum. This checksum algorithm is based upon the
 * algorithm outlined in the paper "The rsync algorithm" by Andrew Tridgell and
 * Paul Mackerras. The algorithm works in such a way that if one knows the sum
 * of a block <em>X<sub>k</sub>...X<sub>l</sub></em>, then it is a simple matter
 * to compute the sum for <em>X<sub>k+1</sub>...X<sub>l+1</sub></em>.
 *
 * @author Casey Marshall
 */
public class Checksum32 {

    /**
     * The first half of the checksum.
     */
    private int a;

    /**
     * The second half of the checksum.
     */
    private int b;

    /**
     * The place from whence the current checksum has been computed.
     */
    private int k;

    /**
     * The place to where the current checksum has been computed.
     */
    private int l;

    /**
     * The block from which the checksum is computed.
     */
    private byte[] block;

    public Checksum32() {
        this.a = 0;
        this.b = 0;
        this.k = 0;
    }

    /**
     * Return the value of the currently computed checksum.
     *
     * @return The currently computed checksum.
     */
    public int getValue() {
        return (this.a & 0xffff) | (this.b << 16);
    }

    /**
     * Reset the checksum.
     */
    public void reset() {
        this.k = 0;
        this.a = 0;
        this.b = 0;
        this.l = 0;
    }

    /**
     * "Roll" the checksum. This method takes a single byte as byte
     * <em>X<sub>l+1</sub></em>, and recomputes the checksum for
     * <em>X<sub>k+1</sub>...X<sub>l+1</sub></em>. This is the preferred method
     * for updating the checksum.
     *
     * @param bt
     *            The next byte.
     * @return the byte "rolled out"
     */
    public byte roll(byte bt) {
        final byte rollingOut = this.block[this.k];
        this.a -= rollingOut;
        this.b -= this.l * rollingOut;
        this.a += bt;
        this.b += this.a;
        this.block[this.k] = bt;
        this.k++;
        if (this.k == this.l) {
            this.k = 0;
        }
        return rollingOut;
    }

    /**
     * Update the checksum by trimming off a byte only, not adding anything.
     */
    public void trim() {
        this.a -= this.block[this.k % this.block.length];
        this.b -= this.l * (this.block[this.k % this.block.length]);
        this.k++;
        this.l--;
    }

    public static int determineFor(byte[] block) {
        final Checksum32 c = new Checksum32();
        c.check(block, 0, block.length);
        return c.getValue();
    }

    /**
     * Update the checksum with an entirely different block, and potentially a
     * different block length.
     *
     * @param buf
     *            The byte array that holds the new block.
     * @param off
     *            From whence to begin reading.
     * @param len
     *            The length of the block to read.
     */
    public void check(byte[] buf, int off, int len) {
        this.block = new byte[len];
        System.arraycopy(buf, off, this.block, 0, len);
        this.reset();
        this.l = this.block.length;
        int i;

        for (i = 0; i < this.block.length - 4; i += 4) {
            this.b += 4 * (this.a + this.block[i])
                    + 3 * this.block[i + 1]
                    + 2 * this.block[i + 2]
                    + this.block[i + 3];
            this.a += this.block[i] + this.block[i + 1] + this.block[i + 2] + this.block[i + 3];
        }
        for (; i < this.block.length; i++) {
            this.a += this.block[i];
            this.b += this.a;
        }
    }

    public void copyBlock(byte[] buffer) {
        assert buffer.length == this.l;
        System.arraycopy(this.block, this.k, buffer, 0, this.l - this.k);
        System.arraycopy(this.block, 0, buffer, this.l - this.k, this.k);
    }

}
