package com.github.pcimcioch.memorystore.persistence.serializer.common;

import com.github.pcimcioch.memorystore.persistence.serializer.SerializerTestBase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.github.pcimcioch.memorystore.persistence.serializer.Serializers.string;
import static org.assertj.core.api.Assertions.assertThat;

class MapSerializerTest extends SerializerTestBase {

    @ParameterizedTest
    @MethodSource("maps")
    void serializing(Map<String, String> map) throws IOException {
        // given
        MapSerializer<String, String> testee = new MapSerializer<>(string(), string(), HashMap::new);

        // when
        testee.serialize(encoder(), map);
        Map<String, String> actual = testee.deserialize(decoder());

        // then
        assertThat(actual).isEqualTo(map);
    }

    public static Stream<Map<String, String>> maps() {
        return Stream.of(
                null,
                Map.of("key", "value"),
                map("key", null),
                map(null, "value"),
                Map.of(
                        "key1", "value1",
                        "key2", "value2",
                        "key3", "value3"
                )
        );
    }

    private static Map<String, String> map(String key, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(key, value);

        return map;
    }
}