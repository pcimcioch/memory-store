package com.github.pcimcioch.memorystore.encoder;

/**
 * Stores byte {@link Byte} on 8 bits of memory
 */
public class ByteEncoder extends BitEncoder {

    public static final int BIT_COUNT = 8;
    public static final int MAX_LAST_BIT = 32;

    private final int mask;

    /**
     * {@inheritDoc}
     */
    public ByteEncoder(Config config) {
        super(config);

        this.mask = 255 << this.bitShift;
    }

    /**
     * Returns byte from given index
     *
     * @param position index of the record
     * @return byte value
     */
    public byte get(long position) {
        return (byte) (store.getInt(storeIndex(position)) >>> bitShift);
    }

    /**
     * Sets byte for record of given index
     *
     * @param position index of the record
     * @param value    byte value
     */
    public void set(long position, byte value) {
        store.setPartialInt(storeIndex(position), (value & 0xff) << bitShift, mask);
    }

    @Override
    protected int minBits() {
        return BIT_COUNT;
    }

    @Override
    protected int maxBits() {
        return BIT_COUNT;
    }

    @Override
    protected int maxLastBit() {
        return MAX_LAST_BIT;
    }
}
