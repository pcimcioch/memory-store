package com.github.pcimcioch.serializer.compressed;

import com.github.pcimcioch.serializer.SerializerTestBase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class CompressedIntegerSerializerTest extends SerializerTestBase {

    @ParameterizedTest
    @ValueSource(longs = {0L, 1L, -1L, 1000L, -1000L, 100_000_000_000L, -100_000_000_000L, Long.MAX_VALUE, Long.MIN_VALUE})
    void serialization(long value) throws IOException {
        // given
        CompressedIntegerSerializer testee = new CompressedIntegerSerializer();

        // when
        testee.serialize(encoder(), value);
        long actual = testee.deserialize(decoder());

        // then
        assertThat(actual).isEqualTo(value);
    }
}