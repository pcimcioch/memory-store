package com.github.pcimcioch.memorystore.persistence.serializer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DoubleArraySerializerTest extends SerializerTestBase {

    private final DoubleArraySerializer testee = new DoubleArraySerializer();

    @ParameterizedTest
    @MethodSource("arrays")
    void serializing(double[] array) throws IOException {
        // when
        testee.serialize(encoder(), array);
        double[] actual = testee.deserialize(decoder());

        // then
        assertThat(actual).isEqualTo(array);
    }

    @SuppressWarnings("PrimitiveArrayArgumentToVarargsMethod")
    public static Stream<Arguments> arrays() {
        return Stream.of(
                Arguments.of((double[]) null),
                Arguments.of(new double[]{}),
                Arguments.of(new double[]{1.0d}),
                Arguments.of(new double[]{-1.0d}),
                Arguments.of(new double[]{Double.MIN_VALUE, Double.MIN_NORMAL, Double.NEGATIVE_INFINITY,
                        -10.1d, 0.0d, 10.1d, Double.NaN, Double.MAX_VALUE, Double.POSITIVE_INFINITY})
        );
    }
}