package com.github.pcimcioch.memorystore.persistence.serializer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class IntArraySerializerTest extends SerializerTestBase {

    private final IntArraySerializer testee = new IntArraySerializer();

    @ParameterizedTest
    @MethodSource("arrays")
    void serializing(int[] array) throws IOException {
        // when
        testee.serialize(encoder(), array);
        int[] actual = testee.deserialize(decoder());

        // then
        assertThat(actual).isEqualTo(array);
    }

    @SuppressWarnings("PrimitiveArrayArgumentToVarargsMethod")
    public static Stream<Arguments> arrays() {
        return Stream.of(
                Arguments.of((int[]) null),
                Arguments.of(new int[]{}),
                Arguments.of(new int[]{1}),
                Arguments.of(new int[]{-1}),
                Arguments.of(new int[]{Integer.MIN_VALUE, -100000, 0, 100000, Integer.MAX_VALUE})
        );
    }
}