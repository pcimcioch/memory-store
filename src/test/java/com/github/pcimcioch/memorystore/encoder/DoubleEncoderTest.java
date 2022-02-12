package com.github.pcimcioch.memorystore.encoder;

import com.github.pcimcioch.memorystore.encoder.BitEncoder.Config;
import com.github.pcimcioch.memorystore.store.IntStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static java.lang.Double.doubleToRawLongBits;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class DoubleEncoderTest {

    private static final String BITS_COUNT_EX = "Bits Count outside of defined bounds";
    private static final String BIT_SHIFT_EX = "Bit Shift over a limit";
    private static final double DEFAULT_VALUE = 34359738.36865446723d;

    private final IntStore store = new IntStore();

    @ParameterizedTest
    @MethodSource("incorrectConfigs")
    void incorrectConfig(int bitShift, int bitsCount, String message) {
        // given
        Config config = new Config(store, 3, 0, bitShift, bitsCount);

        // when
        Throwable thrown = catchThrowable(() -> new DoubleEncoder(config));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);
    }

    private static Stream<Arguments> incorrectConfigs() {
        return Stream.of(
                Arguments.of(1, 64, BIT_SHIFT_EX),
                Arguments.of(0, 63, BITS_COUNT_EX),
                Arguments.of(0, 65, BITS_COUNT_EX)
        );
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0d, -0.0d, -1.0d, 1234.0d, 34359738.368d, -3435.9738368d,
            Double.MAX_VALUE, Double.MIN_VALUE, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY})
    void savesDifferentValues(double value) {
        // given
        Config config = new Config(store, 2, 0, 0, 64);
        DoubleEncoder testee = new DoubleEncoder(config);

        // when
        testee.set(0, value);

        // then
        assertThat(testee.get(0)).isEqualTo(value);
        assertThat(store.getLong(0)).isEqualTo(doubleToRawLongBits(value));
    }

    @Test
    void savesNaN() {
        // given
        Config config = new Config(store, 2, 0, 0, 64);
        DoubleEncoder testee = new DoubleEncoder(config);

        // when
        testee.set(0, Double.NaN);

        // then
        assertThat(testee.get(0)).isNaN();
        assertThat(store.getLong(0)).isEqualTo(doubleToRawLongBits(Double.NaN));
    }

    @ParameterizedTest
    @MethodSource("positions")
    void usesCorrectPosition(int recordSize, int positionInRecord, int position, int storePosition) {
        // given
        Config config = new Config(store, recordSize, positionInRecord, 0, 64);
        DoubleEncoder testee = new DoubleEncoder(config);

        // when
        testee.set(position, DEFAULT_VALUE);

        // then
        assertThat(testee.get(position)).isEqualTo(DEFAULT_VALUE);
        assertThat(store.getLong(storePosition)).isEqualTo(doubleToRawLongBits(DEFAULT_VALUE));
    }

    private static Stream<Arguments> positions() {
        return Stream.of(
                Arguments.of(2, 0, 0, 0),
                Arguments.of(2, 0, 10, 20),
                Arguments.of(5, 0, 0, 0),
                Arguments.of(5, 0, 3, 15),
                Arguments.of(5, 2, 0, 2),
                Arguments.of(5, 2, 3, 17)
        );
    }
}