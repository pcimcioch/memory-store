package com.github.pcimcioch.memorystore.encoder;

import static java.lang.Float.floatToRawIntBits;
import static java.lang.Float.intBitsToFloat;

/**
 * Stores floating point number {@link Float} on 32 bits of memory
 */
public class FloatEncoder extends BitEncoder {

    public static final int BIT_COUNT = 32;
    public static final int MAX_LAST_BIT = 32;

    /**
     * {@inheritDoc}
     */
    public FloatEncoder(Config config) {
        super(config);
    }

    /**
     * Returns float from given index
     *
     * @param position index of the record
     * @return float value
     */
    public float get(long position) {
        return intBitsToFloat(store.getInt(storeIndex(position)));
    }

    /**
     * Sets float for record of given index
     *
     * @param position index of the record
     * @param value    float value
     */
    public void set(long position, float value) {
        store.setInt(storeIndex(position), floatToRawIntBits(value));
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
