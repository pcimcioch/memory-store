package com.github.pcimcioch.memorystore.header;

import com.github.pcimcioch.memorystore.encoder.BitSetEncoder;
import com.github.pcimcioch.memorystore.encoder.BooleanEncoder;
import com.github.pcimcioch.memorystore.encoder.ByteEncoder;
import com.github.pcimcioch.memorystore.encoder.CharEncoder;
import com.github.pcimcioch.memorystore.encoder.DoubleEncoder;
import com.github.pcimcioch.memorystore.encoder.EnumBitSetEncoder;
import com.github.pcimcioch.memorystore.encoder.EnumEncoder;
import com.github.pcimcioch.memorystore.encoder.FloatEncoder;
import com.github.pcimcioch.memorystore.encoder.IntEncoder;
import com.github.pcimcioch.memorystore.encoder.ListEncoder;
import com.github.pcimcioch.memorystore.encoder.LongEncoder;
import com.github.pcimcioch.memorystore.encoder.ShortEncoder;
import com.github.pcimcioch.memorystore.encoder.SignedIntegerEncoder;
import com.github.pcimcioch.memorystore.encoder.UnsignedIntegerEncoder;
import com.github.pcimcioch.memorystore.header.ObjectPoolHeader.PoolDefinition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class HeadersTest {

    private static final String HEADER_NAME = "TestHeader";
    private static final String POOL_NAME = "TestPool";

    @Test
    void bool() {
        // when
        BitHeader<BooleanEncoder> header = Headers.bool(HEADER_NAME);

        // then
        assertHeader(header, 1);
    }

    @Test
    void byte8() {
        // when
        BitHeader<ByteEncoder> header = Headers.byte8(HEADER_NAME);

        // then
        assertHeader(header, 8);
    }

    @Test
    void char16() {
        // when
        BitHeader<CharEncoder> header = Headers.char16(HEADER_NAME);

        // then
        assertHeader(header, 16);
    }

    @Test
    void double64() {
        // when
        BitHeader<DoubleEncoder> header = Headers.double64(HEADER_NAME);

        // then
        assertHeader(header, 64);
    }

    @Test
    void float32() {
        // when
        BitHeader<FloatEncoder> header = Headers.float32(HEADER_NAME);

        // then
        assertHeader(header, 32);
    }

    @Test
    void int32() {
        // when
        BitHeader<IntEncoder> header = Headers.int32(HEADER_NAME);

        // then
        assertHeader(header, 32);
    }

    @Test
    void long64() {
        // when
        BitHeader<LongEncoder> header = Headers.long64(HEADER_NAME);

        // then
        assertHeader(header, 64);
    }

    @Test
    void short16() {
        // when
        BitHeader<ShortEncoder> header = Headers.short16(HEADER_NAME);

        // then
        assertHeader(header, 16);
    }

    @ParameterizedTest
    @CsvSource({
            "0,    1",
            "0,    10",
            "0,    31",
            "-100, 1",
            "-100, 10",
            "-100, 31",
            "100,  1",
            "100,  10",
            "100,  31",
    })
    void intOnBitsCorrect(int minValue, int bitsCount) {
        // when
        BitHeader<SignedIntegerEncoder> header = Headers.intOnBits(HEADER_NAME, minValue, bitsCount);

        // then
        assertHeader(header, bitsCount);
    }

    @ParameterizedTest
    @CsvSource({
            "0,     0",
            "0,    -1",
            "0,     32",
            "-100,  0",
            "-100, -1",
            "-100,  32",
            "100,   0",
            "100,  -1",
            "100,   32",
    })
    void intOnBitsIncorrect(int minValue, int bitsCount) {
        // when
        Throwable thrown = catchThrowable(() -> Headers.intOnBits(HEADER_NAME, minValue, bitsCount));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bits Count must be between 1 and 31");
    }

    @ParameterizedTest
    @CsvSource({
            " 0,    1,          1",
            " 0,    2,          2",
            " 0,    7,          3",
            " 0,    8,          4",
            " 100,  101,        1",
            " 100,  102,        2",
            " 100,  107,        3",
            " 100,  108,        4",
            "-100, -99,         1",
            "-100, -98,         2",
            "-100, -93,         3",
            "-100, -92,         4",
            "-1,    0,          1",
            "-1,    1,          2",
            "-1,    6,          3",
            "-1,    7,          4",
            " 0,    2147483647, 31",
    })
    void intRangeCorrect(int minValue, int maxValue, int bitsCount) {
        // when
        BitHeader<SignedIntegerEncoder> header = Headers.intRange(HEADER_NAME, minValue, maxValue);

        // then
        assertHeader(header, bitsCount);
    }

    @ParameterizedTest
    @CsvSource({
            " 0,   -100",
            " 0,   -1",
            " 0,    0",
            " 100,  100",
            " 100,  99",
            " 100, -100",
            "-100, -100",
            "-1,    2147483647",
    })
    void intRangeIncorrect(int minValue, int maxValue) {
        // when
        Throwable thrown = catchThrowable(() -> Headers.intRange(HEADER_NAME, minValue, maxValue));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Number of possible values must be between 2 and 2147483648");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 31})
    void unsignedIntOnBitsCorrect(int bitsCount) {
        // given
        BitHeader<UnsignedIntegerEncoder> header = Headers.unsignedIntOnBits(HEADER_NAME, bitsCount);

        // then
        assertHeader(header, bitsCount);
    }

    @ParameterizedTest
    @ValueSource(ints = {-100, 0, 32, 100})
    void unsignedIntOnBitsIncorrect(int bitsCount) {
        // given
        Throwable thrown = catchThrowable(() -> Headers.unsignedIntOnBits(HEADER_NAME, bitsCount));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bits Count must be between 1 and 31");
    }

    @ParameterizedTest
    @CsvSource({
            "1, 1",
            "2, 2",
            "7, 3",
            "8, 4",
            "2147483647, 31",
    })
    void unsignedIntMaxValueCorrect(int maxValue, int bitsCount) {
        // given
        BitHeader<UnsignedIntegerEncoder> header = Headers.unsignedIntMaxValue(HEADER_NAME, maxValue);

        // then
        assertHeader(header, bitsCount);
    }

    @ParameterizedTest
    @ValueSource(ints = {-100, -1, 0, Integer.MIN_VALUE})
    void unsignedIntMaxValueIncorrect(int maxValue) {
        // given
        Throwable thrown = catchThrowable(() -> Headers.unsignedIntMaxValue(HEADER_NAME, maxValue));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Max Value must be between 1 and 2147483647");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 100, 1024})
    void bitSetCorrect(int bitsCount) {
        // given
        BitHeader<BitSetEncoder> header = Headers.bitSet(HEADER_NAME, bitsCount);

        // then
        assertHeader(header, bitsCount);
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -1, 0, 1025, Integer.MAX_VALUE})
    void bitSetIncorrect(int bitsCount) {
        // given
        Throwable thrown = catchThrowable(() -> Headers.bitSet(HEADER_NAME, bitsCount));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bits Count must be between 1 and 1024");
    }

    @ParameterizedTest
    @MethodSource("enumBitSetCorrectValues")
    <E extends Enum<E>> void enumBitSetCorrect(Class<E> type, int bitsCount) {
        // when
        BitHeader<EnumBitSetEncoder<E>> header = Headers.enumBitSet(HEADER_NAME, type);

        // then
        assertHeader(header, bitsCount);
    }

    private static Stream<Arguments> enumBitSetCorrectValues() {
        return Stream.of(
                Arguments.of(Type2.class, 2),
                Arguments.of(Type3.class, 3),
                Arguments.of(Type6.class, 6),
                Arguments.of(Type7.class, 7),
                Arguments.of(Type8.class, 8),
                Arguments.of(Type9.class, 9)
        );
    }

    @Test
    void enumBitSetIncorrect() {
        // when
        Throwable thrown = catchThrowable(() -> Headers.enumBitSet(HEADER_NAME, Type0.class));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bits Count must be between 1 and 1024");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 100, 1024})
    void enumBitSetMaxSizeCorrect(int bitsCount) {
        // when
        BitHeader<EnumBitSetEncoder<Type2>> header = Headers.enumBitSetMaxSize(HEADER_NAME, bitsCount, e -> 0);

        // then
        assertHeader(header, bitsCount);
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -1, 0, 1025, Integer.MAX_VALUE})
    void enumBitSetMaxSizeIncorrect(int bitsCount) {
        // when
        Throwable thrown = catchThrowable(() -> Headers.enumBitSetMaxSize(HEADER_NAME, bitsCount, e -> 0));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bits Count must be between 1 and 1024");
    }

    @ParameterizedTest
    @MethodSource("enumTypeCorrectValues")
    <E extends Enum<E>> void enumTypeCorrect(Class<E> type, int bitsCount) {
        // when
        BitHeader<EnumEncoder<E>> header = Headers.enumType(HEADER_NAME, type);

        // then
        assertHeader(header, bitsCount);
    }

    private static Stream<Arguments> enumTypeCorrectValues() {
        return Stream.of(
                Arguments.of(Type2.class, 1),
                Arguments.of(Type3.class, 2),
                Arguments.of(Type6.class, 3),
                Arguments.of(Type7.class, 3),
                Arguments.of(Type8.class, 3),
                Arguments.of(Type9.class, 4)
        );
    }

    @ParameterizedTest
    @ValueSource(classes = {Type0.class, Type1.class})
    <E extends Enum<E>> void enumTypeIncorrect(Class<E> type) {
        // when
        Throwable thrown = catchThrowable(() -> Headers.enumType(HEADER_NAME, type));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Number of possible values must be between 2 and 2147483648");
    }

    @ParameterizedTest
    @MethodSource("nullableEnumTypeCorrectValues")
    <E extends Enum<E>> void nullableEnumTypeCorrect(Class<E> type, int bitsCount) {
        // when
        BitHeader<EnumEncoder<E>> header = Headers.nullableEnumType(HEADER_NAME, type);

        // then
        assertHeader(header, bitsCount);
    }

    private static Stream<Arguments> nullableEnumTypeCorrectValues() {
        return Stream.of(
                Arguments.of(Type1.class, 1),
                Arguments.of(Type2.class, 2),
                Arguments.of(Type3.class, 2),
                Arguments.of(Type6.class, 3),
                Arguments.of(Type7.class, 3),
                Arguments.of(Type8.class, 4),
                Arguments.of(Type9.class, 4)
        );
    }

    @Test
    void enumTypeIncorrect() {
        // when
        Throwable thrown = catchThrowable(() -> Headers.enumType(HEADER_NAME, Type0.class));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Number of possible values must be between 2 and 2147483648");
    }

    @ParameterizedTest
    @CsvSource({
            "2,          1",
            "3,          2",
            "4,          2",
            "7,          3",
            "8,          3",
            "9,          4",
            "9,          4",
            "2147483648, 31"
    })
    void enumTypeMaxSizeCorrect(long enumSize, int bitsCount) {
        // when
        BitHeader<EnumEncoder<Type2>> header = Headers.enumTypeMaxSize(HEADER_NAME, enumSize, x -> null, e -> 0);

        // then
        assertHeader(header, bitsCount);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 0, 1, 2147483649L})
    void enumTypeMaxSizeIncorrect(long enumSize) {
        // when
        Throwable thrown = catchThrowable(() -> Headers.enumTypeMaxSize(HEADER_NAME, enumSize, x -> null, e -> 0));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Number of possible values must be between 2 and 2147483648");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 31})
    void enumTypeOnBitsCorrect(int bitsCount) {
        // when
        BitHeader<EnumEncoder<Type1>> header = Headers.enumTypeOnBits(HEADER_NAME, bitsCount, x -> Type1.VALUE1, e -> 0);

        // then
        assertHeader(header, bitsCount);
    }

    @ParameterizedTest
    @ValueSource(ints = {-100, -1, 0, 32})
    void enumTypeOnBitsIncorrect(int bitsCount) {
        // when
        Throwable thrown = catchThrowable(() -> Headers.enumTypeOnBits(HEADER_NAME, bitsCount, x -> null, e -> 0));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bits Count must be between 1 and 31");
    }

    @Test
    void object() {
        // when
        ObjectDirectHeader<Object> header = Headers.object(HEADER_NAME);

        // then
        assertHeader(header);
    }

    @Test
    void objectPool() {
        // when
        ObjectPoolHeader<Object> header = Headers.objectPool(HEADER_NAME, Headers.poolOnBits(POOL_NAME, 5));

        // then
        assertHeader(header);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 31})
    void poolOnBitsCorrect(int poolBits) {
        // when
        PoolDefinition pool = Headers.poolOnBits(POOL_NAME, poolBits);

        // then
        assertPool(pool, poolBits);
    }

    @ParameterizedTest
    @ValueSource(ints = {-100, -1, 0, 32})
    void poolOnBitsIncorrect(int poolBits) {
        // when
        Throwable thrown = catchThrowable(() -> Headers.poolOnBits(POOL_NAME, poolBits));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bits Count must be between 1 and 31");
    }

    @ParameterizedTest
    @CsvSource({
            "2,          1",
            "3,          2",
            "7,          3",
            "8,          3",
            "9,          4",
            "2147483648, 31"
    })
    void poolSizeCorrect(long size, int poolBits) {
        // when
        PoolDefinition pool = Headers.poolOfSize(POOL_NAME, size);

        // then
        assertPool(pool, poolBits);
    }

    @ParameterizedTest
    @ValueSource(longs = {-100, -1, 0, 1, 2147483649L})
    void poolSizeIncorrect(long size) {
        // when
        Throwable thrown = catchThrowable(() -> Headers.poolOfSize(POOL_NAME, size));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Number of possible values must be between 2 and 2147483648");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 31})
    void listOnBitsCorrect(int bitsCount) {
        // when
        BitHeader<ListEncoder> header = Headers.listOnBits(HEADER_NAME, bitsCount);

        // then
        assertHeader(header, bitsCount);
    }

    @ParameterizedTest
    @ValueSource(ints = {-100, -1, 0, 32})
    void listOnBitsIncorrect(int bitsCount) {
        // when
        Throwable thrown = catchThrowable(() -> Headers.listOnBits(HEADER_NAME, bitsCount));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bits Count must be between 1 and 31");
    }

    @ParameterizedTest
    @CsvSource({
            "2,          1",
            "3,          2",
            "7,          3",
            "8,          3",
            "9,          4",
            "2147483648, 31"
    })
    void listSizeCorrect(long size, int bitsCount) {
        // when
        BitHeader<ListEncoder> header = Headers.listOfSize(HEADER_NAME, size);

        // then
        assertHeader(header, bitsCount);
    }

    @ParameterizedTest
    @ValueSource(longs = {-100, -1, 0, 1, 2147483649L})
    void listSizeIncorrect(long size) {
        // when
        Throwable thrown = catchThrowable(() -> Headers.listOfSize(HEADER_NAME, size));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Number of possible values must be between 2 and 2147483648");
    }

    private static void assertHeader(Header<?> header) {
        assertThat(header.name()).isEqualTo(HEADER_NAME);
    }

    private static void assertHeader(BitHeader<?> header, int bitsCount) {
        assertThat(header.name()).isEqualTo(HEADER_NAME);
        assertThat(header.bitsCount()).isEqualTo(bitsCount);
    }

    private static void assertPool(PoolDefinition pool, int poolBits) {
        assertThat(pool.name()).isEqualTo(POOL_NAME);
        assertThat(pool.poolBits()).isEqualTo(poolBits);
    }

    private enum Type0 {
    }

    private enum Type1 {
        VALUE1
    }

    private enum Type2 {
        VALUE1, VALUE2
    }

    private enum Type3 {
        VALUE1, VALUE2, VALUE3
    }

    private enum Type6 {
        VALUE1, VALUE2, VALUE3, VALUE4, VALUE5, VALUE6
    }

    private enum Type7 {
        VALUE1, VALUE2, VALUE3, VALUE4, VALUE5, VALUE6, VALUE7
    }

    private enum Type8 {
        VALUE1, VALUE2, VALUE3, VALUE4, VALUE5, VALUE6, VALUE7, VALUE8
    }

    private enum Type9 {
        VALUE1, VALUE2, VALUE3, VALUE4, VALUE5, VALUE6, VALUE7, VALUE8, VALUE9
    }
}