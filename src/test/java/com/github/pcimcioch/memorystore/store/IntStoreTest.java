package com.github.pcimcioch.memorystore.store;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class IntStoreTest {

    @ParameterizedTest
    @ValueSource(ints = {0, 1023, 33554433, -1, -1024, -2048, Integer.MAX_VALUE})
    void creationWithIncorrectBlockSize(int minBlockSize) {
        // when
        Throwable thrown = catchThrowable(() -> new IntStore(minBlockSize));

        // then
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("correctBlockSizes")
    void creationWithCorrectBlockSize(int minBlockSize, int blockSize, int numberOfIndexBits, int indexMask) {
        // when
        IntStore testee = new IntStore(minBlockSize);

        // then
        assertThat(testee.blockSize()).isEqualTo(blockSize);
        assertThat(testee.numberOfIndexBits()).isEqualTo(numberOfIndexBits);
        assertThat(testee.indexMask()).isEqualTo(indexMask);
    }

    private static Stream<Arguments> correctBlockSizes() {
        return Stream.of(
                Arguments.of(1024, 1024, 10, 0x3ff),
                Arguments.of(1025, 2048, 11, 0x7ff),
                Arguments.of(4095, 4096, 12, 0xfff),
                Arguments.of(4096, 4096, 12, 0xfff),
                Arguments.of(4097, 8192, 13, 0x1fff),
                Arguments.of(33554431, 33554432, 25, 0x1ffffff),
                Arguments.of(33554432, 33554432, 25, 0x1ffffff)
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 10, -1, -10, Integer.MAX_VALUE, Integer.MIN_VALUE})
    void settingIntValues(int value) {
        // given
        IntStore testee = new IntStore();

        // when
        testee.setInt(0, value);

        // then
        assertThat(testee.getInt(0)).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, 1L, 10L, -1L, -10L, Long.MAX_VALUE, Long.MIN_VALUE})
    void settingLongValues(long value) {
        // given
        IntStore testee = new IntStore();

        // when
        testee.setLong(0, value);

        // then
        assertThat(testee.getLong(0)).isEqualTo(value);
    }

    @Test
    void settingLongValueOnLastElementOfBlock() {
        // given
        IntStore testee = new IntStore();
        int index = 1023;
        long value = 1234L;

        // when
        testee.setLong(index, value);

        // then
        assertThat(testee.getLong(index)).isEqualTo(value);
    }

    @ParameterizedTest
    @MethodSource("blocksCount")
    void settingValuesShouldExpandStore(int minBlockSize, int index, int blocksCount) {
        // given
        IntStore testee = new IntStore(minBlockSize);
        int value = 1234;

        // when
        testee.setInt(index, value);

        // then
        assertThat(testee.getInt(index)).isEqualTo(value);
        assertThat(testee.blocksCount()).isEqualTo(blocksCount);
    }

    private static Stream<Arguments> blocksCount() {
        return Stream.of(
                Arguments.of(1024, 0, 1),
                Arguments.of(1024, 1023, 1),
                Arguments.of(1024, 1024, 2),
                Arguments.of(1024, 5000, 5),
                Arguments.of(2048, 0, 1),
                Arguments.of(2048, 1023, 1),
                Arguments.of(2048, 1024, 1),
                Arguments.of(2048, 5000, 3)
        );
    }

    @Test
    void creatingMultipleBlocks() {
        // given
        IntStore testee = new IntStore(1024);

        // when
        testee.setInt(0, 1);
        testee.setInt(4096, 5);
        testee.setInt(1024, 2);
        testee.setInt(2048, 3);
        testee.setInt(3072, 4);

        // then
        assertThat(testee.blocksCount()).isEqualTo(5);
        assertThat(testee.getInt(0)).isEqualTo(1);
        assertThat(testee.getInt(1024)).isEqualTo(2);
        assertThat(testee.getInt(2048)).isEqualTo(3);
        assertThat(testee.getInt(3072)).isEqualTo(4);
        assertThat(testee.getInt(4096)).isEqualTo(5);
    }

    @ParameterizedTest
    @MethodSource("partialInts")
    void settingPartialInt(int initValue, int value, int mask, int expected) {
        // given
        IntStore testee = new IntStore();
        int index = 0;
        testee.setInt(index, initValue);

        // when
        testee.setPartialInt(index, value, mask);

        // then
        assertThat(testee.getInt(index)).isEqualTo(expected);
    }

    private static Stream<Arguments> partialInts() {
        return Stream.of(
                Arguments.of(0x00, 0x00, 0x00, 0x00),
                Arguments.of(0x00, 0xff, 0xff, 0xff),
                Arguments.of(0x00, 0x00, 0xff, 0x00),
                Arguments.of(0x00, 0xff, 0x0f, 0x0f),
                Arguments.of(0x00, 0xf0, 0x0f, 0x00),
                Arguments.of(0x00, 0b11001100, 0b00111100, 0b00001100),
                Arguments.of(0xff, 0x00, 0x00, 0xff),
                Arguments.of(0xff, 0xff, 0xff, 0xff),
                Arguments.of(0xff, 0x00, 0xff, 0x00),
                Arguments.of(0xff, 0xff, 0x0f, 0xff),
                Arguments.of(0xff, 0xf0, 0x0f, 0xf0),
                Arguments.of(0xff, 0b11001100, 0b00111100, 0b11001111)
        );
    }

    @Test
    void missingValueInBlock() {
        // given
        IntStore testee = new IntStore();
        testee.setInt(1, 1);

        // when
        int value = testee.getInt(0);

        // then
        assertThat(value).isZero();
    }

    @Test
    void missingValueOutsideBlock() {
        // given
        IntStore testee = new IntStore();

        // when
        Throwable thrown = catchThrowable(() -> testee.getInt(0));

        // then
        assertThat(thrown).isInstanceOf(IndexOutOfBoundsException.class);
    }
}