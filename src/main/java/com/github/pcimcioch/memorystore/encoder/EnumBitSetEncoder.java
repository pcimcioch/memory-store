package com.github.pcimcioch.memorystore.encoder;

import static com.github.pcimcioch.memorystore.util.Utils.assertArgument;

// TODO add tests
public class EnumBitSetEncoder<E extends Enum<E>> extends EnumEncoderBase<E> {

    public static final int MIN_BIT_COUNT = 1;
    public static final int MAX_BIT_COUNT = 1024;
    public static final int MAX_LAST_BIT = MAX_BIT_COUNT + 32;

    private final String incorrectValueException;

    public EnumBitSetEncoder(Config config, EnumToIntFunction<E> enumIndexer) {
        super(config, noEnumFactory(), enumIndexer);

        this.incorrectValueException = String.format("Enum Value must be between [0, %d]", this.bitsCount - 1);
    }

    public boolean get(long position, E enumValue) {
        int bitPosition = indexOf(enumValue);
        assertArgument(bitPosition >= 0 && bitPosition < bitsCount, incorrectValueException);

        int shiftedBitPosition = bitShift + bitPosition;
        long storeIndex = storeIndex(position) + (shiftedBitPosition >>> 5);
        int mask = 1 << (shiftedBitPosition & 0b11111);

        return (store.getInt(storeIndex) & mask) != 0;
    }

    public void set(long position, E enumValue, boolean value) {
        int bitPosition = indexOf(enumValue);
        assertArgument(bitPosition >= 0 && bitPosition < bitsCount, incorrectValueException);

        int shiftedBitPosition = bitShift + bitPosition;
        long storeIndex = storeIndex(position) + (shiftedBitPosition >>> 5);
        int mask = 1 << (shiftedBitPosition & 0b11111);

        store.setPartialInt(storeIndex, value ? 0xffffffff : 0x0, mask);
    }

    public void set(long position, E enumValue) {
        set(position, enumValue, true);
    }

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
