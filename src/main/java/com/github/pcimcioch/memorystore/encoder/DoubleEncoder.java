package com.github.pcimcioch.memorystore.encoder;

import static java.lang.Double.doubleToRawLongBits;
import static java.lang.Double.longBitsToDouble;

// TODO javadocs
public class DoubleEncoder extends BitEncoder {

    public static final int BIT_COUNT = 64;
    public static final int MAX_LAST_BIT = 64;

    public DoubleEncoder(Config config) {
        super(config);
    }

    public double get(long index) {
        return longBitsToDouble(store.getLong(storeIndex(index)));
    }

    public void set(long index, double value) {
        store.setLong(storeIndex(index), doubleToRawLongBits(value));
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
