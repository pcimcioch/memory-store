package com.github.pcimcioch.memorystore.encoder;

/**
 * Stores short {@link Short} on 16 bits of memory
 */
public class ShortEncoder extends BitEncoder {

    public static final int BIT_COUNT = 16;
    public static final int MAX_LAST_BIT = 32;

    private final int mask;

    /**
     * {@inheritDoc}
     */
    public ShortEncoder(Config config) {
        super(config);
        this.mask = 65535 << this.bitShift;
    }

    /**
     * Returns short from given index
     *
     * @param position index of the record
     * @return short value
     */
    public short get(long position) {
        return (short) (store.getInt(storeIndex(position)) >>> bitShift);
    }

    /**
     * Sets short for record of given index
     *
     * @param position index of the record
     * @param value    short value
     */
    public void set(long position, short value) {
        store.setPartialInt(storeIndex(position), (value & 0xffff) << bitShift, mask);
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
