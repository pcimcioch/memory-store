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
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class OverlappingMemoryLayoutBuilderTest {

    private static final String WORD_SIZE_EX = "Word Size must be over 0";
    private static final String RECORD_SIZE_EX = "Record Size must be over 0";

    @ParameterizedTest
    @MethodSource("incorrectSizes")
    void incorrectSize(int wordSize, int recordSize, String message) {
        // when
        Throwable thrown = catchThrowable(() -> new OverlappingMemoryLayoutBuilder(wordSize, recordSize, Map.of(
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
        Throwable thrown = catchThrowable(() -> new OverlappingMemoryLayoutBuilder(wordSize, recordSize, Map.of(
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
        Throwable thrown = catchThrowable(() -> new OverlappingMemoryLayoutBuilder(wordSize, recordSize, Map.of(
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
        new OverlappingMemoryLayoutBuilder(wordSize, recordSize, Map.of(
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
        OverlappingMemoryLayoutBuilder testee = new OverlappingMemoryLayoutBuilder(32, 8, Map.of(
                header, position(0, 0)
        ));

        // when
        Throwable thrown = catchThrowable(() -> testee.compute(24, singletonList(header)));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("This memory layout supports 32 word size, but 24 requested");
    }

    @Test
    void missingHeader() {
        // given
        BitHeader<TestEncoder> header1 = header("header1", 32, 32);
        BitHeader<TestEncoder> header2 = header("header2", 32, 32);
        BitHeader<TestEncoder> header3 = header("header3", 32, 32);
        OverlappingMemoryLayoutBuilder testee = new OverlappingMemoryLayoutBuilder(32, 8, Map.of(
                header1, position(0, 0),
                header2, position(1, 0)
        ));

        // when
        Throwable thrown = catchThrowable(() -> testee.compute(32, List.of(header1, header3)));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot find Memory Position for header header3");
    }

    @Test
    void correctPositions() {
        // given
        BitHeader<TestEncoder> header1 = header("header1", 32, 32);
        BitHeader<TestEncoder> header2 = header("header2", 30, 64);
        BitHeader<TestEncoder> header3 = header("header3", 8, 32);
        BitHeader<TestEncoder> header4 = header("header4", 20, 32);
        BitHeader<TestEncoder> missingHeader = header("header5", 32, 32);
        OverlappingMemoryLayoutBuilder testee = new OverlappingMemoryLayoutBuilder(32, 7, Map.of(
                header1, position(0, 0),
                header2, position(1, 0),
                header3, position(2, 0),
                header4, position(2, 8)
        ));

        // when
        MemoryLayout layout = testee.compute(32, List.of(header1, header2, header3, header4));

        // then
        assertThat(layout.recordSize()).isEqualTo(7);
        assertThat(layout.memoryPositionFor(header1)).isEqualTo(position(0, 0));
        assertThat(layout.memoryPositionFor(header2)).isEqualTo(position(1, 0));
        assertThat(layout.memoryPositionFor(header3)).isEqualTo(position(2, 0));
        assertThat(layout.memoryPositionFor(header4)).isEqualTo(position(2, 8));
        assertThat(layout.memoryPositionFor(missingHeader)).isNull();
    }

    @Test
    void positionsCanOverlap() {
        // given
        BitHeader<TestEncoder> header1 = header("header1", 32, 32);
        BitHeader<TestEncoder> header2 = header("header2", 30, 64);
        OverlappingMemoryLayoutBuilder testee = new OverlappingMemoryLayoutBuilder(32, 7, Map.of(
                header1, position(0, 0),
                header2, position(0, 0)
        ));

        // when
        MemoryLayout layout = testee.compute(32, List.of(header1, header2));

        // then
        assertThat(layout.recordSize()).isEqualTo(7);
        assertThat(layout.memoryPositionFor(header1)).isEqualTo(position(0, 0));
        assertThat(layout.memoryPositionFor(header2)).isEqualTo(position(0, 0));
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