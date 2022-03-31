package com.github.pcimcioch.memorystore.header;

import com.github.pcimcioch.memorystore.encoder.BitSetEncoder;
import com.github.pcimcioch.memorystore.encoder.BooleanEncoder;
import com.github.pcimcioch.memorystore.encoder.ByteEncoder;
import com.github.pcimcioch.memorystore.encoder.CharEncoder;
import com.github.pcimcioch.memorystore.encoder.DoubleEncoder;
import com.github.pcimcioch.memorystore.encoder.EnumBitSetEncoder;
import com.github.pcimcioch.memorystore.encoder.EnumEncoder;
import com.github.pcimcioch.memorystore.encoder.EnumEncoderBase;
import com.github.pcimcioch.memorystore.encoder.EnumEncoderBase.EnumToIntFunction;
import com.github.pcimcioch.memorystore.encoder.EnumEncoderBase.IntToEnumFunction;
import com.github.pcimcioch.memorystore.encoder.FloatEncoder;
import com.github.pcimcioch.memorystore.encoder.IntEncoder;
import com.github.pcimcioch.memorystore.encoder.LongEncoder;
import com.github.pcimcioch.memorystore.encoder.ShortEncoder;
import com.github.pcimcioch.memorystore.encoder.SignedIntegerEncoder;
import com.github.pcimcioch.memorystore.encoder.UnsignedIntegerEncoder;
import com.github.pcimcioch.memorystore.header.ObjectPoolHeader.PoolDefinition;
import com.github.pcimcioch.memorystore.util.Utils;

import static com.github.pcimcioch.memorystore.encoder.EnumEncoderBase.enumFactory;
import static com.github.pcimcioch.memorystore.encoder.EnumEncoderBase.enumIndexer;
import static com.github.pcimcioch.memorystore.encoder.EnumEncoderBase.enumSize;
import static com.github.pcimcioch.memorystore.encoder.EnumEncoderBase.nullableEnumFactory;
import static com.github.pcimcioch.memorystore.encoder.EnumEncoderBase.nullableEnumIndexer;
import static com.github.pcimcioch.memorystore.encoder.EnumEncoderBase.nullableEnumSize;
import static com.github.pcimcioch.memorystore.util.Utils.assertArgument;

/**
 * This class contains constructor methods for common headers implemented by this library. It's just a facade
 * that hides construction process behind nicer interface. It also provides validation, so it's impossible to create a
 * header with encoder that can not be built
 */
public final class Headers {

    private Headers() {
    }

    /**
     * Store boolean on one bit
     *
     * @param name name of the header
     * @return new header
     */
    public static BitHeader<BooleanEncoder> bool(String name) {
        return new BitHeader<>(name, BooleanEncoder.BIT_COUNT, BooleanEncoder.MAX_LAST_BIT, BooleanEncoder::new);
    }

    /**
     * Store byte on one 8 bits
     *
     * @param name name of the header
     * @return new header
     */
    public static BitHeader<ByteEncoder> byte8(String name) {
        return new BitHeader<>(name, ByteEncoder.BIT_COUNT, ByteEncoder.MAX_LAST_BIT, ByteEncoder::new);
    }

    /**
     * Store char on 8 bits
     *
     * @param name name of the header
     * @return new header
     */
    public static BitHeader<CharEncoder> char16(String name) {
        return new BitHeader<>(name, CharEncoder.BIT_COUNT, CharEncoder.MAX_LAST_BIT, CharEncoder::new);
    }

    /**
     * Store double on 64 bits
     *
     * @param name name of the header
     * @return new header
     */
    public static BitHeader<DoubleEncoder> double64(String name) {
        return new BitHeader<>(name, DoubleEncoder.BIT_COUNT, DoubleEncoder.MAX_LAST_BIT, DoubleEncoder::new);
    }

    /**
     * Store enum on as many bits to fill all possible enum values excluding null. It is not possible to store null
     * value in this structure
     * <p>
     * To store null value see {@link #nullableEnumType(String, Class)}
     * <p>
     * To reserve more bits for the enum (for example in case when you expect enum to grow in the future) use
     * {@link #enumTypeMaxSize(String, long, IntToEnumFunction, EnumToIntFunction)} or
     * {@link #enumTypeOnBits(String, int, IntToEnumFunction, EnumToIntFunction)}
     *
     * @param name        name of the header
     * @param elementType enum class
     * @param <E>         enum type
     * @return new header
     */
    public static <E extends Enum<E>> BitHeader<EnumEncoder<E>> enumType(String name, Class<E> elementType) {
        return enumTypeMaxSize(name, enumSize(elementType), enumFactory(elementType), enumIndexer());
    }

    /**
     * Store enum on as many bits to fill all possible enum values including null. It is possible to store null
     * value in this structure
     * <p>
     * To store non-null value see {@link #enumType(String, Class)}
     * <p>
     * To reserve more bits for the enum (for example in case when you expect enum to grow in the future) use
     * {@link #enumTypeMaxSize(String, long, IntToEnumFunction, EnumToIntFunction)} or
     * {@link #enumTypeOnBits(String, int, IntToEnumFunction, EnumToIntFunction)}
     *
     * @param name        name of the header
     * @param elementType enum class
     * @param <E>         enum type
     * @return new header
     */
    public static <E extends Enum<E>> BitHeader<EnumEncoder<E>> nullableEnumType(String name, Class<E> elementType) {
        return enumTypeMaxSize(name, nullableEnumSize(elementType), nullableEnumFactory(elementType), nullableEnumIndexer());
    }

    /**
     * Store one of predefined number of enum values
     * <p>
     * To automatically compute required size, see {@link #enumType(String, Class)} or
     * {@link #nullableEnumType(String, Class)}
     *
     * @param name        name of the header
     * @param enumSize    maximum number of different enum values that can be stored
     * @param enumFactory factory that will build and enum based on the int value stored
     * @param enumIndexer indexer, that will return int index of the enum. User must make sure that it will not return
     *                    value too big to store in the memory
     * @param <E>         type of the enum
     * @return new header
     */
    public static <E extends Enum<E>> BitHeader<EnumEncoder<E>> enumTypeMaxSize(String name,
                                                                                long enumSize,
                                                                                IntToEnumFunction<E> enumFactory,
                                                                                EnumToIntFunction<E> enumIndexer) {
        assertNumberOfValuesInRange(enumSize, EnumEncoder.MIN_BIT_COUNT, EnumEncoder.MAX_BIT_COUNT);

        return new BitHeader<>(
                name,
                Utils.countBits(enumSize),
                EnumEncoder.MAX_LAST_BIT,
                config -> new EnumEncoder<>(config, enumFactory, enumIndexer)
        );
    }

    /**
     * Store enum value on predefined number of bits
     * <p>
     * To automatically compute required size, see {@link #enumType(String, Class)} or
     * {@link #nullableEnumType(String, Class)}
     *
     * @param name        name of the header
     * @param bitsCount   number of bits to store enum value
     * @param enumFactory factory that will build and enum based on the int value stored
     * @param enumIndexer indexer, that will return int index of the enum. User must make sure that it will not return
     *                    value too big to store in the memory
     * @param <E>         type of the enum
     * @return new header
     */
    public static <E extends Enum<E>> BitHeader<EnumEncoder<E>> enumTypeOnBits(String name,
                                                                               int bitsCount,
                                                                               IntToEnumFunction<E> enumFactory,
                                                                               EnumToIntFunction<E> enumIndexer) {
        assertBitsCount(bitsCount, EnumEncoder.MIN_BIT_COUNT, EnumEncoder.MAX_BIT_COUNT);

        return new BitHeader<>(
                name,
                bitsCount,
                EnumEncoder.MAX_LAST_BIT,
                config -> new EnumEncoder<>(config, enumFactory, enumIndexer)
        );
    }

    /**
     * Store float on 32 bits
     *
     * @param name name of the header
     * @return new header
     */
    public static BitHeader<FloatEncoder> float32(String name) {
        return new BitHeader<>(name, FloatEncoder.BIT_COUNT, FloatEncoder.MAX_LAST_BIT, FloatEncoder::new);
    }

    /**
     * Store int on 32 bits
     *
     * @param name name of the header
     * @return new header
     */
    public static BitHeader<IntEncoder> int32(String name) {
        return new BitHeader<>(name, IntEncoder.BIT_COUNT, IntEncoder.MAX_LAST_BIT, IntEncoder::new);
    }

    /**
     * Store int on defined number of bits
     *
     * @param name      name of the header
     * @param minValue  minimum value that can be stored in this store
     * @param bitsCount number of bits to use to store a value
     * @return new header
     */
    public static BitHeader<SignedIntegerEncoder> intOnBits(String name, int minValue, int bitsCount) {
        assertBitsCount(bitsCount, SignedIntegerEncoder.MIN_BIT_COUNT, SignedIntegerEncoder.MAX_BIT_COUNT);

        return new BitHeader<>(name, bitsCount, SignedIntegerEncoder.MAX_LAST_BIT, config -> new SignedIntegerEncoder(config, minValue));
    }

    /**
     * Store int from defined range
     *
     * @param name     name of the header
     * @param minValue minimum value that can be stored in this store
     * @param maxValue maximum value that can be stored in this store
     * @return new header
     */
    public static BitHeader<SignedIntegerEncoder> intRange(String name, int minValue, int maxValue) {
        long range = (long) maxValue - minValue + 1;
        assertNumberOfValuesInRange(range, SignedIntegerEncoder.MIN_BIT_COUNT, SignedIntegerEncoder.MAX_BIT_COUNT);

        return new BitHeader<>(name, Utils.countBits(range), SignedIntegerEncoder.MAX_LAST_BIT, config -> new SignedIntegerEncoder(config, minValue));
    }

    /**
     * Store long on 64 bits
     *
     * @param name name of the header
     * @return new header
     */
    public static BitHeader<LongEncoder> long64(String name) {
        return new BitHeader<>(name, LongEncoder.BIT_COUNT, LongEncoder.MAX_LAST_BIT, LongEncoder::new);
    }

    /**
     * Store any Object
     *
     * @param name name of the header
     * @param <T>  object type
     * @return new header
     */
    public static <T> ObjectDirectHeader<T> object(String name) {
        return new ObjectDirectHeader<>(name);
    }

    /**
     * Creates object pool for {@link #objectPool(String, PoolDefinition)} that can store elements on given
     * number of bits
     *
     * @param name     name of the pool
     * @param poolBits number of bits that can be used to represent object index in the pool
     * @return new pool definition
     */
    public static PoolDefinition poolOnBits(String name, int poolBits) {
        assertBitsCount(poolBits, UnsignedIntegerEncoder.MIN_BIT_COUNT, UnsignedIntegerEncoder.MAX_BIT_COUNT);

        return new PoolDefinition(name, poolBits);
    }

    /**
     * Creates object pool for {@link #objectPool(String, PoolDefinition)} that can store some number of elements
     *
     * @param name name of the pool
     * @param size maximum number of objects in the pool
     * @return new pool definition
     */
    public static PoolDefinition poolOfSize(String name, long size) {
        assertNumberOfValuesInRange(size, UnsignedIntegerEncoder.MIN_BIT_COUNT, UnsignedIntegerEncoder.MAX_BIT_COUNT);

        return new PoolDefinition(name, Utils.countBits(size));
    }

    /**
     * Store any object of some object pool. Use when objects are very repeatable and there can be created a limited
     * pool of objects. Each record does not have to store its own object, but it can share object with another
     * record and store only index to such object.
     *
     * @param name           name of the header
     * @param poolDefinition pool definition. See {@link #poolOnBits(String, int)} and {@link #poolOfSize(String, long)}
     * @param <T>            object type
     * @return new header
     */
    public static <T> ObjectPoolHeader<T> objectPool(String name, PoolDefinition poolDefinition) {
        return new ObjectPoolHeader<>(name, poolDefinition);
    }

    /**
     * Store short on 16 bits
     *
     * @param name name of the header
     * @return new header
     */
    public static BitHeader<ShortEncoder> short16(String name) {
        return new BitHeader<>(name, ShortEncoder.BIT_COUNT, ShortEncoder.MAX_LAST_BIT, ShortEncoder::new);
    }

    /**
     * Store unsigned int on defined number of bits
     *
     * @param name      name of the header
     * @param bitsCount number of bits to use to store a value
     * @return new header
     */
    public static BitHeader<UnsignedIntegerEncoder> unsignedIntOnBits(String name, int bitsCount) {
        assertBitsCount(bitsCount, UnsignedIntegerEncoder.MIN_BIT_COUNT, UnsignedIntegerEncoder.MAX_BIT_COUNT);

        return new BitHeader<>(name, bitsCount, UnsignedIntegerEncoder.MAX_LAST_BIT, UnsignedIntegerEncoder::new);
    }

    /**
     * Store unsigned int
     *
     * @param name     name of the header
     * @param maxValue maximum value that can be stored
     * @return new header
     */
    public static BitHeader<UnsignedIntegerEncoder> unsignedIntMaxValue(String name, int maxValue) {
        assertValueInRange(maxValue, UnsignedIntegerEncoder.MIN_BIT_COUNT, UnsignedIntegerEncoder.MAX_BIT_COUNT);

        return new BitHeader<>(name, Utils.countBits((long) maxValue + 1), UnsignedIntegerEncoder.MAX_LAST_BIT, UnsignedIntegerEncoder::new);
    }

    /**
     * Store BitSet on defined number of bits
     *
     * @param name      name of the header
     * @param bitsCount number of possible values in the bits set
     * @return new header
     */
    // TODO add tests
    public static BitHeader<BitSetEncoder> bitSet(String name, int bitsCount) {
        assertBitsCount(bitsCount, BitSetEncoder.MIN_BIT_COUNT, BitSetEncoder.MAX_BIT_COUNT);

        return new BitHeader<>(name, bitsCount, BitSetEncoder.MAX_LAST_BIT, BitSetEncoder::new);
    }

    /**
     * Store enum bit set on as many bits as there are possible enum values excluding null. It is not possible to store null
     * value in this structure
     * <p>
     * To store null value see {@link #nullableEnumBitSet(String, Class)}
     * <p>
     * To reserve more bits for the enum (for example in case when you expect enum to grow in the future) use
     * {@link #enumBitSetMaxSize(String, int, EnumToIntFunction)}
     *
     * @param name        name of the header
     * @param elementType enum class
     * @param <E>         enum type
     * @return new header
     */
    // TODO add tests
    public static <E extends Enum<E>> BitHeader<EnumBitSetEncoder<E>> enumBitSet(String name, Class<E> elementType) {
        return enumBitSetMaxSize(name, enumSize(elementType), EnumEncoderBase.<E>enumIndexer());
    }

    /**
     * Store enum on as many bits as there are possible enum values including null. It is possible to store null
     * value in this structure
     * <p>
     * To store non-null value see {@link #enumBitSet(String, Class)}
     * <p>
     * To reserve more bits for the enum (for example in case when you expect enum to grow in the future) use
     * {@link #enumBitSetMaxSize(String, int, EnumToIntFunction)}
     *
     * @param name        name of the header
     * @param elementType enum class
     * @param <E>         enum type
     * @return new header
     */
    // TODO add tests
    public static <E extends Enum<E>> BitHeader<EnumBitSetEncoder<E>> nullableEnumBitSet(String name, Class<E> elementType) {
        return enumBitSetMaxSize(name, nullableEnumSize(elementType), EnumEncoderBase.<E>nullableEnumIndexer());
    }

    /**
     * Store enum bit set
     * <p>
     * To automatically compute required size, see {@link #enumBitSet(String, Class)} or
     * {@link #nullableEnumBitSet(String, Class)}
     *
     * @param name        name of the header
     * @param enumSize    maximum number of different enum values that can be stored
     * @param enumIndexer indexer, that will return int index of the enum. User must make sure that it will not return
     *                    value too big to store in the memory
     * @param <E>         type of the enum
     * @return new header
     */
    // TODO add tests
    public static <E extends Enum<E>> BitHeader<EnumBitSetEncoder<E>> enumBitSetMaxSize(String name,
                                                                                        int enumSize,
                                                                                        EnumToIntFunction<E> enumIndexer) {
        assertBitsCount(enumSize, EnumBitSetEncoder.MIN_BIT_COUNT, EnumBitSetEncoder.MAX_BIT_COUNT);

        return new BitHeader<>(
                name,
                enumSize,
                EnumBitSetEncoder.MAX_LAST_BIT,
                config -> new EnumBitSetEncoder<>(config, enumIndexer)
        );
    }

    private static void assertValueInRange(int maxValue, int minBitCount, int maxBitCount) {
        int min = (1 << minBitCount) - 1;
        int max = (1 << maxBitCount) - 1;

        assertArgument(maxValue >= min && maxValue <= max, "Max Value must be between %d and %d", min, max);
    }

    private static void assertNumberOfValuesInRange(long numberOfValues, int minBitCount, int maxBitCount) {
        long min = 1L << minBitCount;
        long max = 1L << maxBitCount;

        assertArgument(numberOfValues >= min && numberOfValues <= max,
                "Number of possible values must be between %d and %d", min, max);
    }

    private static void assertBitsCount(int bitsCount, int minBitsCount, int maxBitsCount) {
        assertArgument(bitsCount >= minBitsCount && bitsCount <= maxBitsCount,
                "Bits Count must be between %d and %d", minBitsCount, maxBitsCount);
    }
}
