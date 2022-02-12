package com.github.pcimcioch.memorystore.encoder;

import java.util.Objects;

import static com.github.pcimcioch.memorystore.BitUtils.assertArgument;

public class SignedIntegerEncoder extends BitEncoder {

    public static final int MIN_BIT_COUNT = 1;
    public static final int MAX_BIT_COUNT = 31;
    public static final int MAX_LAST_BIT = 32;

    private final int minValue;
    private final int maxValue;
    private final int bitShift;
    private final int mask;
    private final String incorrectValueException;

    public SignedIntegerEncoder(Config config, int minValue) {
        super(config);

        long maxValue = (1L << config.bitsCount()) - 1 + minValue;
        this.minValue = minValue;
        this.maxValue = maxValue > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) maxValue;
        this.bitShift = config.bitShift();

        this.mask = ((1 << config.bitsCount()) - 1) << bitShift;
        this.incorrectValueException = String.format("Value must be between [%d, %d]", this.minValue, this.maxValue);
    }

    public int get(long position) {
        return ((store.getInt(storeIndex(position)) & mask) >>> bitShift) + minValue;
    }

    public void set(long position, int value) {
        assertArgument(value >= minValue && value <= maxValue, incorrectValueException);
        store.setPartialInt(storeIndex(position), (value - minValue) << bitShift, mask);
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
        SignedIntegerEncoder that = (SignedIntegerEncoder) o;
        return minValue == that.minValue && maxValue == that.maxValue && bitShift == that.bitShift;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), minValue, maxValue, bitShift);
    }
}
