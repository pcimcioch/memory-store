package com.github.pcimcioch.memorystore.encoder;

import static java.lang.Double.doubleToRawLongBits;
import static java.lang.Double.longBitsToDouble;

/**
 * Stores double precision float number {@link Double} on 64 bits of memory
 */
public class DoubleEncoder extends BitEncoder {

    public static final int BIT_COUNT = 64;
    public static final int MAX_LAST_BIT = 64;

    /**
     * {@inheritDoc}
     */
    public DoubleEncoder(Config config) {
        super(config);
    }

    /**
     * Returns double from given index
     *
     * @param position index of the record
     * @return double value
     */
    public double get(long position) {
        return longBitsToDouble(store.getLong(storeIndex(position)));
    }

    /**
     * Sets double for record of given index
     *
     * @param position index of the record
     * @param value    double value
     */
    public void set(long position, double value) {
        store.setLong(storeIndex(position), doubleToRawLongBits(value));
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
