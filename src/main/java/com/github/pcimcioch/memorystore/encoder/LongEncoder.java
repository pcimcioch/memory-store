package com.github.pcimcioch.memorystore.encoder;

public class LongEncoder extends BitEncoder {

    public static final int BIT_COUNT = 64;
    public static final int MAX_LAST_BIT = 64;

    public LongEncoder(Config config) {
        super(config);
    }

    public long get(long index) {
        return store.getLong(storeIndex(index));
    }

    public void set(long index, long value) {
        store.setLong(storeIndex(index), value);
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
