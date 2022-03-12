package com.github.pcimcioch.memorystore.persistence.binary;

import com.github.pcimcioch.memorystore.SerializerTestBase;
import com.github.pcimcioch.memorystore.header.Header;
import com.github.pcimcioch.memorystore.persistence.binary.HeaderDefinition.BitHeaderDefinition;
import com.github.pcimcioch.memorystore.persistence.binary.HeaderDefinition.ObjectDirectHeaderDefinition;
import com.github.pcimcioch.memorystore.persistence.binary.HeaderDefinition.ObjectPoolHeaderDefinition;
import com.github.pcimcioch.serializer.Serializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.github.pcimcioch.memorystore.header.Headers.bool;
import static com.github.pcimcioch.memorystore.header.Headers.int32;
import static com.github.pcimcioch.memorystore.header.Headers.long64;
import static com.github.pcimcioch.memorystore.header.Headers.object;
import static com.github.pcimcioch.memorystore.header.Headers.objectPool;
import static com.github.pcimcioch.memorystore.header.Headers.poolOnBits;
import static com.github.pcimcioch.memorystore.header.Headers.unsignedIntOnBits;
import static com.github.pcimcioch.serializer.Serializers.listOf;
import static org.assertj.core.api.Assertions.assertThat;

class HeaderDefinitionTest extends SerializerTestBase {

    @ParameterizedTest
    @MethodSource("headers")
    void headerBuilding(Header<?> header, Set<HeaderDefinition> expected) {
        // when
        Set<HeaderDefinition> actual = HeaderDefinition.from(List.of(header));

        // then
        assertThat(actual).isEqualTo(expected);
    }

    private static Stream<Arguments> headers() {
        return Stream.of(
                Arguments.of(int32("test"), Set.of(new BitHeaderDefinition("test", 32, 32))),
                Arguments.of(long64("test"), Set.of(new BitHeaderDefinition("test", 64, 64))),
                Arguments.of(bool("test"), Set.of(new BitHeaderDefinition("test", 1, 32))),
                Arguments.of(unsignedIntOnBits("test", 7), Set.of(new BitHeaderDefinition("test", 7, 32))),
                Arguments.of(object("test"), Set.of(new ObjectDirectHeaderDefinition("test"))),
                Arguments.of(objectPool("test", poolOnBits("pool", 5)), Set.of(new ObjectPoolHeaderDefinition("test", "pool", 5), new BitHeaderDefinition("test-index", 5, 32)))
        );
    }

    @Test
    void emptyHeaders() {
        // when
        Set<HeaderDefinition> actual = HeaderDefinition.from(Collections.emptyList());

        // then
        assertThat(actual).isEmpty();
    }

    @Test
    void multipleHeaders() {
        // given
        List<Header<?>> headers = List.of(
                int32("header1"),
                object("header2"),
                objectPool("header3", poolOnBits("pool", 12))
        );
        Set<HeaderDefinition> expected = Set.of(
                new BitHeaderDefinition("header1", 32, 32),
                new ObjectDirectHeaderDefinition("header2"),
                new ObjectPoolHeaderDefinition("header3", "pool", 12),
                new BitHeaderDefinition("header3-index", 12, 32)
        );

        // when
        Set<HeaderDefinition> actual = HeaderDefinition.from(headers);

        // then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void serialization() throws IOException {
        // given
        List<HeaderDefinition> definitions = Arrays.asList(
                new BitHeaderDefinition("header1", 32, 32),
                new BitHeaderDefinition("header2", 25, 64),
                null,
                new ObjectPoolHeaderDefinition("header3", "pool", 31),
                new ObjectDirectHeaderDefinition("header4")
        );
        Serializer<List<HeaderDefinition>> serializer = listOf(HeaderDefinition.SERIALIZER);

        // when
        serializer.serialize(encoder(), definitions);
        List<HeaderDefinition> actual = serializer.deserialize(decoder());

        // then
        assertThat(actual).isEqualTo(definitions);
    }
}