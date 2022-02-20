package com.github.pcimcioch.serializer.collection;

import com.github.pcimcioch.serializer.SerializerTestBase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ShortArraySerializerTest extends SerializerTestBase {

    @ParameterizedTest
    @MethodSource("arrays")
    void serializing(short[] array) throws IOException {
        // given
        ShortArraySerializer testee = new ShortArraySerializer();

        // when
        testee.serialize(encoder(), array);
        short[] actual = testee.deserialize(decoder());

        // then
        assertThat(actual).isEqualTo(array);
    }

    private static Stream<short[]> arrays() {
        return Stream.of(
                null,
                new short[]{},
                new short[]{1},
                new short[]{-1},
                new short[]{Short.MIN_VALUE, -10000, 0, 10000, Short.MAX_VALUE}
        );
    }
}