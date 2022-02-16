package com.github.pcimcioch.memorystore.encoder;

import java.util.Objects;

public class BooleanEncoder extends BitEncoder {

    public static final int BIT_COUNT = 1;
    public static final int MAX_LAST_BIT = 32;

    private final int mask;

    public BooleanEncoder(Config config) {
        super(config);

        this.mask = 1 << this.bitShift;
    }

    public boolean get(long position) {
        return (store.getInt(storeIndex(position)) & mask) != 0;
    }

    public void set(long position, boolean value) {
        store.setPartialInt(storeIndex(position), value ? 0xffffffff : 0x0, mask);
    }

    public void set(long position) {
        set(position, true);
    }

    public void clear(long position) {
        set(position, false);
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
        BooleanEncoder that = (BooleanEncoder) o;
        return mask == that.mask;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mask);
    }
}
