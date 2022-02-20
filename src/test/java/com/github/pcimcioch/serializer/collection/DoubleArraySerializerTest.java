package com.github.pcimcioch.serializer.collection;

import com.github.pcimcioch.serializer.SerializerTestBase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DoubleArraySerializerTest extends SerializerTestBase {

    @ParameterizedTest
    @MethodSource("arrays")
    void serializing(double[] array) throws IOException {
        // given
        DoubleArraySerializer testee = new DoubleArraySerializer();

        // when
        testee.serialize(encoder(), array);
        double[] actual = testee.deserialize(decoder());

        // then
        assertThat(actual).isEqualTo(array);
    }

    public static Stream<double[]> arrays() {
        return Stream.of(
                null,
                new double[]{},
                new double[]{1.0d},
                new double[]{-1.0d},
                new double[]{Double.MIN_VALUE, Double.MIN_NORMAL, Double.NEGATIVE_INFINITY,
                        -10.1d, 0.0d, 10.1d, Double.NaN, Double.MAX_VALUE, Double.POSITIVE_INFINITY}
        );
    }
}