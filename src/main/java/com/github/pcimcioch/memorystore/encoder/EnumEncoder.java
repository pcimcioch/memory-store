package com.github.pcimcioch.memorystore.encoder;

import java.util.Objects;

import static com.github.pcimcioch.memorystore.BitUtils.assertArgument;
import static java.util.Objects.requireNonNull;

public class EnumEncoder<E extends Enum<E>> extends BitEncoder {

    public static final int MIN_BIT_COUNT = 1;
    public static final int MAX_BIT_COUNT = 31;
    public static final int MAX_LAST_BIT = 32;

    private final int maxValue;
    private final int mask;
    private final String incorrectValueException;
    private final IntToEnumFunction<E> enumFactory;
    private final EnumToIntFunction<E> enumIndexer;


    @FunctionalInterface
    public interface IntToEnumFunction<E extends Enum<E>> {
        E apply(int index);
    }

    @FunctionalInterface
    public interface EnumToIntFunction<E extends Enum<E>> {
        int apply(E value);
    }

    public EnumEncoder(Config config, IntToEnumFunction<E> enumFactory, EnumToIntFunction<E> enumIndexer) {
        super(config);

        this.maxValue = (1 << this.bitsCount) - 1;
        this.enumFactory = requireNonNull(enumFactory, "Enum Factory cannot be null");
        this.enumIndexer = requireNonNull(enumIndexer, "Enum Indexer cannot be null");

        this.mask = this.maxValue << this.bitShift;
        this.incorrectValueException = String.format("Enum Value must be between [0, %d]", this.maxValue);
    }

    public E get(long position) {
        int valueIndex = (store.getInt(storeIndex(position)) & mask) >>> bitShift;
        return enumFactory.apply(valueIndex);
    }

    public void set(long position, E value) {
        int valueIndex = enumIndexer.apply(value);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EnumEncoder<?> that = (EnumEncoder<?>) o;
        return maxValue == that.maxValue && bitShift == that.bitShift && enumFactory.equals(that.enumFactory) && enumIndexer.equals(that.enumIndexer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), maxValue, bitShift, enumFactory, enumIndexer);
    }

    public static <E extends Enum<E>> IntToEnumFunction<E> enumFactory(Class<E> elementType) {
        E[] universe = elementType.getEnumConstants();
        return index -> universe[index];
    }

    public static <E extends Enum<E>> EnumToIntFunction<E> enumIndexer() {
        return Enum::ordinal;
    }

    public static <E extends Enum<E>> int enumSize(Class<E> elementType) {
        return elementType.getEnumConstants().length;
    }

    public static <E extends Enum<E>> IntToEnumFunction<E> nullableEnumFactory(Class<E> elementType) {
        E[] universe = elementType.getEnumConstants();
        return index -> index == 0 ? null : universe[index - 1];
    }

    public static <E extends Enum<E>> EnumToIntFunction<E> nullableEnumIndexer() {
        return e -> e == null ? 0 : e.ordinal() + 1;
    }

    public static <E extends Enum<E>> int nullableEnumSize(Class<E> elementType) {
        return enumSize(elementType) + 1;
    }
}
