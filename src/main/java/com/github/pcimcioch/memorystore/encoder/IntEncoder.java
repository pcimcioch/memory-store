package com.github.pcimcioch.memorystore.encoder;

/**
 * Stores signed integer {@link Integer} on 32 bits of memory
 */
public class IntEncoder extends BitEncoder {

    public static final int BIT_COUNT = 32;
    public static final int MAX_LAST_BIT = 32;

    /**
     * {@inheritDoc}
     */
    public IntEncoder(Config config) {
        super(config);
    }

    /**
     * Returns integer from given index
     *
     * @param position index of the record
     * @return integer value
     */
    public int get(long position) {
        return store.getInt(storeIndex(position));
    }

    /**
     * Sets integer for record of given index
     *
     * @param position index of the record
     * @param value    integer value
     */
    public void set(long position, int value) {
        store.setInt(storeIndex(position), value);
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
