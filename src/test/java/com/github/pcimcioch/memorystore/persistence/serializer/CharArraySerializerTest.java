package com.github.pcimcioch.memorystore.persistence.serializer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CharArraySerializerTest extends SerializerTestBase {

    private final CharArraySerializer testee = new CharArraySerializer();

    @ParameterizedTest
    @MethodSource("arrays")
    void serializing(char[] array) throws IOException {
        // when
        testee.serialize(encoder(), array);
        char[] actual = testee.deserialize(decoder());

        // then
        assertThat(actual).isEqualTo(array);
    }

    @SuppressWarnings("PrimitiveArrayArgumentToVarargsMethod")
    public static Stream<Arguments> arrays() {
        return Stream.of(
                Arguments.of((char[]) null),
                Arguments.of(new char[]{}),
                Arguments.of(new char[]{1}),
                Arguments.of(new char[]{100}),
                Arguments.of(new char[]{Character.MIN_VALUE, 0, 10, 100, 10000, Character.MAX_VALUE})
        );
    }
}