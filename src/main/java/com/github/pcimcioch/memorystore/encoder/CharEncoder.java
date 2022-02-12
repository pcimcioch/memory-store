package com.github.pcimcioch.memorystore.encoder;

import java.util.Objects;

public class CharEncoder extends BitEncoder {

    public static final int BIT_COUNT = 16;
    public static final int MAX_LAST_BIT = 32;

    private final int bitShift;
    private final int mask;

    public CharEncoder(Config config) {
        super(config);
        this.bitShift = config.bitShift();
        this.mask = 65535 << bitShift;
    }

    public char get(long position) {
        return (char) (store.getInt(storeIndex(position)) >>> bitShift);
    }

    public void set(long position, char value) {
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
        CharEncoder that = (CharEncoder) o;
        return bitShift == that.bitShift;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), bitShift);
    }
}
