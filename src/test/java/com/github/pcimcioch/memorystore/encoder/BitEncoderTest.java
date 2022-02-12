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

class BitEncoderTest {

    public static final IntStore STORE = new IntStore();
    public static final String NULL_STORE_EX = "Store cannot be null";
    public static final String RECORD_SIZE_EX = "Record size must be greater then 0";
    public static final String POSITION_EX = "Position in record must be greater or equal zero";
    public static final String BIT_SHIFT_EX = "Bit Shift must be greater or equal zero";
    public static final String BIT_COUNT_EX = "Bits Count must be greater or equal zero";
    public static final String FIT_EX = "Configured data will not fit into Record Size";

    @ParameterizedTest
    @MethodSource("correctConfigs")
    void correctConfig(int recordSize, int positionInRecord, int bitShift, int bitsCount) {
        // when then
        new Config(STORE, recordSize, positionInRecord, bitShift, bitsCount);
    }

    private static Stream<Arguments> correctConfigs() {
        return Stream.of(
                Arguments.of(1, 0, 0, 1),
                Arguments.of(10, 5, 56, 64),
                Arguments.of(1, 0, 16, 16),
                Arguments.of(3, 1, 60, 4)
        );
    }

    @Test
    void configWithNullStore() {
        // when
        Throwable thrown = catchThrowable(() -> new Config(null, 1, 0, 0, 1));

        // then
        assertThat(thrown)
                .isInstanceOf(NullPointerException.class)
                .hasMessage(NULL_STORE_EX);
    }

    @ParameterizedTest
    @MethodSource("incorrectConfigs")
    void incorrectConfig(int recordSize, int positionInRecord, int bitShift, int bitsCount, String message) {
        // when
        Throwable thrown = catchThrowable(() -> new Config(STORE, recordSize, positionInRecord, bitShift, bitsCount));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);
    }

    private static Stream<Arguments> incorrectConfigs() {
        return Stream.of(
                Arguments.of(0, 0, 0, 1, RECORD_SIZE_EX),
                Arguments.of(-1, 0, 0, 1, RECORD_SIZE_EX),
                Arguments.of(1, -1, 0, 1, POSITION_EX),
                Arguments.of(1, 0, -1, 1, BIT_SHIFT_EX),
                Arguments.of(1, 0, 0, 0, BIT_COUNT_EX),
                Arguments.of(1, 0, 0, -1, BIT_COUNT_EX),
                Arguments.of(1, 0, 16, 17, FIT_EX),
                Arguments.of(3, 1, 61, 4, FIT_EX),
                Arguments.of(5, 5, 0, 1, FIT_EX)
        );
    }
}