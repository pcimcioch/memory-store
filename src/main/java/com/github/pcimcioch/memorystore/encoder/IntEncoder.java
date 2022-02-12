package com.github.pcimcioch.memorystore.encoder;

public class IntEncoder extends BitEncoder {

    public static final int BIT_COUNT = 32;
    public static final int MAX_LAST_BIT = 32;

    public IntEncoder(Config config) {
        super(config);
    }

    public int get(long index) {
        return store.getInt(storeIndex(index));
    }

    public void set(long index, int value) {
        store.setInt(storeIndex(index), value);
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
