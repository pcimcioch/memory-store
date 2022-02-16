package com.github.pcimcioch.memorystore.persistence.serializer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ByteArraySerializerTest extends SerializerTestBase {

    private final ByteArraySerializer testee = new ByteArraySerializer();

    @ParameterizedTest
    @MethodSource("arrays")
    void serializing(byte[] array) throws IOException {
        // when
        testee.serialize(encoder(), array);
        byte[] actual = testee.deserialize(decoder());

        // then
        assertThat(actual).isEqualTo(array);
    }

    @SuppressWarnings("PrimitiveArrayArgumentToVarargsMethod")
    public static Stream<Arguments> arrays() {
        return Stream.of(
                Arguments.of((byte[]) null),
                Arguments.of(new byte[]{}),
                Arguments.of(new byte[]{1}),
                Arguments.of(new byte[]{-1}),
                Arguments.of(new byte[]{Byte.MIN_VALUE, -1, 0, 1, Byte.MAX_VALUE})
        );
    }
}