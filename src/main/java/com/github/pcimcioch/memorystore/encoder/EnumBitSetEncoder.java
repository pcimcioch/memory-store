package com.github.pcimcioch.memorystore.encoder;

import static com.github.pcimcioch.memorystore.util.Utils.assertArgument;

/**
 * Stores multiple enum values in array-like fashion, similar to {@link java.util.BitSet} on 1-1024 bits of memory
 */
public class EnumBitSetEncoder<E extends Enum<E>> extends EnumEncoderBase<E> {

    public static final int MIN_BIT_COUNT = 1;
    public static final int MAX_BIT_COUNT = 1024;
    public static final int MAX_LAST_BIT = MAX_BIT_COUNT + 32;

    private final String incorrectValueException;

    /**
     * Constructor
     *
     * @param config      configuration describing memory layout
     * @param enumIndexer how to create signed integer from enum value
     */
    public EnumBitSetEncoder(Config config, EnumToIntFunction<E> enumIndexer) {
        super(config, noEnumFactory(), enumIndexer);

        this.incorrectValueException = String.format("Enum Value must be between [0, %d]", this.bitsCount - 1);
    }

    /**
     * Returns whether given enum value is set for record of given index
     *
     * @param position  index of the record
     * @param enumValue enum value to check
     * @return if given enum value is set
     */
    public boolean get(long position, E enumValue) {
        int bitPosition = indexOf(enumValue);
        assertArgument(bitPosition >= 0 && bitPosition < bitsCount, incorrectValueException);

        int shiftedBitPosition = bitShift + bitPosition;
        long storeIndex = storeIndex(position) + (shiftedBitPosition >>> 5);
        int mask = 1 << (shiftedBitPosition & 0b11111);

        return (store.getInt(storeIndex) & mask) != 0;
    }

    /**
     * Sets whether given enum value is set for record of given index
     *
     * @param position  index of the record
     * @param enumValue enum value to set
     * @param value     boolean describing whether enum should be set or cleared
     */
    public void set(long position, E enumValue, boolean value) {
        int bitPosition = indexOf(enumValue);
        assertArgument(bitPosition >= 0 && bitPosition < bitsCount, incorrectValueException);

        int shiftedBitPosition = bitShift + bitPosition;
        long storeIndex = storeIndex(position) + (shiftedBitPosition >>> 5);
        int mask = 1 << (shiftedBitPosition & 0b11111);

        store.setPartialInt(storeIndex, value ? 0xffffffff : 0x0, mask);
    }

    /**
     * Sets to true given enum value for record of given index
     *
     * @param position  index of the record
     * @param enumValue enum value to set to true
     */
    public void set(long position, E enumValue) {
        set(position, enumValue, true);
    }

    /**
     * Sets to false given enum value for record of given index
     *
     * @param position  index of the record
     * @param enumValue enum value to set to false
     */
    public void clear(long position, E enumValue) {
        set(position, enumValue, false);
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
