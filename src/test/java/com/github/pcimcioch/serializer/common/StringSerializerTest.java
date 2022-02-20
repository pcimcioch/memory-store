package com.github.pcimcioch.serializer.common;

import com.github.pcimcioch.serializer.SerializerTestBase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class StringSerializerTest extends SerializerTestBase {

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
        // given
        StringSerializer testee = new StringSerializer();

        // when
        testee.serialize(encoder(), array);
        String actual = testee.deserialize(decoder());

        // then
        assertThat(actual).isEqualTo(array);
    }
}