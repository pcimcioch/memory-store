package com.github.pcimcioch.memorystore.encoder;

import com.github.pcimcioch.memorystore.encoder.BitEncoder.Config;
import com.github.pcimcioch.memorystore.store.IntStore;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class LongEncoderTest {

    private static final String BITS_COUNT_EX = "Bits Count outside of defined bounds";
    private static final String BIT_SHIFT_EX = "Bit Shift over a limit";
    private static final long DEFAULT_VALUE = 34359738368L;

    private final IntStore store = new IntStore();

    @ParameterizedTest
    @MethodSource("incorrectConfigs")
    void incorrectConfig(int bitShift, int bitsCount, String message) {
        // given
        Config config = new Config(store, 3, 0, bitShift, bitsCount);

        // when
        Throwable thrown = catchThrowable(() -> new LongEncoder(config));

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
    @ValueSource(longs = {0L, -1L, 1234L, 34359738368L, -34359738368L, Long.MAX_VALUE, Long.MIN_VALUE})
    void savesDifferentValues(long value) {
        // given
        Config config = new Config(store, 2, 0, 0, 64);
        LongEncoder testee = new LongEncoder(config);

        // when
        testee.set(0, value);

        // then
        assertThat(testee.get(0)).isEqualTo(value);
        assertThat(store.getLong(0)).isEqualTo(value);
    }

    @ParameterizedTest
    @MethodSource("positions")
    void usesCorrectPosition(int recordSize, int positionInRecord, int position, int storePosition) {
        // given
        Config config = new Config(store, recordSize, positionInRecord, 0, 64);
        LongEncoder testee = new LongEncoder(config);

        // when
        testee.set(position, DEFAULT_VALUE);

        // then
        assertThat(testee.get(position)).isEqualTo(DEFAULT_VALUE);
        assertThat(store.getLong(storePosition)).isEqualTo(DEFAULT_VALUE);
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