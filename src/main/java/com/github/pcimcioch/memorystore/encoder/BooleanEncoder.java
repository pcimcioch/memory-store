package com.github.pcimcioch.memorystore.encoder;

/**
 * Stores boolean {@link Boolean} on 1 bit of memory
 */
public class BooleanEncoder extends BitEncoder {

    public static final int BIT_COUNT = 1;
    public static final int MAX_LAST_BIT = 32;

    private final int mask;

    /**
     * {@inheritDoc}
     */
    public BooleanEncoder(Config config) {
        super(config);

        this.mask = 1 << this.bitShift;
    }

    /**
     * Returns if boolean is set for given index
     *
     * @param position index of the record
     * @return whether value is set
     */
    public boolean get(long position) {
        return (store.getInt(storeIndex(position)) & mask) != 0;
    }

    /**
     * Sets boolean for record of given index
     *
     * @param position index of the record
     * @param value    boolean value
     */
    public void set(long position, boolean value) {
        store.setPartialInt(storeIndex(position), value ? 0xffffffff : 0x0, mask);
    }

    /**
     * Sets boolean to true for record of given index
     *
     * @param position index of the record
     */
    public void set(long position) {
        set(position, true);
    }

    /**
     * Sets boolean to false for record of given index
     *
     * @param position index of the record
     */
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
