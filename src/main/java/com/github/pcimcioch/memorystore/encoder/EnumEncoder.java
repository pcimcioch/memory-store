package com.github.pcimcioch.memorystore.encoder;

import static com.github.pcimcioch.memorystore.util.Utils.assertArgument;

public class EnumEncoder<E extends Enum<E>> extends EnumEncoderBase<E> {

    public static final int MIN_BIT_COUNT = 1;
    public static final int MAX_BIT_COUNT = 31;
    public static final int MAX_LAST_BIT = 32;

    private final int maxValue;
    private final int mask;
    private final String incorrectValueException;

    public EnumEncoder(Config config, IntToEnumFunction<E> enumFactory, EnumToIntFunction<E> enumIndexer) {
        super(config, enumFactory, enumIndexer);

        this.maxValue = (1 << this.bitsCount) - 1;

        this.mask = this.maxValue << this.bitShift;
        this.incorrectValueException = String.format("Enum Value must be between [0, %d]", this.maxValue);
    }

    public E get(long position) {
        int valueIndex = (store.getInt(storeIndex(position)) & mask) >>> bitShift;
        return valueOf(valueIndex);
    }

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
