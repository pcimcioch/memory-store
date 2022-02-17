package com.github.pcimcioch.memorystore.persistence.serializer.collection;

import com.github.pcimcioch.memorystore.persistence.serializer.SerializerTestBase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ByteArraySerializerTest extends SerializerTestBase {

    @ParameterizedTest
    @MethodSource("arrays")
    void serializing(byte[] array) throws IOException {
        // given
        ByteArraySerializer testee = new ByteArraySerializer();

        // when
        testee.serialize(encoder(), array);
        byte[] actual = testee.deserialize(decoder());

        // then
        assertThat(actual).isEqualTo(array);
    }

    public static Stream<byte[]> arrays() {
        return Stream.of(
                null,
                new byte[]{},
                new byte[]{1},
                new byte[]{-1},
                new byte[]{Byte.MIN_VALUE, -1, 0, 1, Byte.MAX_VALUE}
        );
    }
}