package com.github.pcimcioch.memorystore.encoder;

// TODO javadocs
public class CharEncoder extends BitEncoder {

    public static final int BIT_COUNT = 16;
    public static final int MAX_LAST_BIT = 32;

    private final int mask;

    public CharEncoder(Config config) {
        super(config);
        this.mask = 65535 << this.bitShift;
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
}
