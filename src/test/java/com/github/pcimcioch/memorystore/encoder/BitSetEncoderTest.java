package com.github.pcimcioch.memorystore.encoder;

import com.github.pcimcioch.memorystore.store.IntStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.BitSet;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

class BitSetEncoderTest {

    private static final String BITS_COUNT_EX = "Bits Count outside of defined bounds";

    private final IntStore store = new IntStore();

    @ParameterizedTest
    @MethodSource("incorrectConfigs")
    void incorrectConfig(int bitShift, int bitsCount, String message) {
        // given
        BitEncoder.Config config = new BitEncoder.Config(store, 40, 0, bitShift, bitsCount);

        // when
        Throwable thrown = catchThrowable(() -> new BitSetEncoder(config));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);
    }

    private static Stream<Arguments> incorrectConfigs() {
        return Stream.of(
                Arguments.of(0, 1025, BITS_COUNT_EX)
        );
    }

    @ParameterizedTest
    @MethodSource("correctConfigs")
    void correctConfig(int bitShift, int bitsCount) {
        // given
        BitEncoder.Config config = new BitEncoder.Config(store, 40, 0, bitShift, bitsCount);

        // when
        new BitSetEncoder(config);

        // then
        // no exception thrown
    }

    private static Stream<Arguments> correctConfigs() {
        return Stream.of(
                Arguments.of(31, 1),
                Arguments.of(13, 1),
                Arguments.of(0, 1),
                Arguments.of(0, 1024)
        );
    }

    @Test
    void savesDifferentValues() {
        // given
        BitEncoder.Config config = new BitEncoder.Config(store, 1, 0, 0, 10);
        BitSetEncoder testee = new BitSetEncoder(config);

        // when
        testee.set(0, 0, true);
        testee.set(0, 2, true);

        // then
        assertThat(testee.get(0, 0)).isTrue();
        assertThat(testee.get(0, 2)).isTrue();
        assertThat(store.getInt(0)).isEqualTo(5);

        // when
        testee.set(0, 0, false);

        // then
        assertThat(testee.get(0, 0)).isFalse();
        assertThat(testee.get(0, 2)).isTrue();
        assertThat(store.getInt(0)).isEqualTo(4);
    }

    @Test
    void bitShiftOverNextInt() {
        // given
        BitEncoder.Config config = new BitEncoder.Config(store, 2, 0, 12, 30);
        BitSetEncoder testee = new BitSetEncoder(config);

        // when
        testee.set(0, 25);

        // then
        assertThat(testee.get(0, 25)).isTrue();
        assertThat(store.getInt(0)).isEqualTo(0);
        assertThat(store.getInt(1)).isEqualTo(32);
    }

    @Test
    void savesClearValues() {
        // given
        BitEncoder.Config config = new BitEncoder.Config(store, 1, 0, 0, 4);
        BitSetEncoder testee = new BitSetEncoder(config);

        // when
        testee.set(0, 1);
        testee.set(0, 3);

        // then
        assertThat(testee.get(0, 1)).isTrue();
        assertThat(store.getInt(0)).isEqualTo(10);

        // when
        testee.clear(0, 1);

        // then
        assertThat(testee.get(0, 1)).isFalse();
        assertThat(testee.get(0, 3)).isTrue();
        assertThat(store.getInt(0)).isEqualTo(8);
    }

    @ParameterizedTest
    @MethodSource("positions")
    void usesCorrectPosition(int recordSize, int positionInRecord, int position, int storePosition) {
        // given
        BitEncoder.Config config = new BitEncoder.Config(store, recordSize, positionInRecord, 0, 1);
        BitSetEncoder testee = new BitSetEncoder(config);

        // when
        testee.set(position, 0, true);

        // then
        assertThat(testee.get(position, 0)).isTrue();
        assertThat(store.getInt(storePosition)).isEqualTo(1);
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
    @MethodSource("incorrectBitPositions")
    void incorrectBitPosition(int bitPosition, int bitsCount, String message) {
        // given
        BitEncoder.Config config = new BitEncoder.Config(store, 10, 0, 0, bitsCount);
        BitSetEncoder testee = new BitSetEncoder(config);

        // when then
        assertThatThrownBy(() -> testee.set(0, bitPosition, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);

        // when then
        assertThatThrownBy(() -> testee.set(0, bitPosition))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);

        // when then
        assertThatThrownBy(() -> testee.clear(0, bitPosition))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);

        // when then
        assertThatThrownBy(() -> testee.get(0, bitPosition))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);
    }

    private static Stream<Arguments> incorrectBitPositions() {
        return Stream.of(
                Arguments.of(-1, 10, "Bit Position must be between [0, 9]"),
                Arguments.of(10, 10, "Bit Position must be between [0, 9]")
        );
    }

    @ParameterizedTest
    @MethodSource("bitShifts")
    void doesNotOverrideOtherValues(int bitShift, boolean value, int bitPosition, int previousStoreValue, int nextStoreValue) {
        // given
        int position = 0;

        store.setInt(position, previousStoreValue);
        BitEncoder.Config config = new BitEncoder.Config(store, 1, 0, bitShift, 5);
        BitSetEncoder testee = new BitSetEncoder(config);

        // when
        testee.set(position, bitPosition, value);

        // then
        assertThat(testee.get(position, bitPosition)).isEqualTo(value);
        assertThat(store.getInt(position)).isEqualTo(nextStoreValue);
    }

    private static Stream<Arguments> bitShifts() {
        return Stream.of(
                Arguments.of(0, true, 0, 0b0, 0b1),
                Arguments.of(0, true, 0, 0b1, 0b1),
                Arguments.of(0, false, 0, 0b0, 0b0),
                Arguments.of(0, false, 0, 0b1, 0b0),

                Arguments.of(0, true, 2, 0b0, 0b100),
                Arguments.of(0, true, 2, 0b100, 0b100),
                Arguments.of(0, false, 2, 0b0, 0b0),
                Arguments.of(0, false, 2, 0b100, 0b0),

                Arguments.of(0, true, 0, 0b11111110, 0b11111111),
                Arguments.of(0, true, 0, 0b11111111, 0b11111111),
                Arguments.of(0, false, 0, 0b11111110, 0b11111110),
                Arguments.of(0, false, 0, 0b11111111, 0b11111110),

                Arguments.of(0, true, 2, 0b11111011, 0b11111111),
                Arguments.of(0, true, 2, 0b11111111, 0b11111111),
                Arguments.of(0, false, 2, 0b11111011, 0b11111011),
                Arguments.of(0, false, 2, 0b11111111, 0b11111011),

                Arguments.of(3, true, 0, 0b00000000, 0b00001000),
                Arguments.of(3, true, 0, 0b00001000, 0b00001000),
                Arguments.of(3, false, 0, 0b00000000, 0b00000000),
                Arguments.of(3, false, 0, 0b00001000, 0b00000000),

                Arguments.of(3, true, 2, 0b00000000, 0b00100000),
                Arguments.of(3, true, 2, 0b00100000, 0b00100000),
                Arguments.of(3, false, 2, 0b00000000, 0b00000000),
                Arguments.of(3, false, 2, 0b00100000, 0b00000000),

                Arguments.of(3, true, 0, 0b11110111, 0b11111111),
                Arguments.of(3, true, 0, 0b11111111, 0b11111111),
                Arguments.of(3, false, 0, 0b11110111, 0b11110111),
                Arguments.of(3, false, 0, 0b11111111, 0b11110111),

                Arguments.of(3, true, 2, 0b11011111, 0b11111111),
                Arguments.of(3, true, 2, 0b11111111, 0b11111111),
                Arguments.of(3, false, 2, 0b11011111, 0b11011111),
                Arguments.of(3, false, 2, 0b11111111, 0b11011111)
        );
    }
}