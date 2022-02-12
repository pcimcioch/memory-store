package com.github.pcimcioch.memorystore.encoder;

import com.github.pcimcioch.memorystore.encoder.BitEncoder.Config;
import com.github.pcimcioch.memorystore.store.IntStore;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class CharEncoderTest {

    private static final String BITS_COUNT_EX = "Bits Count outside of defined bounds";
    private static final String BIT_SHIFT_EX = "Bit Shift over a limit";
    private static final char DEFAULT_VALUE = 'z';

    private final IntStore store = new IntStore();

    @ParameterizedTest
    @MethodSource("incorrectConfigs")
    void incorrectConfig(int bitShift, int bitsCount, String message) {
        // given
        Config config = new Config(store, 2, 0, bitShift, bitsCount);

        // when
        Throwable thrown = catchThrowable(() -> new CharEncoder(config));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);
    }

    private static Stream<Arguments> incorrectConfigs() {
        return Stream.of(
                Arguments.of(32, 16, BIT_SHIFT_EX),
                Arguments.of(17, 16, BIT_SHIFT_EX),
                Arguments.of(0, 15, BITS_COUNT_EX),
                Arguments.of(0, 17, BITS_COUNT_EX)
        );
    }

    @ParameterizedTest
    @MethodSource("correctConfigs")
    void correctConfig(int bitShift, int bitsCount) {
        // given
        Config config = new Config(store, 2, 0, bitShift, bitsCount);

        // when
        new CharEncoder(config);

        // then
        // no exception thrown
    }

    private static Stream<Arguments> correctConfigs() {
        return Stream.of(
                Arguments.of(0, 16),
                Arguments.of(7, 16),
                Arguments.of(16, 16)
        );
    }

    @ParameterizedTest
    @MethodSource("saveValues")
    void savesDifferentValues(char value, int storeValue) {
        // given
        Config config = new Config(store, 1, 0, 0, 16);
        CharEncoder testee = new CharEncoder(config);

        // when
        testee.set(0, value);

        // then
        assertThat(testee.get(0)).isEqualTo(value);
        assertThat(store.getInt(0)).isEqualTo(storeValue);
    }

    private static Stream<Arguments> saveValues() {
        return Stream.of(
                Arguments.of('a', 97),
                Arguments.of('z', 122),
                Arguments.of((char) 260, 260),
                Arguments.of(Character.MAX_VALUE, 0xffff),
                Arguments.of(Character.MIN_VALUE, 0)
        );
    }

    @ParameterizedTest
    @MethodSource("positions")
    void usesCorrectPosition(int recordSize, int positionInRecord, int position, int storePosition) {
        // given
        Config config = new Config(store, recordSize, positionInRecord, 0, 16);
        CharEncoder testee = new CharEncoder(config);

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
    void doesNotOverrideOtherValues(int bitShift, char value, int previousStoreValue, int nextStoreValue) {
        // given
        int position = 0;

        store.setInt(position, previousStoreValue);
        Config config = new Config(store, 1, 0, bitShift, 16);
        CharEncoder testee = new CharEncoder(config);

        // when
        testee.set(position, value);

        // then
        assertThat(testee.get(position)).isEqualTo(value);
        assertThat(store.getInt(position)).isEqualTo(nextStoreValue);
    }

    private static Stream<Arguments> bitShifts() {
        return Stream.of(
                Arguments.of(0, (char) 0xffff, 0x0, 0xffff),
                Arguments.of(0, (char) 0xffff, 0xffff, 0xffff),
                Arguments.of(0, (char) 0x0000, 0x0, 0x0),
                Arguments.of(0, (char) 0x0000, 0xffff, 0x0),

                Arguments.of(0, (char) 0xffff, 0xffffffff, 0xffffffff),
                Arguments.of(0, (char) 0xffff, 0xffff0000, 0xffffffff),
                Arguments.of(0, (char) 0x0000, 0xffffffff, 0xffff0000),
                Arguments.of(0, (char) 0x0000, 0xffff0000, 0xffff0000),

                Arguments.of(3, (char) 0xffff, 0x0, 0b111_11111111_11111000),
                Arguments.of(3, (char) 0xffff, 0xffffff, 0xffffff),
                Arguments.of(3, (char) 0x0000, 0x0, 0x0),
                Arguments.of(3, (char) 0x0000, 0b111_11111111_11111000, 0x0),

                Arguments.of(3, (char) 0xffff, 0b11111111_11111000_00000000_00000111, 0xffffffff),
                Arguments.of(3, (char) 0xffff, 0xffffffff, 0xffffffff),
                Arguments.of(3, (char) 0x0000, 0b11111111_11111000_00000000_00000111, 0b11111111_11111000_00000000_00000111),
                Arguments.of(3, (char) 0x0000, 0xffffffff, 0b11111111_11111000_00000000_00000111)
        );
    }
}