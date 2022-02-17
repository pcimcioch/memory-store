package com.github.pcimcioch.memorystore.persistence.serializer.collection;

import com.github.pcimcioch.memorystore.persistence.serializer.SerializerTestBase;
import com.github.pcimcioch.memorystore.persistence.serializer.collection.ShortArraySerializer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ShortArraySerializerTest extends SerializerTestBase {

    private final ShortArraySerializer testee = new ShortArraySerializer();

    @ParameterizedTest
    @MethodSource("arrays")
    void serializing(short[] array) throws IOException {
        // when
        testee.serialize(encoder(), array);
        short[] actual = testee.deserialize(decoder());

        // then
        assertThat(actual).isEqualTo(array);
    }

    @SuppressWarnings("PrimitiveArrayArgumentToVarargsMethod")
    public static Stream<Arguments> arrays() {
        return Stream.of(
                Arguments.of((short[]) null),
                Arguments.of(new short[]{}),
                Arguments.of(new short[]{1}),
                Arguments.of(new short[]{-1}),
                Arguments.of(new short[]{Short.MIN_VALUE, -10000, 0, 10000, Short.MAX_VALUE})
        );
    }
}