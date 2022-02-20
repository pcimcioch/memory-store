package com.github.pcimcioch.serializer.collection;

import com.github.pcimcioch.serializer.SerializerTestBase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class BooleanArraySerializerTest extends SerializerTestBase {

    @ParameterizedTest
    @MethodSource("arrays")
    void serializing(boolean[] array) throws IOException {
        // given
        BooleanArraySerializer testee = new BooleanArraySerializer();

        // when
        testee.serialize(encoder(), array);
        boolean[] actual = testee.deserialize(decoder());

        // then
        assertThat(actual).isEqualTo(array);
    }

    private static Stream<boolean[]> arrays() {
        return Stream.of(
                null,
                new boolean[]{},
                new boolean[]{true},
                new boolean[]{false},
                new boolean[]{true, false, false, true}
        );
    }
}