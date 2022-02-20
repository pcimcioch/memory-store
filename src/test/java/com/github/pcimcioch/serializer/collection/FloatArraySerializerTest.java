package com.github.pcimcioch.serializer.collection;

import com.github.pcimcioch.serializer.SerializerTestBase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class FloatArraySerializerTest extends SerializerTestBase {

    @ParameterizedTest
    @MethodSource("arrays")
    void serializing(float[] array) throws IOException {
        // given
        FloatArraySerializer testee = new FloatArraySerializer();

        // when
        testee.serialize(encoder(), array);
        float[] actual = testee.deserialize(decoder());

        // then
        assertThat(actual).isEqualTo(array);
    }

    public static Stream<float[]> arrays() {
        return Stream.of(
                null,
                new float[]{},
                new float[]{1.0f},
                new float[]{-1.0f},
                new float[]{Float.MIN_VALUE, Float.MIN_NORMAL, Float.NEGATIVE_INFINITY,
                        -10.1f, 0.0f, 10.1f, Float.NaN, Float.MAX_VALUE, Float.POSITIVE_INFINITY}
        );
    }
}