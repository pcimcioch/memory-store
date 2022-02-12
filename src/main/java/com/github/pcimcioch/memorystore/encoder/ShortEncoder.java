package com.github.pcimcioch.memorystore.encoder;

import java.util.Objects;

public class ShortEncoder extends BitEncoder {

    public static final int BIT_COUNT = 16;
    public static final int MAX_LAST_BIT = 32;

    private final int bitShift;
    private final int mask;

    public ShortEncoder(Config config) {
        super(config);

        this.bitShift = config.bitShift();
        this.mask = 65535 << bitShift;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ShortEncoder that = (ShortEncoder) o;
        return bitShift == that.bitShift;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), bitShift);
    }
}