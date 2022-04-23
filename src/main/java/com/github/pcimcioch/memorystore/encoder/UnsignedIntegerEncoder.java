package com.github.pcimcioch.memorystore.encoder;

import static com.github.pcimcioch.memorystore.util.Utils.assertArgument;

/**
 * Stores unsigned integer on 1-31 bits of memory
 */
public class UnsignedIntegerEncoder extends BitEncoder {

    public static final int MIN_BIT_COUNT = 1;
    public static final int MAX_BIT_COUNT = 31;
    public static final int MAX_LAST_BIT = 32;

    private final int maxValue;
    private final int mask;
    private final String incorrectValueException;

    /**
     * {@inheritDoc}
     */
    public UnsignedIntegerEncoder(Config config) {
        super(config);
        this.maxValue = (1 << this.bitsCount) - 1;

        this.mask = this.maxValue << this.bitShift;
        this.incorrectValueException = String.format("Value must be between [0, %d]", this.maxValue);
    }

    /**
     * Returns integer from given index
     *
     * @param position index of the record
     * @return integer value
     */
    public int get(long position) {
        return (store.getInt(storeIndex(position)) & mask) >>> bitShift;
    }

    /**
     * Sets integer for record of given index
     *
     * @param position index of the record
     * @param value    integer value
     */
    public void set(long position, int value) {
        assertArgument(value >= 0 && value <= maxValue, incorrectValueException);
        store.setPartialInt(storeIndex(position), value << bitShift, mask);
    }

    @Override
    protected int minBits() {
        return MIN_BIT_COUNT;
    }

    @Override
    protected int maxBits() {
        return MAX_BIT_COUNT;
    }

    @Override
    protected int maxLastBit() {
        return MAX_LAST_BIT;
    }
}
