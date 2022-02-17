package com.github.pcimcioch.memorystore.persistence.serializer.collection;

import com.github.pcimcioch.memorystore.persistence.serializer.SerializerTestBase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class LongArraySerializerTest extends SerializerTestBase {

    @ParameterizedTest
    @MethodSource("arrays")
    void serializing(long[] array) throws IOException {
        // given
        LongArraySerializer testee = new LongArraySerializer();

        // when
        testee.serialize(encoder(), array);
        long[] actual = testee.deserialize(decoder());

        // then
        assertThat(actual).isEqualTo(array);
    }

    public static Stream<long[]> arrays() {
        return Stream.of(
                null,
                new long[]{},
                new long[]{1L},
                new long[]{-1L},
                new long[]{Long.MIN_VALUE, -100000L, 0L, 100000L, Long.MAX_VALUE}
        );
    }
}