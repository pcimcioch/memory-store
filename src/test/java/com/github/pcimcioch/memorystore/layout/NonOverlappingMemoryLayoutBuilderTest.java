package com.github.pcimcioch.memorystore.layout;

import com.github.pcimcioch.memorystore.encoder.BitEncoder;
import com.github.pcimcioch.memorystore.header.BitHeader;
import com.github.pcimcioch.memorystore.layout.MemoryLayoutBuilder.MemoryLayout;
import com.github.pcimcioch.memorystore.layout.MemoryLayoutBuilder.MemoryPosition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class NonOverlappingMemoryLayoutBuilderTest {

    private static final String WORD_SIZE_EX = "Word Size must be over 0";
    private static final String RECORD_SIZE_EX = "Record Size must be over 0";

    @ParameterizedTest
    @MethodSource("incorrectSizes")
    void incorrectSize(int wordSize, int recordSize, String message) {
        // when
        Throwable thrown = catchThrowable(() -> new NonOverlappingMemoryLayoutBuilder(wordSize, recordSize, Map.of(
                header("header", 8, 32), position(0, 0)
        )));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);
    }

    private static Stream<Arguments> incorrectSizes() {
        return Stream.of(
                Arguments.of(-10, 8, WORD_SIZE_EX),
                Arguments.of(-1, 8, WORD_SIZE_EX),
                Arguments.of(0, 8, WORD_SIZE_EX),

                Arguments.of(32, -10, RECORD_SIZE_EX),
                Arguments.of(32, -1, RECORD_SIZE_EX),
                Arguments.of(32, 0, RECORD_SIZE_EX)
        );
    }

    @ParameterizedTest
    @MethodSource("recordSizeMisfits")
    void doesNotFitIntoSize(int wordSize, int recordSize, BitHeader<TestEncoder> header, MemoryPosition position) {
        // when
        Throwable thrown = catchThrowable(() -> new NonOverlappingMemoryLayoutBuilder(wordSize, recordSize, Map.of(
                header, position
        )));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Memory Position for header header is incorrect. It will not fit into defined Record Size");
    }

    private static Stream<Arguments> recordSizeMisfits() {
        return Stream.of(
                Arguments.of(32, 2, header("header", 8, 32), position(2, 0)),
                Arguments.of(32, 2, header("header", 33, 64), position(1, 0)),
                Arguments.of(8, 2, header("header", 17, 32), position(0, 0))
        );
    }

    @ParameterizedTest
    @MethodSource("maxLastBitMisfits")
    void doesNotFitIntoMaxLastBit(int wordSize, int recordSize, BitHeader<TestEncoder> header, MemoryPosition position) {
        // when
        Throwable thrown = catchThrowable(() -> new NonOverlappingMemoryLayoutBuilder(wordSize, recordSize, Map.of(
                header, position
        )));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Memory Position for header header is incorrect. Maximum Last Bit for this header is violated");
    }

    private static Stream<Arguments> maxLastBitMisfits() {
        return Stream.of(
                Arguments.of(32, 2, header("header", 32, 32), position(0, 1)),
                Arguments.of(8, 4, header("header", 24, 24), position(0, 7))
        );
    }

    @ParameterizedTest
    @MethodSource("correctRecordSizes")
    void fitsIntoSize(int wordSize, int recordSize, BitHeader<TestEncoder> header, MemoryPosition position) {
        // when then
        new NonOverlappingMemoryLayoutBuilder(wordSize, recordSize, Map.of(
                header, position
        ));
    }

    private static Stream<Arguments> correctRecordSizes() {
        return Stream.of(
                Arguments.of(32, 2, header("header", 8, 32), position(1, 0)),
                Arguments.of(32, 2, header("header", 32, 32), position(1, 0)),
                Arguments.of(8, 2, header("header", 16, 32), position(0, 0)),

                Arguments.of(32, 2, header("header", 32, 32), position(0, 0)),
                Arguments.of(8, 4, header("header", 24, 32), position(0, 7))
        );
    }

    @Test
    void computeMustUseTheSameWordSize() {
        // given
        BitHeader<TestEncoder> header = header("header", 32, 32);
        NonOverlappingMemoryLayoutBuilder testee = new NonOverlappingMemoryLayoutBuilder(32, 8, Map.of(
                header, position(0, 0)
        ));

        // when
        Throwable thrown = catchThrowable(() -> testee.compute(24, singletonList(header)));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("This memory layout supports 32 word size, but 24 requested");
    }

    @ParameterizedTest
    @MethodSource("correctPositionValues")
    void correctPositions(int wordSize, int recordSize, Map<? extends BitHeader<?>, MemoryPosition> positions) {
        // given
        NonOverlappingMemoryLayoutBuilder testee = new NonOverlappingMemoryLayoutBuilder(wordSize, recordSize, positions);

        // when
        MemoryLayout layout = testee.compute(wordSize, positions.keySet());

        // then
        assertThat(layout.recordSize()).isEqualTo(recordSize);
        for (Entry<? extends BitHeader<?>, MemoryPosition> entry : positions.entrySet()) {
            assertThat(layout.memoryPositionFor(entry.getKey())).isEqualTo(entry.getValue());
        }
    }

    private static Stream<Arguments> correctPositionValues() {
        return Stream.of(
                Arguments.of(32, 7, Map.of(
                        header("header1", 32, 32), position(0, 0),
                        header("header2", 30, 64), position(1, 0),
                        header("header3", 8, 32), position(2, 0),
                        header("header4", 20, 32), position(2, 8)
                )),
                Arguments.of(16, 5, Map.of(
                        header("header1", 30, 32), position(0, 0),
                        header("header2", 5, 16), position(2, 0),
                        header("header3", 3, 16), position(2, 5),
                        header("header4", 30, 64), position(2, 8),
                        header("header5", 7, 16), position(4, 6)
                )),
                Arguments.of(32, 1, Map.of(
                        header("header1", 16, 32), position(0, 0),
                        header("header2", 16, 32), position(0, 16)
                ))
        );
    }

    @ParameterizedTest
    @MethodSource("overlappingPositionValues")
    void overlappingPositions(int wordSize, int recordSize, Map<? extends BitHeader<?>, MemoryPosition> positions) {
        // when
        Throwable thrown = catchThrowable(() -> new NonOverlappingMemoryLayoutBuilder(wordSize, recordSize, positions));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Incorrect configuration. Headers are overlapping");
    }

    private static Stream<Arguments> overlappingPositionValues() {
        return Stream.of(
                Arguments.of(32, 1, Map.of(
                        header("header1", 16, 32), position(0, 0),
                        header("header2", 16, 32), position(0, 15)
                )),
                Arguments.of(16, 2, Map.of(
                        header("header1", 20, 32), position(0, 0),
                        header("header2", 5, 16), position(1, 0)
                ))
        );
    }

    private static MemoryPosition position(int positionInRecord, int bitShift) {
        return new MemoryPosition(positionInRecord, bitShift);
    }

    private static BitHeader<TestEncoder> header(String name, int bitsCount, int maxLastBit) {
        return new BitHeader<>(name, bitsCount, maxLastBit, TestEncoder::new);
    }

    private static class TestEncoder extends BitEncoder {

        protected TestEncoder(Config config) {
            super(config);
        }

        @Override
        protected int minBits() {
            return 0;
        }

        @Override
        protected int maxBits() {
            return 0;
        }

        @Override
        protected int maxLastBit() {
            return 0;
        }
    }
}