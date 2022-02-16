package com.github.pcimcioch.memorystore.persistence.serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

// TODO tests
public class MapSerializer<K, V> implements Serializer<Map<K, V>> {

    private final Serializer<K> keySerializer;
    private final Serializer<V> valueSerializer;
    private final Supplier<? extends Map<K, V>> mapFactory;

    public MapSerializer(Serializer<K> keySerializer,
                         Serializer<V> valueSerializer,
                         Supplier<? extends Map<K, V>> mapFactory) {
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
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
                valueSerializer.serialize(encoder, entry.getValue());
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
            map.put(
                    keySerializer.deserialize(decoder),
                    valueSerializer.deserialize(decoder)
            );
        }
        return map;
    }
}
