package com.github.pcimcioch.memorystore.encoder;

import com.github.pcimcioch.memorystore.encoder.BitEncoder.Config;
import com.github.pcimcioch.memorystore.store.IntStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.github.pcimcioch.memorystore.encoder.EnumEncoderBase.enumFactory;
import static com.github.pcimcioch.memorystore.encoder.EnumEncoderBase.enumIndexer;
import static com.github.pcimcioch.memorystore.encoder.EnumEncoderBase.nullableEnumFactory;
import static com.github.pcimcioch.memorystore.encoder.EnumEncoderBase.nullableEnumIndexer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class EnumEncoderTest {

    private static final String BITS_COUNT_EX = "Bits Count outside of defined bounds";
    private static final String BIT_SHIFT_EX = "Bit Shift over a limit";
    private static final TestType DEFAULT_VALUE = TestType.TYPE1;
    private static final int DEFAULT_INT_VALUE = DEFAULT_VALUE.ordinal();

    private final IntStore store = new IntStore();

    private enum TestType {
        TYPE1,
        TYPE2,
        TYPE3
    }

    @ParameterizedTest
    @MethodSource("incorrectConfigs")
    void incorrectConfig(int bitShift, int bitsCount, String message) {
        // given
        Config config = new Config(store, 2, 0, bitShift, bitsCount);

        // when
        Throwable thrown = catchThrowable(() -> new EnumEncoder<>(config, enumFactory(TestType.class), enumIndexer()));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);
    }

    private static Stream<Arguments> incorrectConfigs() {
        return Stream.of(
                Arguments.of(15, 18, BIT_SHIFT_EX),
                Arguments.of(2, 31, BIT_SHIFT_EX),
                Arguments.of(32, 1, BIT_SHIFT_EX),
                Arguments.of(0, 64, BITS_COUNT_EX),
                Arguments.of(0, 32, BITS_COUNT_EX)
        );
    }

    @Test
    void nullEnumFactory() {
        // given
        Config config = new Config(store, 2, 0, 0, 31);

        // when
        Throwable thrown = catchThrowable(() -> new EnumEncoder<TestType>(config, null, enumIndexer()));

        // then
        assertThat(thrown).isInstanceOf(NullPointerException.class);
    }

    @Test
    void nullEnumIndexer() {
        // given
        Config config = new Config(store, 2, 0, 0, 31);

        // when
        Throwable thrown = catchThrowable(() -> new EnumEncoder<>(config, enumFactory(TestType.class), null));

        // then
        assertThat(thrown).isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("correctConfigs")
    void correctConfig(int bitShift, int bitsCount) {
        // given
        Config config = new Config(store, 2, 0, bitShift, bitsCount);

        // when
        new EnumEncoder<>(config, enumFactory(TestType.class), enumIndexer());

        // then
        // no exception thrown
    }

    private static Stream<Arguments> correctConfigs() {
        return Stream.of(
                Arguments.of(15, 17),
                Arguments.of(1, 31),
                Arguments.of(0, 31),
                Arguments.of(31, 1)
        );
    }

    @ParameterizedTest
    @MethodSource("saveValues")
    void savesDifferentValues(int bitsCount, TestType value) {
        // given
        Config config = new Config(store, 1, 0, 0, bitsCount);
        EnumEncoder<TestType> testee = new EnumEncoder<>(config, enumFactory(TestType.class), enumIndexer());

        // when
        testee.set(0, value);

        // then
        assertThat(testee.get(0)).isEqualTo(value);
        assertThat(store.getInt(0)).isEqualTo(value.ordinal());
    }

    private static Stream<Arguments> saveValues() {
        return Stream.of(
                Arguments.of(2, TestType.TYPE1),
                Arguments.of(2, TestType.TYPE2),
                Arguments.of(2, TestType.TYPE3),
                Arguments.of(12, TestType.TYPE1),
                Arguments.of(12, TestType.TYPE2),
                Arguments.of(12, TestType.TYPE3),
                Arguments.of(31, TestType.TYPE1),
                Arguments.of(31, TestType.TYPE2)
        );
    }

    @ParameterizedTest
    @MethodSource("saveNullValues")
    void savesNullValues(int bitsCount, TestType value, int expectedIntValue) {
        // given
        Config config = new Config(store, 1, 0, 0, bitsCount);
        EnumEncoder<TestType> testee = new EnumEncoder<>(config, nullableEnumFactory(TestType.class), nullableEnumIndexer());

        // when
        testee.set(0, value);

        // then
        assertThat(testee.get(0)).isEqualTo(value);
        assertThat(store.getInt(0)).isEqualTo(expectedIntValue);
    }

    private static Stream<Arguments> saveNullValues() {
        return Stream.of(
                Arguments.of(2, null, 0),
                Arguments.of(2, TestType.TYPE1, 1),
                Arguments.of(2, TestType.TYPE2, 2),
                Arguments.of(2, TestType.TYPE3, 3),
                Arguments.of(12, null, 0),
                Arguments.of(12, TestType.TYPE1, 1),
                Arguments.of(12, TestType.TYPE2, 2),
                Arguments.of(12, TestType.TYPE3, 3),
                Arguments.of(31, null, 0),
                Arguments.of(31, TestType.TYPE1, 1),
                Arguments.of(31, TestType.TYPE2, 2)
        );
    }

    @ParameterizedTest
    @MethodSource("incorrectValues")
    void rejectsIncorrectValues(int bitsCount, int value, String message) {
        // given
        Config config = new Config(store, 1, 0, 0, bitsCount);
        EnumEncoder<TestType> testee = new EnumEncoder<>(config, enumFactory(TestType.class), type -> value);

        // when
        Throwable thrown = catchThrowable(() -> testee.set(0, DEFAULT_VALUE));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);
        assertThat(store.blocksCount()).isEqualTo(0);
    }

    private static Stream<Arguments> incorrectValues() {
        return Stream.of(
                Arguments.of(4, -1, "Enum Value must be between [0, 15]"),
                Arguments.of(4, 16, "Enum Value must be between [0, 15]"),
                Arguments.of(4, 1000, "Enum Value must be between [0, 15]"),
                Arguments.of(12, -1, "Enum Value must be between [0, 4095]"),
                Arguments.of(12, 4096, "Enum Value must be between [0, 4095]"),
                Arguments.of(12, 10000, "Enum Value must be between [0, 4095]"),
                Arguments.of(31, -1, "Enum Value must be between [0, 2147483647]")
        );
    }

    @ParameterizedTest
    @MethodSource("positions")
    void usesCorrectPosition(int recordSize, int positionInRecord, int position, int storePosition) {
        // given
        Config config = new Config(store, recordSize, positionInRecord, 0, 12);
        EnumEncoder<TestType> testee = new EnumEncoder<>(config, enumFactory(TestType.class), enumIndexer());

        // when
        testee.set(position, DEFAULT_VALUE);

        // then
        assertThat(testee.get(position)).isEqualTo(DEFAULT_VALUE);
        assertThat(store.getInt(storePosition)).isEqualTo(DEFAULT_INT_VALUE);
    }

    private static Stream<Arguments> positions() {
        return Stream.of(
                Arguments.of(1, 0, 0, 0),
                Arguments.of(1, 0, 10, 10),
                Arguments.of(5, 0, 0, 0),
                Arguments.of(5, 0, 3, 15),
                Arguments.of(5, 2, 0, 2),
                Arguments.of(5, 2, 3, 17)
        );
    }

    @ParameterizedTest
    @MethodSource("bitShifts")
    void doesNotOverrideOtherValues(int bitShift, int value, int previousStoreValue, int nextStoreValue) {
        // given
        int position = 0;

        store.setInt(position, previousStoreValue);
        Config config = new Config(store, 1, 0, bitShift, 7);
        EnumEncoder<TestType> testee = new EnumEncoder<>(config, index -> DEFAULT_VALUE, type -> value);

        // when
        testee.set(position, DEFAULT_VALUE);

        // then
        assertThat(testee.get(position)).isEqualTo(DEFAULT_VALUE);
        assertThat(store.getInt(position)).isEqualTo(nextStoreValue);
    }

    private static Stream<Arguments> bitShifts() {
        return Stream.of(
                Arguments.of(0, 0x7f, 0x0, 0x7f),
                Arguments.of(0, 0x7f, 0x7f, 0x7f),
                Arguments.of(0, 0x00, 0x0, 0x0),
                Arguments.of(0, 0x00, 0x7f, 0x0),

                Arguments.of(0, 0x7f, 0xffffffff, 0xffffffff),
                Arguments.of(0, 0x7f, 0xffffff80, 0xffffffff),
                Arguments.of(0, 0x00, 0xffffffff, 0xffffff80),
                Arguments.of(0, 0x00, 0xffffff80, 0xffffff80),

                Arguments.of(3, 0x7f, 0x0, 0b11_11111000),
                Arguments.of(3, 0x7f, 0xffffff, 0xffffff),
                Arguments.of(3, 0x00, 0x0, 0x0),
                Arguments.of(3, 0x00, 0b11_11111000, 0x0),

                Arguments.of(3, 0x7f, 0b11111111_11111111_11111100_00000111, 0xffffffff),
                Arguments.of(3, 0x7f, 0xffffffff, 0xffffffff),
                Arguments.of(3, 0x00, 0b11111111_11111111_11111100_00000111, 0b11111111_11111111_11111100_00000111),
                Arguments.of(3, 0x00, 0xffffffff, 0b11111111_11111111_11111100_00000111)
        );
    }
}