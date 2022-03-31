package com.github.pcimcioch.memorystore.encoder;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public abstract class EnumEncoderBase<E extends Enum<E>> extends BitEncoder {

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

    protected EnumEncoderBase(Config config, IntToEnumFunction<E> enumFactory, EnumToIntFunction<E> enumIndexer) {
        super(config);

        this.enumFactory = requireNonNull(enumFactory, "Enum Factory cannot be null");
        this.enumIndexer = requireNonNull(enumIndexer, "Enum Indexer cannot be null");
    }

    protected int indexOf(E value) {
        return enumIndexer.apply(value);
    }

    protected E valueOf(int index) {
        return enumFactory.apply(index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EnumEncoderBase<?> that = (EnumEncoderBase<?>) o;
        return enumFactory.equals(that.enumFactory) && enumIndexer.equals(that.enumIndexer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), enumFactory, enumIndexer);
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

    public static <E extends Enum<E>> IntToEnumFunction<E> noEnumFactory() {
        return index -> {
            throw new IllegalStateException("Factory not implemented");
        };
    }
}
