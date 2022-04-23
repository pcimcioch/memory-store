package com.github.pcimcioch.memorystore.encoder;

/**
 * Stores character {@link Character} on 16 bits of memory
 */
public class CharEncoder extends BitEncoder {

    public static final int BIT_COUNT = 16;
    public static final int MAX_LAST_BIT = 32;

    private final int mask;

    /**
     * {@inheritDoc}
     */
    public CharEncoder(Config config) {
        super(config);
        this.mask = 65535 << this.bitShift;
    }

    /**
     * Returns character from given index
     *
     * @param position index of the record
     * @return character value
     */
    public char get(long position) {
        return (char) (store.getInt(storeIndex(position)) >>> bitShift);
    }

    /**
     * Sets character for record of given index
     *
     * @param position index of the record
     * @param value    character value
     */
    public void set(long position, char value) {
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
