package com.github.pcimcioch.memorystore.encoder;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Base encoder for enum like types
 *
 * @param <E> enum type supported by this encoder
 */
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

    /**
     * Enum factory that creates enum by their index. 0-indexed
     *
     * @param elementType enum type
     * @param <E>         enum type
     * @return factory
     */
    public static <E extends Enum<E>> IntToEnumFunction<E> enumFactory(Class<E> elementType) {
        E[] universe = elementType.getEnumConstants();
        return index -> universe[index];
    }

    /**
     * Enum indexer that returns 0-indexed enum ordinal
     *
     * @param <E> enum type
     * @return indexer
     */
    public static <E extends Enum<E>> EnumToIntFunction<E> enumIndexer() {
        return Enum::ordinal;
    }

    /**
     * Number of possible enum elements excluding null
     *
     * @param elementType enum type
     * @param <E>         enum type
     * @return elements count
     */
    public static <E extends Enum<E>> int enumSize(Class<E> elementType) {
        return elementType.getEnumConstants().length;
    }

    /**
     * Enum factory that creates enum by their index. It can also create null values
     * 0 index is reserved for the null value.
     * All other indexes are enum ordinal plus one
     *
     * @param elementType enum type
     * @param <E>         enum type
     * @return factory
     */
    public static <E extends Enum<E>> IntToEnumFunction<E> nullableEnumFactory(Class<E> elementType) {
        E[] universe = elementType.getEnumConstants();
        return index -> index == 0 ? null : universe[index - 1];
    }

    /**
     * Enum indexer that returns enum index.
     * 0 index is reserved for the null value.
     * All other indexes are enum ordinal plus one
     *
     * @param <E> enum type
     * @return indexer
     */
    public static <E extends Enum<E>> EnumToIntFunction<E> nullableEnumIndexer() {
        return e -> e == null ? 0 : e.ordinal() + 1;
    }

    /**
     * Number of possible enum elements including null
     *
     * @param elementType enum type
     * @param <E>         enum type
     * @return elements count
     */
    public static <E extends Enum<E>> int nullableEnumSize(Class<E> elementType) {
        return enumSize(elementType) + 1;
    }

    /**
     * Stub factory that always throws an exception
     *
     * @param <E> enum type
     * @return factory
     */
    public static <E extends Enum<E>> IntToEnumFunction<E> noEnumFactory() {
        return index -> {
            throw new IllegalStateException("Factory not implemented");
        };
    }
}
