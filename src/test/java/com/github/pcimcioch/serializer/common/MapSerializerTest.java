package com.github.pcimcioch.serializer.common;

import com.github.pcimcioch.serializer.Serializer;
import com.github.pcimcioch.serializer.SerializerTestBase;
import com.github.pcimcioch.serializer.Serializers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.github.pcimcioch.serializer.Serializers.string;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@SuppressWarnings("unchecked")
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

    private static Stream<Map<String, String>> maps() {
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

    @Test
    void valueSerializerProvider() throws IOException {
        // given
        Map<String, Serializer<Object>> valueSerializers = Map.of(
                "key1", (Serializer) string(),
                "key2", (Serializer) Serializers.intArray()
        );
        MapSerializer<String, Object> testee = new MapSerializer<>(string(), valueSerializers::get, HashMap::new);

        Map<String, Object> map = Map.of(
                "key1", "value1",
                "key2", new int[]{10, 20, 50, -100}
        );

        // when
        testee.serialize(encoder(), map);
        Map<String, Object> actual = testee.deserialize(decoder());

        // then
        assertThat(actual).containsExactlyInAnyOrderEntriesOf(map);
    }

    @Test
    void valueSerializerMissingOnSerialization() throws IOException {
        // given
        Map<String, Serializer<Object>> valueSerializers = Map.of(
                "key1", (Serializer) string()
        );
        MapSerializer<String, Object> testee = new MapSerializer<>(string(), valueSerializers::get, HashMap::new);

        Map<String, Object> map = Map.of(
                "key1", "value1",
                "key2", new int[]{10, 20, 50, -100}
        );

        // when
        Throwable thrown = catchThrowable(() -> testee.serialize(encoder(), map));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Missing value serializer for key key2");
    }

    @Test
    void valueSerializerMissingOnDeserialization() throws IOException {
        // given
        Map<String, Serializer<Object>> valueSerializers = Map.of(
                "key1", (Serializer) string(),
                "key2", (Serializer) Serializers.intArray()
        );
        Map<String, Serializer<Object>> valueDeserializers = Map.of(
                "key1", (Serializer) string()
        );
        MapSerializer<String, Object> testeeSerializer = new MapSerializer<>(string(), valueSerializers::get, HashMap::new);
        MapSerializer<String, Object> testeeDeserializer = new MapSerializer<>(string(), valueDeserializers::get, HashMap::new);

        Map<String, Object> map = Map.of(
                "key1", "value1",
                "key2", new int[]{10, 20, 50, -100}
        );
        testeeSerializer.serialize(encoder(), map);

        // when
        Throwable thrown = catchThrowable(() -> testeeDeserializer.deserialize(decoder()));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Missing value serializer for key key2");
    }

    private static Map<String, String> map(String key, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(key, value);

        return map;
    }
}