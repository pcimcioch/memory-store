package com.github.pcimcioch.memorystore.store;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class ObjectStoreTest {

    @ParameterizedTest
    @ValueSource(ints = {0, 1023, 33554433, -1, -1024, -2048, Integer.MAX_VALUE})
    void creationWithIncorrectBlockSize(int minBlockSize) {
        // when
        Throwable thrown = catchThrowable(() -> new ObjectStore<String>(minBlockSize));

        // then
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("correctBlockSizes")
    void creationWithCorrectBlockSize(int minBlockSize, int blockSize, int numberOfIndexBits, int indexMask) {
        // when
        ObjectStore<String> testee = new ObjectStore<>(minBlockSize);

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
    @ValueSource(strings = {" ", "First", "Second", "\n"})
    @NullAndEmptySource
    void settingValues(String value) {
        // given
        ObjectStore<String> testee = new ObjectStore<>();

        // when
        testee.set(0, value);

        // then
        assertThat(testee.get(0)).isEqualTo(value);
    }

    @ParameterizedTest
    @MethodSource("blocksCount")
    void settingValuesShouldExpandStore(int minBlockSize, int index, int blocksCount) {
        // given
        ObjectStore<String> testee = new ObjectStore<>(minBlockSize);
        String value = "Value";

        // when
        testee.set(index, value);

        // then
        assertThat(testee.get(index)).isEqualTo(value);
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
        ObjectStore<String> testee = new ObjectStore<>(1024);

        // when
        testee.set(0, "First");
        testee.set(4096, "Fifth");
        testee.set(1024, "Second");
        testee.set(2048, "Third");
        testee.set(3072, "Fourth");

        // then
        assertThat(testee.blocksCount()).isEqualTo(5);
        assertThat(testee.get(0)).isEqualTo("First");
        assertThat(testee.get(1024)).isEqualTo("Second");
        assertThat(testee.get(2048)).isEqualTo("Third");
        assertThat(testee.get(3072)).isEqualTo("Fourth");
        assertThat(testee.get(4096)).isEqualTo("Fifth");
    }

    @Test
    void missingValueInBlock() {
        // given
        ObjectStore<String> testee = new ObjectStore<>();
        testee.set(1, "Value");

        // when
        String value = testee.get(0);

        // then
        assertThat(value).isNull();
    }

    @Test
    void missingValueOutsideBlock() {
        // given
        ObjectStore<String> testee = new ObjectStore<>();

        // when
        Throwable thrown = catchThrowable(() -> testee.get(0));

        // then
        assertThat(thrown).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void sizeOfEmpty() {
        // given
        ObjectStore<String> testee = new ObjectStore<>();

        // when
        long size = testee.size();

        // then
        assertThat(size).isZero();
    }

    @ParameterizedTest
    @MethodSource("sizes")
    void sizeOfBlock(int index, int expectedSize) {
        // given
        ObjectStore<String> testee = new ObjectStore<>(1024);
        testee.set(index, "test");

        // when
        long size = testee.size();

        // then
        assertThat(size).isEqualTo(expectedSize);
    }

    public static Stream<Arguments> sizes() {
        return Stream.of(
                Arguments.of(0, 1024),
                Arguments.of(1023, 1024),
                Arguments.of(1024, 2048),
                Arguments.of(2047, 2048),
                Arguments.of(2048, 3072)
        );
    }

    @Test
    void sizeOfBlockAfterRemoval() {
        // given
        ObjectStore<String> testee = new ObjectStore<>(1024);
        testee.set(2000, "test");
        testee.set(2000, null);

        // when
        long size = testee.size();

        // then
        assertThat(size).isEqualTo(2048);
    }
}