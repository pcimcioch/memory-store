package com.github.pcimcioch.memorystore.persistence.serializer.common;

import com.github.pcimcioch.memorystore.persistence.serializer.SerializerTestBase;
import com.github.pcimcioch.memorystore.persistence.serializer.collection.CollectionSerializer;
import com.github.pcimcioch.memorystore.persistence.serializer.common.MultiTypeSerializer.TypeMapping;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.github.pcimcioch.memorystore.persistence.serializer.Serializers.intArray;
import static com.github.pcimcioch.memorystore.persistence.serializer.Serializers.listOf;
import static com.github.pcimcioch.memorystore.persistence.serializer.Serializers.string;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class MultiTypeSerializerTest extends SerializerTestBase {

    @ParameterizedTest
    @MethodSource("values")
    void serializing(Object value) throws IOException {
        // given
        MultiTypeSerializer<Object> testee = new MultiTypeSerializer<>(List.of(
                new TypeMapping<>(String.class, string()),
                new TypeMapping<>(int[].class, intArray()),
                new TypeMapping<List<String>>((Class) List.class, listOf(string()))
        ));

        // when
        testee.serialize(encoder(), value);
        Object actual = testee.deserialize(decoder());

        // then
        assertThat(actual).isEqualTo(value);
    }

    public static Stream<Object> values() {
        return Stream.of(
                null,
                "some string",
                "",
                new int[]{1, 2, 3},
                new int[0],
                List.of()
        );
    }

    @Test
    void noSerializers() {
        // given
        MultiTypeSerializer<Object> testee = new MultiTypeSerializer<>(List.of());

        // when
        Throwable thrown = catchThrowable(() -> testee.serialize(encoder(), new Object()));

        // then
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void missingSerializer() {
        // given
        MultiTypeSerializer<Object> testee = new MultiTypeSerializer<>(List.of(
                new TypeMapping<>(int[].class, intArray())
        ));

        // when
        Throwable thrown = catchThrowable(() -> testee.serialize(encoder(), "test"));

        // then
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    void inheritance() throws IOException {
        // given
        MultiTypeSerializer<Object> testee = new MultiTypeSerializer<>(List.of(
                new TypeMapping<>(Collection.class, new CollectionSerializer<>(string(), HashSet::new))
        ));

        // when
        testee.serialize(encoder(), List.of("one", "two", "one"));
        Object actual = testee.deserialize(decoder());

        // then
        assertThat(actual.getClass()).isEqualTo(HashSet.class);
        assertThat((Set<String>) actual).containsExactlyInAnyOrder("one", "two");
    }
}