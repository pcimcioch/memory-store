package com.github.pcimcioch.memorystore.encoder;

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
}
