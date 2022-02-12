package com.github.pcimcioch.memorystore.encoder;

import java.util.Objects;

import static com.github.pcimcioch.memorystore.BitUtils.assertArgument;

public class UnsignedIntegerEncoder extends BitEncoder {

    public static final int MIN_BIT_COUNT = 1;
    public static final int MAX_BIT_COUNT = 31;
    public static final int MAX_LAST_BIT = 32;

    private final int maxValue;
    private final int bitShift;
    private final int mask;
    private final String incorrectValueException;

    public UnsignedIntegerEncoder(Config config) {
        super(config);
        this.maxValue = (1 << config.bitsCount()) - 1;
        this.bitShift = config.bitShift();

        this.mask = this.maxValue << this.bitShift;
        this.incorrectValueException = String.format("Value must be between [0, %d]", this.maxValue);
    }

    public int get(long position) {
        return (store.getInt(storeIndex(position)) & mask) >>> bitShift;
    }

    public void set(long position, int value) {
        assertArgument(value >=0 && value <= maxValue, incorrectValueException);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UnsignedIntegerEncoder that = (UnsignedIntegerEncoder) o;
        return maxValue == that.maxValue && bitShift == that.bitShift;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), maxValue, bitShift);
    }
}
