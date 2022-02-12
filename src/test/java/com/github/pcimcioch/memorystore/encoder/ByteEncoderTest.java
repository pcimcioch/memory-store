package com.github.pcimcioch.memorystore.encoder;

import com.github.pcimcioch.memorystore.encoder.BitEncoder.Config;
import com.github.pcimcioch.memorystore.store.IntStore;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class ByteEncoderTest {

    private static final String BITS_COUNT_EX = "Bits Count outside of defined bounds";
    private static final String BIT_SHIFT_EX = "Bit Shift over a limit";
    private static final byte DEFAULT_VALUE = 123;

    private final IntStore store = new IntStore();

    @ParameterizedTest
    @MethodSource("incorrectConfigs")
    void incorrectConfig(int bitShift, int bitsCount, String message) {
        // given
        Config config = new Config(store, 2, 0, bitShift, bitsCount);

        // when
        Throwable thrown = catchThrowable(() -> new ByteEncoder(config));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);
    }

    private static Stream<Arguments> incorrectConfigs() {
        return Stream.of(
                Arguments.of(32, 8, BIT_SHIFT_EX),
                Arguments.of(25, 8, BIT_SHIFT_EX),
                Arguments.of(0, 7, BITS_COUNT_EX),
                Arguments.of(0, 9, BITS_COUNT_EX)
        );
    }

    @ParameterizedTest
    @MethodSource("correctConfigs")
    void correctConfig(int bitShift, int bitsCount) {
        // given
        Config config = new Config(store, 2, 0, bitShift, bitsCount);

        // when
         new ByteEncoder(config);

        // then
        // no exception thrown
    }

    private static Stream<Arguments> correctConfigs() {
        return Stream.of(
                Arguments.of(0, 8),
                Arguments.of(15, 8),
                Arguments.of(24, 8)
        );
    }

    @ParameterizedTest
    @MethodSource("saveValues")
    void savesDifferentValues(byte value, int storeValue) {
        // given
        Config config = new Config(store, 1, 0, 0, 8);
        ByteEncoder testee = new ByteEncoder(config);

        // when
        testee.set(0, value);

        // then
        assertThat(testee.get(0)).isEqualTo(value);
        assertThat(store.getInt(0)).isEqualTo(storeValue);
    }

    private static Stream<Arguments> saveValues() {
        return Stream.of(
                Arguments.of((byte) 0, 0),
                Arguments.of((byte) -1, 255),
                Arguments.of((byte) 100, 100),
                Arguments.of((byte) -100, 156),
                Arguments.of(Byte.MAX_VALUE, Byte.MAX_VALUE),
                Arguments.of(Byte.MIN_VALUE, 128)
        );
    }

    @ParameterizedTest
    @MethodSource("positions")
    void usesCorrectPosition(int recordSize, int positionInRecord, int position, int storePosition) {
        // given
        Config config = new Config(store, recordSize, positionInRecord, 0, 8);
        ByteEncoder testee = new ByteEncoder(config);

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
    void doesNotOverrideOtherValues(int bitShift, byte value, int previousStoreValue, int nextStoreValue) {
        // given
        int position = 0;

        store.setInt(position, previousStoreValue);
        Config config = new Config(store, 1, 0, bitShift, 8);
        ByteEncoder testee = new ByteEncoder(config);

        // when
        testee.set(position, value);

        // then
        assertThat(testee.get(position)).isEqualTo(value);
        assertThat(store.getInt(position)).isEqualTo(nextStoreValue);
    }

    private static Stream<Arguments> bitShifts() {
        return Stream.of(
                Arguments.of(0, (byte) 0xff, 0x0, 0xff),
                Arguments.of(0, (byte) 0xff, 0xff, 0xff),
                Arguments.of(0, (byte) 0x00, 0x0, 0x0),
                Arguments.of(0, (byte) 0x00, 0xff, 0x0),

                Arguments.of(0, (byte) 0xff, 0xffffffff, 0xffffffff),
                Arguments.of(0, (byte) 0xff, 0xffffff00, 0xffffffff),
                Arguments.of(0, (byte) 0x00, 0xffffffff, 0xffffff00),
                Arguments.of(0, (byte) 0x00, 0xffffff00, 0xffffff00),

                Arguments.of(3, (byte) 0xff, 0x0, 0b111_11111000),
                Arguments.of(3, (byte) 0xff, 0xffffff, 0xffffff),
                Arguments.of(3, (byte) 0x00, 0x0, 0x0),
                Arguments.of(3, (byte) 0x00, 0b111_11111000, 0x0),

                Arguments.of(3, (byte) 0xff, 0b11111111_11111111_11111000_00000111, 0xffffffff),
                Arguments.of(3, (byte) 0xff, 0xffffffff, 0xffffffff),
                Arguments.of(3, (byte) 0x00, 0b11111111_11111111_11111000_00000111, 0b11111111_11111111_11111000_00000111),
                Arguments.of(3, (byte) 0x00, 0xffffffff, 0b11111111_11111111_11111000_00000111)
        );
    }
}