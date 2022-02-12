package com.github.pcimcioch.memorystore.encoder;

import com.github.pcimcioch.memorystore.encoder.BitEncoder.Config;
import com.github.pcimcioch.memorystore.store.IntStore;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class UnsignedIntegerEncoderTest {

    private static final String BITS_COUNT_EX = "Bits Count outside of defined bounds";
    private static final String BIT_SHIFT_EX = "Bit Shift over a limit";
    private static final short DEFAULT_VALUE = 1234;

    private final IntStore store = new IntStore();

    @ParameterizedTest
    @MethodSource("incorrectConfigs")
    void incorrectConfig(int bitShift, int bitsCount, String message) {
        // given
        Config config = new Config(store, 2, 0, bitShift, bitsCount);

        // when
        Throwable thrown = catchThrowable(() -> new UnsignedIntegerEncoder(config));

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

    @ParameterizedTest
    @MethodSource("correctConfigs")
    void correctConfig(int bitShift, int bitsCount) {
        // given
        Config config = new Config(store, 2, 0, bitShift, bitsCount);

        // when
        new UnsignedIntegerEncoder(config);

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
    void savesDifferentValues(int bitsCount, int value) {
        // given
        Config config = new Config(store, 1, 0, 0, bitsCount);
        UnsignedIntegerEncoder testee = new UnsignedIntegerEncoder(config);

        // when
        testee.set(0, value);

        // then
        assertThat(testee.get(0)).isEqualTo(value);
        assertThat(store.getInt(0)).isEqualTo(value);
    }

    private static Stream<Arguments> saveValues() {
        return Stream.of(
                Arguments.of(4, 0),
                Arguments.of(4, 3),
                Arguments.of(4, 15),
                Arguments.of(12, 0),
                Arguments.of(12, 1000),
                Arguments.of(12, 4095),
                Arguments.of(31, 0),
                Arguments.of(31, Integer.MAX_VALUE)
        );
    }

    @ParameterizedTest
    @MethodSource("incorrectValues")
    void rejectsIncorrectValues(int bitsCount, int value, String message) {
        // given
        Config config = new Config(store, 1, 0, 0, bitsCount);
        UnsignedIntegerEncoder testee = new UnsignedIntegerEncoder(config);

        // when
        Throwable thrown = catchThrowable(() -> testee.set(0, value));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);
        assertThat(store.blocksCount()).isEqualTo(0);
    }

    private static Stream<Arguments> incorrectValues() {
        return Stream.of(
                Arguments.of(4, -1, "Value must be between [0, 15]"),
                Arguments.of(4, 16, "Value must be between [0, 15]"),
                Arguments.of(4, 1000, "Value must be between [0, 15]"),
                Arguments.of(12, -1, "Value must be between [0, 4095]"),
                Arguments.of(12, 4096, "Value must be between [0, 4095]"),
                Arguments.of(12, 10000, "Value must be between [0, 4095]"),
                Arguments.of(31, -1, "Value must be between [0, 2147483647]")
        );
    }

    @ParameterizedTest
    @MethodSource("positions")
    void usesCorrectPosition(int recordSize, int positionInRecord, int position, int storePosition) {
        // given
        Config config = new Config(store, recordSize, positionInRecord, 0, 12);
        UnsignedIntegerEncoder testee = new UnsignedIntegerEncoder(config);

        // when
        testee.set(position, DEFAULT_VALUE);

        // then
        assertThat(testee.get(position)).isEqualTo(DEFAULT_VALUE);
        assertThat(store.getInt(storePosition)).isEqualTo(DEFAULT_VALUE);
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
        UnsignedIntegerEncoder testee = new UnsignedIntegerEncoder(config);

        // when
        testee.set(position, value);

        // then
        assertThat(testee.get(position)).isEqualTo(value);
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