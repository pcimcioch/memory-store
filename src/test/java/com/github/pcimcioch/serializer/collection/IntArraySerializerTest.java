package com.github.pcimcioch.serializer.collection;

import com.github.pcimcioch.serializer.SerializerTestBase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class IntArraySerializerTest extends SerializerTestBase {

    @ParameterizedTest
    @MethodSource("arrays")
    void serializing(int[] array) throws IOException {
        // given
        IntArraySerializer testee = new IntArraySerializer();

        // when
        testee.serialize(encoder(), array);
        int[] actual = testee.deserialize(decoder());

        // then
        assertThat(actual).isEqualTo(array);
    }

    public static Stream<int[]> arrays() {
        return Stream.of(
                null,
                new int[]{},
                new int[]{1},
                new int[]{-1},
                new int[]{Integer.MIN_VALUE, -100000, 0, 100000, Integer.MAX_VALUE}
        );
    }
}