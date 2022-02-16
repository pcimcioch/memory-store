package com.github.pcimcioch.memorystore.encoder;

import java.util.Objects;

public class ByteEncoder extends BitEncoder {

    public static final int BIT_COUNT = 8;
    public static final int MAX_LAST_BIT = 32;

    private final int mask;

    public ByteEncoder(Config config) {
        super(config);

        this.mask = 255 << this.bitShift;
    }

    public byte get(long position) {
        return (byte) (store.getInt(storeIndex(position)) >>> bitShift);
    }

    public void set(long position, byte value) {
        store.setPartialInt(storeIndex(position), (value & 0xff) << bitShift, mask);
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
        ByteEncoder that = (ByteEncoder) o;
        return bitShift == that.bitShift;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), bitShift);
    }
}
