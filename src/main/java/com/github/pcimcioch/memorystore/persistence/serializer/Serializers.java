package com.github.pcimcioch.memorystore.persistence.serializer;

import com.github.pcimcioch.memorystore.persistence.serializer.MultiTypeSerializer.TypeMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

// TODO javadocs
public final class Serializers {

    private static final BooleanArraySerializer BOOLEAN_ARRAY = new BooleanArraySerializer();
    private static final ByteArraySerializer BYTE_ARRAY = new ByteArraySerializer();
    private static final CharArraySerializer CHAR_ARRAY = new CharArraySerializer();
    private static final DoubleArraySerializer DOUBLE_ARRAY = new DoubleArraySerializer();
    private static final FloatArraySerializer FLOAT_ARRAY = new FloatArraySerializer();
    private static final IntArraySerializer INT_ARRAY = new IntArraySerializer();
    private static final LongArraySerializer LONG_ARRAY = new LongArraySerializer();
    private static final ShortArraySerializer SHORT_ARRAY = new ShortArraySerializer();
    private static final StringSerializer STRING = new StringSerializer();

    private Serializers() {
    }

    public static BooleanArraySerializer booleanArray() {
        return BOOLEAN_ARRAY;
    }

    public static ByteArraySerializer byteArray() {
        return BYTE_ARRAY;
    }

    public static CharArraySerializer charArray() {
        return CHAR_ARRAY;
    }

    public static DoubleArraySerializer doubleArray() {
        return DOUBLE_ARRAY;
    }

    public static FloatArraySerializer floatArray() {
        return FLOAT_ARRAY;
    }

    public static IntArraySerializer intArray() {
        return INT_ARRAY;
    }

    public static LongArraySerializer longArray() {
        return LONG_ARRAY;
    }

    public static ShortArraySerializer shortArray() {
        return SHORT_ARRAY;
    }

    public static <T> ObjectArraySerializer<T> objectArray(Serializer<T> elementSerializer) {
        return new ObjectArraySerializer<>(elementSerializer);
    }

    public static StringSerializer string() {
        return STRING;
    }

    public static <T> CollectionSerializer<T, List<T>> listOf(Serializer<T> elementSerializer) {
        return listOf(elementSerializer, ArrayList::new);
    }

    public static <T> CollectionSerializer<T, List<T>> listOf(Serializer<T> elementSerializer,
                                                              Supplier<? extends List<T>> listFactory) {
        return new CollectionSerializer<>(elementSerializer, listFactory);
    }

    public static <T> CollectionSerializer<T, Set<T>> setOf(Serializer<T> elementSerializer) {
        return setOf(elementSerializer, HashSet::new);
    }

    public static <T> CollectionSerializer<T, Set<T>> setOf(Serializer<T> elementSerializer,
                                                            Supplier<? extends Set<T>> setFactory) {
        return new CollectionSerializer<>(elementSerializer, setFactory);
    }

    public static <K, V> MapSerializer<K, V> mapOf(Serializer<K> keySerializer, Serializer<V> valueSerializer) {
        return new MapSerializer<>(keySerializer, valueSerializer, HashMap::new);
    }

    public static <K, V> MapSerializer<K, V> mapOf(Serializer<K> keySerializer,
                                                   Serializer<V> valueSerializer,
                                                   Supplier<? extends Map<K, V>> mapFactory) {
        return new MapSerializer<>(keySerializer, valueSerializer, mapFactory);
    }

    public static <T> TypeMapping<T> mapping(Class<T> type, Serializer<T> serializer) {
        return new TypeMapping<>(type, serializer);
    }

    public static <T> MultiTypeSerializer<T> multiTypeOf(List<TypeMapping<? extends T>> mappings) {
        return new MultiTypeSerializer<>(mappings);
    }
}
