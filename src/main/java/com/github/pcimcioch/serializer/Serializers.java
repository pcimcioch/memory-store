package com.github.pcimcioch.serializer;

import com.github.pcimcioch.serializer.collection.BooleanArraySerializer;
import com.github.pcimcioch.serializer.collection.ByteArraySerializer;
import com.github.pcimcioch.serializer.collection.CharArraySerializer;
import com.github.pcimcioch.serializer.collection.CollectionSerializer;
import com.github.pcimcioch.serializer.collection.DoubleArraySerializer;
import com.github.pcimcioch.serializer.collection.FloatArraySerializer;
import com.github.pcimcioch.serializer.collection.IntArraySerializer;
import com.github.pcimcioch.serializer.collection.LongArraySerializer;
import com.github.pcimcioch.serializer.collection.ObjectArraySerializer;
import com.github.pcimcioch.serializer.collection.ShortArraySerializer;
import com.github.pcimcioch.serializer.common.MapSerializer;
import com.github.pcimcioch.serializer.common.MultiTypeSerializer;
import com.github.pcimcioch.serializer.common.MultiTypeSerializer.TypeMapping;
import com.github.pcimcioch.serializer.common.StringSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Serializers for most common data structures
 */
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

    /**
     * Used to serialize boolean[]
     *
     * @return serializer
     */
    public static BooleanArraySerializer booleanArray() {
        return BOOLEAN_ARRAY;
    }

    /**
     * Used to serialize byte[]
     *
     * @return serializer
     */
    public static ByteArraySerializer byteArray() {
        return BYTE_ARRAY;
    }

    /**
     * Used to serialize char[]
     *
     * @return serializer
     */
    public static CharArraySerializer charArray() {
        return CHAR_ARRAY;
    }

    /**
     * Used to serialize double[]
     *
     * @return serializer
     */
    public static DoubleArraySerializer doubleArray() {
        return DOUBLE_ARRAY;
    }

    /**
     * Used to serialize float[]
     *
     * @return serializer
     */
    public static FloatArraySerializer floatArray() {
        return FLOAT_ARRAY;
    }

    /**
     * Used to serialize int[]
     *
     * @return serializer
     */
    public static IntArraySerializer intArray() {
        return INT_ARRAY;
    }

    /**
     * Used to serialize long[]
     *
     * @return serializer
     */
    public static LongArraySerializer longArray() {
        return LONG_ARRAY;
    }

    /**
     * Used to serialize short[]
     *
     * @return serializer
     */
    public static ShortArraySerializer shortArray() {
        return SHORT_ARRAY;
    }

    /**
     * Used to serialize array of elements of given type
     *
     * @param type              type of element
     * @param elementSerializer serializer for array element
     * @param <T>               type of element
     * @return serializer
     */
    public static <T> ObjectArraySerializer<T> objectArray(Class<T> type, Serializer<T> elementSerializer) {
        return new ObjectArraySerializer<>(elementSerializer, type);
    }

    /**
     * Used to serialize String
     *
     * @return serializer
     */
    public static StringSerializer string() {
        return STRING;
    }

    /**
     * Used to serialize list of elements. Deserialization will use {@link ArrayList}. If you want to use other
     * implementation see {@link #listOf(Serializer, Supplier)}
     *
     * @param elementSerializer serializer for list element
     * @param <T>               type of element
     * @return serializer
     */
    public static <T> CollectionSerializer<T, List<T>> listOf(Serializer<T> elementSerializer) {
        return listOf(elementSerializer, ArrayList::new);
    }

    /**
     * Used to serialize list of elements
     *
     * @param elementSerializer serializer for list element
     * @param listFactory       list implementation to use during deserialize
     * @param <T>               type of element
     * @return serializer
     */
    public static <T> CollectionSerializer<T, List<T>> listOf(Serializer<T> elementSerializer,
                                                              Supplier<? extends List<T>> listFactory) {
        return new CollectionSerializer<>(elementSerializer, listFactory);
    }

    /**
     * Used to serialize set of elements. Deserialization will use {@link HashSet}. If you want to use other
     * implementation see {@link #setOf(Serializer, Supplier)}
     *
     * @param elementSerializer serializer for set element
     * @param <T>               type of element
     * @return serializer
     */
    public static <T> CollectionSerializer<T, Set<T>> setOf(Serializer<T> elementSerializer) {
        return setOf(elementSerializer, HashSet::new);
    }

    /**
     * Used to serialize set of elements
     *
     * @param elementSerializer serializer for set element
     * @param setFactory        set implementation to use during deserialize
     * @param <T>               type of element
     * @return serializer
     */
    public static <T> CollectionSerializer<T, Set<T>> setOf(Serializer<T> elementSerializer,
                                                            Supplier<? extends Set<T>> setFactory) {
        return new CollectionSerializer<>(elementSerializer, setFactory);
    }

    /**
     * Used to serialize map of elements. Deserialization will use {@link HashMap}. If you want to use other
     * implementation see {@link #mapOf(Serializer, Serializer, Supplier)}
     *
     * @param keySerializer   serializer for keys
     * @param valueSerializer serializer for values
     * @param <K>             key type
     * @param <V>             value type
     * @return serializer
     */
    public static <K, V> MapSerializer<K, V> mapOf(Serializer<K> keySerializer, Serializer<V> valueSerializer) {
        return new MapSerializer<>(keySerializer, valueSerializer, HashMap::new);
    }

    /**
     * Used to serialize map of elements
     *
     * @param keySerializer   serializer for keys
     * @param valueSerializer serializer for values
     * @param mapFactory      map implementation to use during deserialize
     * @param <K>             key type
     * @param <V>             value type
     * @return serializer
     */
    public static <K, V> MapSerializer<K, V> mapOf(Serializer<K> keySerializer,
                                                   Serializer<V> valueSerializer,
                                                   Supplier<? extends Map<K, V>> mapFactory) {
        return new MapSerializer<>(keySerializer, valueSerializer, mapFactory);
    }

    /**
     * Used to serialize map of elements where each value can use different serializer depending on the key.
     * Deserialization will use {@link HashMap}. If you want to use other implementation
     * see {@link #mapOf(Serializer, Function, Supplier)}
     *
     * @param keySerializer           serializer for keys
     * @param valueSerializerProvider provide serializer for value based on the key
     * @param <K>                     key type
     * @param <V>                     value type
     * @return serializer
     */
    public static <K, V> MapSerializer<K, V> mapOf(Serializer<K> keySerializer,
                                                   Function<K, Serializer<V>> valueSerializerProvider) {
        return new MapSerializer<>(keySerializer, valueSerializerProvider, HashMap::new);
    }

    /**
     * Used to serialize map of elements where each value can use different serializer depending on the key.
     *
     * @param keySerializer           serializer for keys
     * @param valueSerializerProvider provide serializer for value based on the key
     * @param mapFactory              map implementation to use during deserializer
     * @param <K>                     key type
     * @param <V>                     value type
     * @return serializer
     */
    public static <K, V> MapSerializer<K, V> mapOf(Serializer<K> keySerializer,
                                                   Function<K, Serializer<V>> valueSerializerProvider,
                                                   Supplier<? extends Map<K, V>> mapFactory) {
        return new MapSerializer<>(keySerializer, valueSerializerProvider, mapFactory);
    }

    /**
     * Type to serializer mapping, used by {@link #multiTypeOf(List)}
     *
     * @param type       type
     * @param serializer serializer
     * @param <T>        type
     * @return type mapping
     */
    public static <T> TypeMapping<T> mapping(Class<T> type, Serializer<T> serializer) {
        return new TypeMapping<>(type, serializer);
    }

    /**
     * Used to serialize object, which uses different serializer based on the object class.
     * Mapping will be considered in provided order. First one matching will be selected to serialize. It doesn't have
     * to be an exact match, the first type assignable to given object will be selected (like in Exception catching).
     * In particular, such mappings does not have sense:
     *
     * <pre>
     *     multiTypeOf(
     *          mapping(Number.class, someNumberSerializer),
     *          mapping(Integer.class, someIntegerSerializer) // this will never be used, as all Integers are matched by previous mapping!
     *     );
     * </pre>
     *
     * @param mappings list of type to serializer mappings
     * @param <T>      base type for all the types that should be serialized by this serializer
     * @return serializer
     */
    public static <T> MultiTypeSerializer<T> multiTypeOf(List<TypeMapping<? extends T>> mappings) {
        return new MultiTypeSerializer<>(mappings);
    }
}
