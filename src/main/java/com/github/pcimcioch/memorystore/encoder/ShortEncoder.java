package com.github.pcimcioch.memorystore.encoder;

import java.util.Objects;

public class ShortEncoder extends BitEncoder {

    public static final int BIT_COUNT = 16;
    public static final int MAX_LAST_BIT = 32;

    private final int mask;

    public ShortEncoder(Config config) {
        super(config);
        this.mask = 65535 << this.bitShift;
    }

    public short get(long position) {
        return (short) (store.getInt(storeIndex(position)) >>> bitShift);
    }

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
