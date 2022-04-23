package com.github.pcimcioch.memorystore.encoder;

import static java.lang.Float.floatToRawIntBits;
import static java.lang.Float.intBitsToFloat;

// TODO javadocs
public class FloatEncoder extends BitEncoder {

    public static final int BIT_COUNT = 32;
    public static final int MAX_LAST_BIT = 32;

    public FloatEncoder(Config config) {
        super(config);
    }

    public float get(long index) {
        return intBitsToFloat(store.getInt(storeIndex(index)));
    }

    public void set(long index, float value) {
        store.setInt(storeIndex(index), floatToRawIntBits(value));
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
