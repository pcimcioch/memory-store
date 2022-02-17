package com.github.pcimcioch.memorystore.persistence.serializer.collection;

import com.github.pcimcioch.memorystore.persistence.serializer.SerializerTestBase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CharArraySerializerTest extends SerializerTestBase {

    @ParameterizedTest
    @MethodSource("arrays")
    void serializing(char[] array) throws IOException {
        // given
        CharArraySerializer testee = new CharArraySerializer();

        // when
        testee.serialize(encoder(), array);
        char[] actual = testee.deserialize(decoder());

        // then
        assertThat(actual).isEqualTo(array);
    }

    public static Stream<char[]> arrays() {
        return Stream.of(
                null,
                new char[]{},
                new char[]{1},
                new char[]{100},
                new char[]{Character.MIN_VALUE, 0, 10, 100, 10000, Character.MAX_VALUE}
        );
    }
}