package com.github.pcimcioch.memorystore.persistence.serializer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class StringSerializerTest extends SerializerTestBase {

    private final StringSerializer testee = new StringSerializer();

    @ParameterizedTest
    @ValueSource(strings = {
            " ",
            "\n",
            "test",
            "test test\ttest\nTest",
            "śćńź"
    })
    @NullAndEmptySource
    void serializing(String array) throws IOException {
        // when
        testee.serialize(encoder(), array);
        String actual = testee.deserialize(decoder());

        // then
        assertThat(actual).isEqualTo(array);
    }
}