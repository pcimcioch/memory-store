package com.github.pcimcioch.memorystore.encoder;

import static com.github.pcimcioch.memorystore.util.Utils.assertArgument;

/**
 * Stores enum {@link Enum} on 1-31 bits of memory
 *
 * @param <E> enum type
 */
public class EnumEncoder<E extends Enum<E>> extends EnumEncoderBase<E> {

    public static final int MIN_BIT_COUNT = 1;
    public static final int MAX_BIT_COUNT = 31;
    public static final int MAX_LAST_BIT = 32;

    private final int maxValue;
    private final int mask;
    private final String incorrectValueException;

    /**
     * Constructor
     *
     * @param config      configuration describing memory layout
     * @param enumFactory how to create enum from signed integer
     * @param enumIndexer how to create signed integer from enum value
     */
    public EnumEncoder(Config config, IntToEnumFunction<E> enumFactory, EnumToIntFunction<E> enumIndexer) {
        super(config, enumFactory, enumIndexer);

        this.maxValue = (1 << this.bitsCount) - 1;

        this.mask = this.maxValue << this.bitShift;
        this.incorrectValueException = String.format("Enum Value must be between [0, %d]", this.maxValue);
    }

    /**
     * Returns enum from given index
     *
     * @param position index of the record
     * @return enum value
     */
    public E get(long position) {
        int valueIndex = (store.getInt(storeIndex(position)) & mask) >>> bitShift;
        return valueOf(valueIndex);
    }

    /**
     * Sets enum for record of given index
     *
     * @param position index of the record
     * @param value    enum value
     */
    public void set(long position, E value) {
        int valueIndex = indexOf(value);
        assertArgument(valueIndex >= 0 && valueIndex <= maxValue, incorrectValueException);
        store.setPartialInt(storeIndex(position), valueIndex << bitShift, mask);
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
