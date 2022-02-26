package com.github.pcimcioch.memorystore.persistence.binary;

import com.github.pcimcioch.memorystore.SerializerTestBase;
import com.github.pcimcioch.memorystore.encoder.BitEncoder.Config;
import com.github.pcimcioch.memorystore.encoder.BooleanEncoder;
import com.github.pcimcioch.memorystore.encoder.CharEncoder;
import com.github.pcimcioch.memorystore.encoder.IntEncoder;
import com.github.pcimcioch.memorystore.encoder.ObjectDirectEncoder;
import com.github.pcimcioch.memorystore.layout.MemoryLayoutBuilder.MemoryLayout;
import com.github.pcimcioch.memorystore.layout.MemoryLayoutBuilder.MemoryPosition;
import com.github.pcimcioch.memorystore.store.IntStore;
import com.github.pcimcioch.memorystore.store.ObjectStore;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.pcimcioch.memorystore.header.Headers.bool;
import static com.github.pcimcioch.memorystore.header.Headers.char16;
import static com.github.pcimcioch.memorystore.header.Headers.int32;
import static com.github.pcimcioch.memorystore.header.Headers.object;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class LoaderMemoryLayoutTest extends SerializerTestBase {

    private final IntStore intStore = new IntStore();
    private final ObjectStore<String> objectStore = new ObjectStore<>();

    @Test
    void noHeaders() {
        // given
        LoaderMemoryLayout testee = new LoaderMemoryLayout(32, emptyMap());

        // when
        MemoryLayout memoryLayout = testee.compute(32, emptyList());

        // then
        assertThat(memoryLayout).isEqualTo(new MemoryLayout(0, emptyMap()));
    }

    @Test
    void incorrectWordSize() {
        // given
        LoaderMemoryLayout testee = new LoaderMemoryLayout(16, emptyMap());

        // when
        Throwable thrown = catchThrowable(() -> testee.compute(15, emptyList()));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("This memory layout supports 16 word size, but 15 requested");
    }

    @Test
    void bitHeaders() {
        // given
        LoaderMemoryLayout testee = new LoaderMemoryLayout(32, Map.of(
                int32("header1"), new IntEncoder(new Config(intStore, 5, 0, 0, 32)),
                bool("header2"), new BooleanEncoder(new Config(intStore, 5, 3, 2, 1)),
                char16("header3"), new CharEncoder(new Config(intStore, 5, 1, 5, 16))
        ));

        // when
        MemoryLayout memoryLayout = testee.compute(32, List.of(int32("header1"), bool("header2"), char16("header3")));

        // then
        assertThat(memoryLayout).isEqualTo(new MemoryLayout(5, Map.of(
                int32("header1"), new MemoryPosition(0, 0),
                bool("header2"), new MemoryPosition(3, 2),
                char16("header3"), new MemoryPosition(1, 5)
        )));
    }

    @Test
    void onlyBitHeaders() {
        // given
        LoaderMemoryLayout testee = new LoaderMemoryLayout(32, Map.of(
                int32("header1"), new IntEncoder(new Config(intStore, 5, 0, 0, 32)),
                bool("header2"), new BooleanEncoder(new Config(intStore, 5, 3, 2, 1)),
                object("header3"), new ObjectDirectEncoder<>(objectStore)
        ));

        // when
        MemoryLayout memoryLayout = testee.compute(32, List.of(int32("header1"), bool("header2"), char16("header3")));

        // then
        assertThat(memoryLayout).isEqualTo(new MemoryLayout(5, map(
                int32("header1"), new MemoryPosition(0, 0),
                bool("header2"), new MemoryPosition(3, 2),
                char16("header3"), null
        )));
    }

    @Test
    void serializeLayout() throws IOException {
        // given
        LoaderMemoryLayout layout = new LoaderMemoryLayout(32, Map.of(
                int32("header1"), new IntEncoder(new Config(intStore, 5, 0, 0, 32)),
                bool("header2"), new BooleanEncoder(new Config(intStore, 5, 3, 2, 1)),
                object("header3"), new ObjectDirectEncoder<>(objectStore)
        ));
        LoaderMemoryLayout.SERIALIZER.serialize(encoder(), layout);
        LoaderMemoryLayout testee = LoaderMemoryLayout.SERIALIZER.deserialize(decoder());

        // when
        MemoryLayout memoryLayout = testee.compute(32, List.of(int32("header1"), bool("header2")));

        // then
        assertThat(memoryLayout).isEqualTo(new MemoryLayout(5, Map.of(
                int32("header1"), new MemoryPosition(0, 0),
                bool("header2"), new MemoryPosition(3, 2)
        )));
    }

    @Test
    void serializeNull() throws IOException {
        // when
        LoaderMemoryLayout.SERIALIZER.serialize(encoder(), null);
        LoaderMemoryLayout testee = LoaderMemoryLayout.SERIALIZER.deserialize(decoder());

        // then
        assertThat(testee).isNull();
    }

    private <K, V> Map<K, V> map(K key1, V value1, K key2, V value2, K key3, V value3) {
        Map<K, V> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);

        return map;
    }
}