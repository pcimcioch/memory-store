package com.github.pcimcioch.memorystore.encoder;

/**
 * Stores signed long {@link Long} on 64 bits of memory
 */
public class LongEncoder extends BitEncoder {

    public static final int BIT_COUNT = 64;
    public static final int MAX_LAST_BIT = 64;

    /**
     * {@inheritDoc}
     */
    public LongEncoder(Config config) {
        super(config);
    }

    /**
     * Returns long from given index
     *
     * @param position index of the record
     * @return long value
     */
    public long get(long position) {
        return store.getLong(storeIndex(position));
    }

    /**
     * Sets long for record of given index
     *
     * @param position index of the record
     * @param value    long value
     */
    public void set(long position, long value) {
        store.setLong(storeIndex(position), value);
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
