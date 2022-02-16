package com.github.pcimcioch.memorystore.persistence.serializer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class BooleanArraySerializerTest extends SerializerTestBase {

    private final BooleanArraySerializer testee = new BooleanArraySerializer();

    @ParameterizedTest
    @MethodSource("arrays")
    void serializing(boolean[] array) throws IOException {
        // when
        testee.serialize(encoder(), array);
        boolean[] actual = testee.deserialize(decoder());

        // then
        assertThat(actual).isEqualTo(array);
    }

    @SuppressWarnings("PrimitiveArrayArgumentToVarargsMethod")
    public static Stream<Arguments> arrays() {
        return Stream.of(
                Arguments.of((boolean[]) null),
                Arguments.of(new boolean[]{}),
                Arguments.of(new boolean[]{true}),
                Arguments.of(new boolean[]{false}),
                Arguments.of(new boolean[]{true, false, false, true})
        );
    }
}