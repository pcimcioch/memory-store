package com.github.pcimcioch.memorystore.persistence.serializer.common;

import com.github.pcimcioch.memorystore.persistence.serializer.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.pcimcioch.memorystore.util.Utils.assertArgument;

public class MapSerializer<K, V> implements Serializer<Map<K, V>> {

    private final Serializer<K> keySerializer;
    private final Function<K, Serializer<V>> valueSerializerProvider;
    private final Supplier<? extends Map<K, V>> mapFactory;

    public MapSerializer(Serializer<K> keySerializer,
                         Serializer<V> valueSerializer,
                         Supplier<? extends Map<K, V>> mapFactory) {
        this(keySerializer, k -> valueSerializer, mapFactory);
    }

    // TODO tests
    public MapSerializer(Serializer<K> keySerializer,
                         Function<K, Serializer<V>> valueSerializerProvider,
                         Supplier<? extends Map<K, V>> mapFactory) {
        this.keySerializer = keySerializer;
        this.valueSerializerProvider = valueSerializerProvider;
        this.mapFactory = mapFactory;
    }

    @Override
    public void serialize(DataOutput encoder, Map<K, V> map) throws IOException {
        if (map == null) {
            encoder.writeInt(-1);
        } else {
            encoder.writeInt(map.size());
            for (Map.Entry<K, V> entry : map.entrySet()) {
                keySerializer.serialize(encoder, entry.getKey());
                valueSerializer(entry.getKey()).serialize(encoder, entry.getValue());
            }
        }
    }

    @Override
    public Map<K, V> deserialize(DataInput decoder) throws IOException {
        int length = decoder.readInt();
        if (length == -1) {
            return null;
        }

        Map<K, V> map = mapFactory.get();
        for (int i = 0; i < length; i++) {
            K key = keySerializer.deserialize(decoder);
            V value = valueSerializer(key).deserialize(decoder);
            map.put(key, value);
        }
        return map;
    }

    private Serializer<V> valueSerializer(K key) {
        Serializer<V> serializer = valueSerializerProvider.apply(key);
        assertArgument(serializer != null, "Missing value serializer for key %s", key);

        return serializer;
    }
}
