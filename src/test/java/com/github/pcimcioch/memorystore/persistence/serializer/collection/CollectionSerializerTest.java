package com.github.pcimcioch.memorystore.persistence.serializer.collection;

import com.github.pcimcioch.memorystore.persistence.serializer.SerializerTestBase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.github.pcimcioch.memorystore.persistence.serializer.Serializers.string;
import static org.assertj.core.api.Assertions.assertThat;

class CollectionSerializerTest extends SerializerTestBase {

    @ParameterizedTest
    @MethodSource("collections")
    void serializing(List<String> collection) throws IOException {
        // given
        CollectionSerializer<String, List<String>> testee = new CollectionSerializer<>(string(), ArrayList::new);

        // when
        testee.serialize(encoder(), collection);
        List<String> actual = testee.deserialize(decoder());

        // then
        assertThat(actual).isEqualTo(collection);
    }

    public static Stream<List<String>> collections() {
        return Stream.of(
                null,
                List.of(),
                List.of("element"),
                List.of("one", "two", "three")
        );
    }
}