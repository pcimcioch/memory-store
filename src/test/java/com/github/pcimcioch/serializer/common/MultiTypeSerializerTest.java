package com.github.pcimcioch.serializer.common;

import com.github.pcimcioch.serializer.SerializerTestBase;
import com.github.pcimcioch.serializer.Serializers;
import com.github.pcimcioch.serializer.collection.CollectionSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class MultiTypeSerializerTest extends SerializerTestBase {

    @ParameterizedTest
    @MethodSource("values")
    void serializing(Object value) throws IOException {
        // given
        MultiTypeSerializer<Object> testee = new MultiTypeSerializer<>(List.of(
                new MultiTypeSerializer.TypeMapping<>(String.class, Serializers.string()),
                new MultiTypeSerializer.TypeMapping<>(int[].class, Serializers.intArray()),
                new MultiTypeSerializer.TypeMapping<List<String>>((Class) List.class, Serializers.listOf(Serializers.string()))
        ));

        // when
        testee.serialize(encoder(), value);
        Object actual = testee.deserialize(decoder());

        // then
        assertThat(actual).isEqualTo(value);
    }

    private static Stream<Object> values() {
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
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Class class java.lang.Object not supported");
    }

    @Test
    void missingSerializer() {
        // given
        MultiTypeSerializer<Object> testee = new MultiTypeSerializer<>(List.of(
                new MultiTypeSerializer.TypeMapping<>(int[].class, Serializers.intArray())
        ));

        // when
        Throwable thrown = catchThrowable(() -> testee.serialize(encoder(), "test"));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Class class java.lang.String not supported");
    }

    @SuppressWarnings("unchecked")
    @Test
    void inheritance() throws IOException {
        // given
        MultiTypeSerializer<Object> testee = new MultiTypeSerializer<>(List.of(
                new MultiTypeSerializer.TypeMapping<>(Collection.class, new CollectionSerializer<>(Serializers.string(), HashSet::new))
        ));

        // when
        testee.serialize(encoder(), List.of("one", "two", "one"));
        Object actual = testee.deserialize(decoder());

        // then
        assertThat(actual.getClass()).isEqualTo(HashSet.class);
        assertThat((Set<String>) actual).containsExactlyInAnyOrder("one", "two");
    }
}