package com.github.pcimcioch.memorystore.encoder;

import com.github.pcimcioch.memorystore.encoder.BitEncoder.Config;
import com.github.pcimcioch.memorystore.store.IntStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static java.lang.Float.floatToRawIntBits;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class FloatEncoderTest {

    private static final String BITS_COUNT_EX = "Bits Count outside of defined bounds";
    private static final String BIT_SHIFT_EX = "Bit Shift over a limit";
    private static final float DEFAULT_VALUE = 12.34f;

    private final IntStore store = new IntStore();

    @ParameterizedTest
    @MethodSource("incorrectConfigs")
    void incorrectConfig(int bitShift, int bitsCount, String message) {
        // given
        Config config = new Config(store, 2, 0, bitShift, bitsCount);

        // when
        Throwable thrown = catchThrowable(() -> new FloatEncoder(config));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);
    }

    private static Stream<Arguments> incorrectConfigs() {
        return Stream.of(
                Arguments.of(1, 32, BIT_SHIFT_EX),
                Arguments.of(0, 31, BITS_COUNT_EX),
                Arguments.of(0, 33, BITS_COUNT_EX)
        );
    }

    @ParameterizedTest
    @ValueSource(floats = {0.0f, -0.0f, -1.0f, 12.34f,
            Float.MAX_VALUE, Float.MIN_VALUE, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY})
    void savesDifferentValues(float value) {
        // given
        Config config = new Config(store, 1, 0, 0, 32);
        FloatEncoder testee = new FloatEncoder(config);

        // when
        testee.set(0, value);

        // then
        assertThat(testee.get(0)).isEqualTo(value);
        assertThat(store.getInt(0)).isEqualTo(floatToRawIntBits(value));
    }

    @Test
    void savesNaN() {
        // given
        Config config = new Config(store, 1, 0, 0, 32);
        FloatEncoder testee = new FloatEncoder(config);

        // when
        testee.set(0, Float.NaN);

        // then
        assertThat(testee.get(0)).isNaN();
        assertThat(store.getInt(0)).isEqualTo(floatToRawIntBits(Float.NaN));
    }

    @ParameterizedTest
    @MethodSource("positions")
    void usesCorrectPosition(int recordSize, int positionInRecord, int position, int storePosition) {
        // given
        Config config = new Config(store, recordSize, positionInRecord, 0, 32);
        FloatEncoder testee = new FloatEncoder(config);

        // when
        testee.set(position, DEFAULT_VALUE);

        // then
        assertThat(testee.get(position)).isEqualTo(DEFAULT_VALUE);
        assertThat(store.getInt(storePosition)).isEqualTo(floatToRawIntBits(DEFAULT_VALUE));
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
}