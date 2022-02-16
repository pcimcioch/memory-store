package com.github.pcimcioch.memorystore.persistence.serializer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class LongArraySerializerTest extends SerializerTestBase {

    private final LongArraySerializer testee = new LongArraySerializer();

    @ParameterizedTest
    @MethodSource("arrays")
    void serializing(long[] array) throws IOException {
        // when
        testee.serialize(encoder(), array);
        long[] actual = testee.deserialize(decoder());

        // then
        assertThat(actual).isEqualTo(array);
    }

    @SuppressWarnings("PrimitiveArrayArgumentToVarargsMethod")
    public static Stream<Arguments> arrays() {
        return Stream.of(
                Arguments.of((long[]) null),
                Arguments.of(new long[]{}),
                Arguments.of(new long[]{1L}),
                Arguments.of(new long[]{-1L}),
                Arguments.of(new long[]{Long.MIN_VALUE, -100000L, 0L, 100000L, Long.MAX_VALUE})
        );
    }
}