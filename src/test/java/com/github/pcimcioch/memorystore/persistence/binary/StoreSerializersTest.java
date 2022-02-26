package com.github.pcimcioch.memorystore.persistence.binary;

import com.github.pcimcioch.memorystore.SerializerTestBase;
import com.github.pcimcioch.memorystore.store.IntStore;
import com.github.pcimcioch.memorystore.store.ObjectPoolStore;
import com.github.pcimcioch.memorystore.store.ObjectStore;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.github.pcimcioch.memorystore.persistence.binary.StoreSerializers.intStore;
import static com.github.pcimcioch.memorystore.persistence.binary.StoreSerializers.objectPoolStore;
import static com.github.pcimcioch.memorystore.persistence.binary.StoreSerializers.objectStore;
import static com.github.pcimcioch.serializer.Serializers.string;
import static org.assertj.core.api.Assertions.assertThat;

class StoreSerializersTest extends SerializerTestBase {

    @Test
    void nullIntStore() throws IOException {
        // given
        intStore().serialize(encoder(), null);

        // when
        IntStore store = intStore().deserialize(decoder());

        // then
        assertThat(store).isNull();
    }

    @Test
    void wholeIntStore() throws IOException {
        // given
        IntStore store = new IntStore(1024);
        store.setInt(0, 10);
        store.setInt(1, 20);
        store.setInt(3, 30);

        intStore().serialize(encoder(), store);

        // when
        IntStore deserialized = intStore().deserialize(decoder());

        // then
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.size()).isEqualTo(1024);
        assertThat(deserialized.getInt(0)).isEqualTo(10);
        assertThat(deserialized.getInt(1)).isEqualTo(20);
        assertThat(deserialized.getInt(2)).isEqualTo(0);
        assertThat(deserialized.getInt(3)).isEqualTo(30);
        assertThat(deserialized.getInt(1023)).isEqualTo(0);
    }

    @Test
    void intStoreMultipleBlocks() throws IOException {
        // given
        IntStore store = new IntStore(1024);
        store.setInt(0, 10);
        store.setInt(1, 20);
        store.setInt(2000, 30);

        intStore().serialize(encoder(), store);

        // when
        IntStore deserialized = intStore().deserialize(decoder());

        // then
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.size()).isEqualTo(2048);
        assertThat(deserialized.getInt(0)).isEqualTo(10);
        assertThat(deserialized.getInt(1)).isEqualTo(20);
        assertThat(deserialized.getInt(2)).isEqualTo(0);
        assertThat(deserialized.getInt(2000)).isEqualTo(30);
        assertThat(deserialized.getInt(2047)).isEqualTo(0);
    }

    @Test
    void intStoreSaveEmptyBlock() throws IOException {
        // given
        IntStore store = new IntStore(1024);
        store.setInt(0, 10);
        store.setInt(1, 20);
        store.setInt(2000, 30);
        store.setInt(2000, 0);

        intStore().serialize(encoder(), store);

        // when
        IntStore deserialized = intStore().deserialize(decoder());

        // then
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.size()).isEqualTo(2048);
        assertThat(deserialized.getInt(0)).isEqualTo(10);
        assertThat(deserialized.getInt(1)).isEqualTo(20);
        assertThat(deserialized.getInt(2)).isEqualTo(0);
        assertThat(deserialized.getInt(2000)).isEqualTo(0);
        assertThat(deserialized.getInt(2047)).isEqualTo(0);
    }

    @Test
    void nullObjectStore() throws IOException {
        // given
        objectStore(string()).serialize(encoder(), null);

        // when
        IntStore store = intStore().deserialize(decoder());

        // then
        assertThat(store).isNull();
    }

    @Test
    void wholeObjectStore() throws IOException {
        // given
        ObjectStore<String> store = new ObjectStore<>(1024);
        store.set(0, "first");
        store.set(1, "second");
        store.set(3, "third");

        objectStore(string()).serialize(encoder(), store);

        // when
        ObjectStore<String> deserialized = objectStore(string()).deserialize(decoder());

        // then
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.size()).isEqualTo(1024);
        assertThat(deserialized.get(0)).isEqualTo("first");
        assertThat(deserialized.get(1)).isEqualTo("second");
        assertThat(deserialized.get(2)).isNull();
        assertThat(deserialized.get(3)).isEqualTo("third");
        assertThat(deserialized.get(1023)).isNull();
    }

    @Test
    void objectStoreMultipleBlocks() throws IOException {
        // given
        ObjectStore<String> store = new ObjectStore<>(1024);
        store.set(0, "first");
        store.set(1, "second");
        store.set(2000, "third");

        objectStore(string()).serialize(encoder(), store);

        // when
        ObjectStore<String> deserialized = objectStore(string()).deserialize(decoder());

        // then
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.size()).isEqualTo(2048);
        assertThat(deserialized.get(0)).isEqualTo("first");
        assertThat(deserialized.get(1)).isEqualTo("second");
        assertThat(deserialized.get(2)).isNull();
        assertThat(deserialized.get(2000)).isEqualTo("third");
        assertThat(deserialized.get(2047)).isNull();
    }

    @Test
    void objectStoreSaveEmptyBlock() throws IOException {
        // given
        ObjectStore<String> store = new ObjectStore<>(1024);
        store.set(0, "first");
        store.set(1, "second");
        store.set(2000, "third");
        store.set(2000, null);

        objectStore(string()).serialize(encoder(), store);

        // when
        ObjectStore<String> deserialized = objectStore(string()).deserialize(decoder());

        // then
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.size()).isEqualTo(2048);
        assertThat(deserialized.get(0)).isEqualTo("first");
        assertThat(deserialized.get(1)).isEqualTo("second");
        assertThat(deserialized.get(2)).isNull();
        assertThat(deserialized.get(2000)).isNull();
        assertThat(deserialized.get(2047)).isNull();
    }

    @Test
    void nullPoolStore() throws IOException {
        // given
        objectPoolStore(string()).serialize(encoder(), null);

        // when
        ObjectPoolStore<String> store = objectPoolStore(string()).deserialize(decoder());

        // then
        assertThat(store).isNull();
    }

    @Test
    void wholePoolStore() throws IOException {
        // given
        ObjectPoolStore<String> store = new ObjectPoolStore<>();
        store.set("first");
        store.set("second");
        store.set("third");

        objectPoolStore(string()).serialize(encoder(), store);

        // when
        ObjectPoolStore<String> deserialized = objectPoolStore(string()).deserialize(decoder());

        // then
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.size()).isEqualTo(3);
        assertThat(deserialized.get(0)).isEqualTo("first");
        assertThat(deserialized.get(1)).isEqualTo("second");
        assertThat(deserialized.get(2)).isEqualTo("third");
    }
}