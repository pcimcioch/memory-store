package com.github.pcimcioch.memorystore.persistence.serializer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class FloatArraySerializerTest extends SerializerTestBase {

    private final FloatArraySerializer testee = new FloatArraySerializer();

    @ParameterizedTest
    @MethodSource("arrays")
    void serializing(float[] array) throws IOException {
        // when
        testee.serialize(encoder(), array);
        float[] actual = testee.deserialize(decoder());

        // then
        assertThat(actual).isEqualTo(array);
    }

    @SuppressWarnings("PrimitiveArrayArgumentToVarargsMethod")
    public static Stream<Arguments> arrays() {
        return Stream.of(
                Arguments.of((float[]) null),
                Arguments.of(new float[]{}),
                Arguments.of(new float[]{1.0f}),
                Arguments.of(new float[]{-1.0f}),
                Arguments.of(new float[]{Float.MIN_VALUE, Float.MIN_NORMAL, Float.NEGATIVE_INFINITY,
                        -10.1f, 0.0f, 10.1f, Float.NaN, Float.MAX_VALUE, Float.POSITIVE_INFINITY})
        );
    }
}