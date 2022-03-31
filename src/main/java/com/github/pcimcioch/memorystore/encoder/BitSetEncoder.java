package com.github.pcimcioch.memorystore.encoder;

import static com.github.pcimcioch.memorystore.util.Utils.assertArgument;

public class BitSetEncoder extends BitEncoder {

    public static final int MIN_BIT_COUNT = 1;
    public static final int MAX_BIT_COUNT = 1024;
    public static final int MAX_LAST_BIT = MAX_BIT_COUNT + 32;

    private final String incorrectValueException;

    public BitSetEncoder(Config config) {
        super(config);

        this.incorrectValueException = String.format("Bit Position must be between [0, %d]", this.bitsCount - 1);
    }

    public boolean get(long position, int bitPosition) {
        assertArgument(bitPosition >=0 && bitPosition < bitsCount, incorrectValueException);

        int shiftedBitPosition = bitShift + bitPosition;
        long storeIndex = storeIndex(position) + (shiftedBitPosition >>> 5);
        int mask = 1 << (shiftedBitPosition & 0b11111);

        return (store.getInt(storeIndex) & mask) != 0;
    }

    public void set(long position, int bitPosition, boolean value) {
        assertArgument(bitPosition >=0 && bitPosition < bitsCount, incorrectValueException);

        int shiftedBitPosition = bitShift + bitPosition;
        long storeIndex = storeIndex(position) + (shiftedBitPosition >>> 5);
        int mask = 1 << (shiftedBitPosition & 0b11111);

        store.setPartialInt(storeIndex, value ? 0xffffffff : 0x0, mask);
    }

    public void set(long position, int bitPosition) {
        set(position, bitPosition, true);
    }

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
