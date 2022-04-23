package com.github.pcimcioch.memorystore.encoder;

import static com.github.pcimcioch.memorystore.util.Utils.assertArgument;

/**
 * Stores multiple boolean values in array-like fashion, similar to {@link java.util.BitSet} on 1-1024 bits of memory
 */
public class BitSetEncoder extends BitEncoder {

    public static final int MIN_BIT_COUNT = 1;
    public static final int MAX_BIT_COUNT = 1024;
    public static final int MAX_LAST_BIT = MAX_BIT_COUNT + 32;

    private final String incorrectValueException;

    /**
     * {@inheritDoc}
     */
    public BitSetEncoder(Config config) {
        super(config);

        this.incorrectValueException = String.format("Bit Position must be between [0, %d]", this.bitsCount - 1);
    }

    /**
     * Returns given boolean from given index
     *
     * @param position    index of the record
     * @param bitPosition index of the boolean value
     * @return boolean value
     */
    public boolean get(long position, int bitPosition) {
        assertArgument(bitPosition >= 0 && bitPosition < bitsCount, incorrectValueException);

        int shiftedBitPosition = bitShift + bitPosition;
        long storeIndex = storeIndex(position) + (shiftedBitPosition >>> 5);
        int mask = 1 << (shiftedBitPosition & 0b11111);

        return (store.getInt(storeIndex) & mask) != 0;
    }

    /**
     * Sets given boolean for record of given index
     *
     * @param position    index of the record
     * @param bitPosition index of the boolean value
     * @param value       boolean value
     */
    public void set(long position, int bitPosition, boolean value) {
        assertArgument(bitPosition >= 0 && bitPosition < bitsCount, incorrectValueException);

        int shiftedBitPosition = bitShift + bitPosition;
        long storeIndex = storeIndex(position) + (shiftedBitPosition >>> 5);
        int mask = 1 << (shiftedBitPosition & 0b11111);

        store.setPartialInt(storeIndex, value ? 0xffffffff : 0x0, mask);
    }

    /**
     * Sets given boolean for record of given index to true
     *
     * @param position    index of the record
     * @param bitPosition index of the boolean value
     */
    public void set(long position, int bitPosition) {
        set(position, bitPosition, true);
    }

    /**
     * Sets given boolean for record of given index to false
     *
     * @param position    index of the record
     * @param bitPosition index of the boolean value
     */
    public void clear(long position, int bitPosition) {
        set(position, bitPosition, false);
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
