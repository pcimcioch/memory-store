package com.github.pcimcioch.memorystore.layout;

import com.github.pcimcioch.memorystore.encoder.BitEncoder;
import com.github.pcimcioch.memorystore.header.BitHeader;
import com.github.pcimcioch.memorystore.layout.MemoryLayoutBuilder.MemoryLayout;
import com.github.pcimcioch.memorystore.layout.MemoryLayoutBuilder.MemoryPosition;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;

class AutomaticMemoryLayoutBuilderTest {

    private final AutomaticMemoryLayoutBuilder testee = new AutomaticMemoryLayoutBuilder();

    @ParameterizedTest
    @MethodSource("incorrectHeaderValues")
    void incorrectHeaders(int wordSize, List<BitHeader<?>> headers) {
        // when
        Throwable thrown = catchThrowable(() -> testee.compute(wordSize, headers));

        // then
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    private static Stream<Arguments> incorrectHeaderValues() {
        return Stream.of(
                Arguments.of(32, List.of(
                        header("header1", 16, 16)
                )),
                Arguments.of(32, List.of(
                        header("header1", 40, 48)
                ))
        );
    }

    @ParameterizedTest
    @MethodSource("correctHeaderValues")
    void correctHeaders(int wordSize, int recordSize, List<BitHeader<?>> headers) {
        // when
        MemoryLayout memoryLayout = testee.compute(wordSize, headers);

        // then
        assertContainsAllHeaders(memoryLayout, headers);
        assertRecordSize(memoryLayout, recordSize);
        assertNonOverlapping(wordSize, memoryLayout, headers);
    }

    private static Stream<Arguments> correctHeaderValues() {
        return Stream.of(
                Arguments.of(32, 1, List.of(
                        header("header1", 32, 32)
                )),
                Arguments.of(32, 4, List.of(
                        header("header1", 32, 32),
                        header("header2", 7, 32),
                        header("header3", 16, 32),
                        header("header4", 64, 64),
                        header("header5", 1, 32)
                )),
                Arguments.of(16, 3, List.of(
                        header("header1", 11, 16),
                        header("header2", 10, 16),
                        header("header3", 9, 16)
                )),
                Arguments.of(16, 2, List.of(
                        header("header1", 11, 16),
                        header("header2", 10, 32),
                        header("header3", 9, 16)
                ))
        );
    }

    private void assertContainsAllHeaders(MemoryLayout memoryLayout, List<BitHeader<?>> headers) {
        for (BitHeader<?> header : headers) {
            assertThat(memoryLayout.memoryPositionFor(header)).isNotNull();
        }
    }

    private void assertRecordSize(MemoryLayout memoryLayout, int recordSize) {
        assertThat(memoryLayout.recordSize()).isEqualTo(recordSize);
    }

    private void assertNonOverlapping(int wordSize, MemoryLayout memoryLayout, List<BitHeader<?>> headers) {
        Map<BitHeader<?>, MemoryPosition> headerMemoryPosition = headers.stream()
                .collect(toMap(
                        Function.identity(),
                        memoryLayout::memoryPositionFor
                ));

        assertDoesNotThrow(() -> new NonOverlappingMemoryLayoutBuilder(wordSize, memoryLayout.recordSize(), headerMemoryPosition));
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