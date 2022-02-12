package com.github.pcimcioch.memorystore.encoder;

import com.github.pcimcioch.memorystore.encoder.BitEncoder.Config;
import com.github.pcimcioch.memorystore.store.IntStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class BooleanEncoderTest {

    private static final String BITS_COUNT_EX = "Bits Count outside of defined bounds";
    private static final String BIT_SHIFT_EX = "Bit Shift over a limit";

    private final IntStore store = new IntStore();

    @ParameterizedTest
    @MethodSource("incorrectConfigs")
    void incorrectConfig(int bitShift, int bitsCount, String message) {
        // given
        Config config = new Config(store, 2, 0, bitShift, bitsCount);

        // when
        Throwable thrown = catchThrowable(() -> new BooleanEncoder(config));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);
    }

    private static Stream<Arguments> incorrectConfigs() {
        return Stream.of(
                Arguments.of(32, 1, BIT_SHIFT_EX),
                Arguments.of(0, 2, BITS_COUNT_EX)
        );
    }

    @ParameterizedTest
    @MethodSource("correctConfigs")
    void correctConfig(int bitShift, int bitsCount) {
        // given
        Config config = new Config(store, 2, 0, bitShift, bitsCount);

        // when
        new BooleanEncoder(config);

        // then
        // no exception thrown
    }

    private static Stream<Arguments> correctConfigs() {
        return Stream.of(
                Arguments.of(31, 1),
                Arguments.of(13, 1),
                Arguments.of(0, 1)
        );
    }

    @Test
    void savesDifferentValues() {
        // given
        Config config = new Config(store, 1, 0, 0, 1);
        BooleanEncoder testee = new BooleanEncoder(config);

        // when
        testee.set(0, true);

        // then
        assertThat(testee.get(0)).isTrue();
        assertThat(store.getInt(0)).isEqualTo(1);

        // when
        testee.set(0, false);

        // then
        assertThat(testee.get(0)).isFalse();
        assertThat(store.getInt(0)).isEqualTo(0);
    }

    @Test
    void savesClearValues() {
        // given
        Config config = new Config(store, 1, 0, 0, 1);
        BooleanEncoder testee = new BooleanEncoder(config);

        // when
        testee.set(0);

        // then
        assertThat(testee.get(0)).isTrue();
        assertThat(store.getInt(0)).isEqualTo(1);

        // when
        testee.clear(0);

        // then
        assertThat(testee.get(0)).isFalse();
        assertThat(store.getInt(0)).isEqualTo(0);
    }

    @ParameterizedTest
    @MethodSource("positions")
    void usesCorrectPosition(int recordSize, int positionInRecord, int position, int storePosition) {
        // given
        Config config = new Config(store, recordSize, positionInRecord, 0, 1);
        BooleanEncoder testee = new BooleanEncoder(config);

        // when
        testee.set(position, true);

        // then
        assertThat(testee.get(position)).isTrue();
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
    @MethodSource("bitShifts")
    void doesNotOverrideOtherValues(int bitShift, boolean value, int previousStoreValue, int nextStoreValue) {
        // given
        int position = 0;

        store.setInt(position, previousStoreValue);
        Config config = new Config(store, 1, 0, bitShift, 1);
        BooleanEncoder testee = new BooleanEncoder(config);

        // when
        testee.set(position, value);

        // then
        assertThat(testee.get(position)).isEqualTo(value);
        assertThat(store.getInt(position)).isEqualTo(nextStoreValue);
    }

    private static Stream<Arguments> bitShifts() {
        return Stream.of(
                Arguments.of(0, true, 0b0, 0b1),
                Arguments.of(0, true, 0b1, 0b1),
                Arguments.of(0, false, 0b0, 0b0),
                Arguments.of(0, false, 0b1, 0b0),

                Arguments.of(0, true, 0b11111110, 0b11111111),
                Arguments.of(0, true, 0b11111111, 0b11111111),
                Arguments.of(0, false, 0b11111110, 0b11111110),
                Arguments.of(0, false, 0b11111111, 0b11111110),

                Arguments.of(3, true, 0b00000000, 0b00001000),
                Arguments.of(3, true, 0b00001000, 0b00001000),
                Arguments.of(3, false, 0b00000000, 0b00000000),
                Arguments.of(3, false, 0b00001000, 0b00000000),

                Arguments.of(3, true, 0b11110111, 0b11111111),
                Arguments.of(3, true, 0b11111111, 0b11111111),
                Arguments.of(3, false, 0b11110111, 0b11110111),
                Arguments.of(3, false, 0b11111111, 0b11110111)
        );
    }
}