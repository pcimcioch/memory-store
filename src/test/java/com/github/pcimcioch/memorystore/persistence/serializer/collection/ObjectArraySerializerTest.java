package com.github.pcimcioch.memorystore.persistence.serializer.collection;

import com.github.pcimcioch.memorystore.persistence.serializer.SerializerTestBase;
import com.github.pcimcioch.memorystore.persistence.serializer.Serializers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectArraySerializerTest extends SerializerTestBase {

    private final ObjectArraySerializer<String> testee = new ObjectArraySerializer<>(String.class, Serializers.string());

    @ParameterizedTest
    @MethodSource("arrays")
    void serializing(String[] array) throws IOException {
        // when
        testee.serialize(encoder(), array);
        String[] actual = testee.deserialize(decoder());

        // then
        assertThat(actual).isEqualTo(array);
    }

    public static Stream<Arguments> arrays() {
        return Stream.of(
                Arguments.of((Object) null),
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{"test"}),
                Arguments.of((Object) new String[]{""}),
                Arguments.of((Object) new String[]{null, "", " ", "test", "some test values"})
        );
    }
}