package com.github.pcimcioch.memorystore.persistence.serializer.collection;

import com.github.pcimcioch.memorystore.persistence.serializer.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Supplier;

public class CollectionSerializer<T, K extends Collection<T>> implements Serializer<K> {

    private final Serializer<T> elementSerializer;
    private final Supplier<? extends K> collectionFactory;

    public CollectionSerializer(Serializer<T> elementSerializer, Supplier<? extends K> collectionFactory) {
        this.elementSerializer = elementSerializer;
        this.collectionFactory = collectionFactory;
    }

    @Override
    public void serialize(DataOutput encoder, K collection) throws IOException {
        if (collection == null) {
            encoder.writeInt(-1);
        } else {
            encoder.writeInt(collection.size());
            for (T element : collection) {
                elementSerializer.serialize(encoder, element);
            }
        }
    }

    @Override
    public K deserialize(DataInput decoder) throws IOException {
        int length = decoder.readInt();
        if (length == -1) {
            return null;
        }

        K collection = collectionFactory.get();
        for (int i = 0; i < length; i++) {
            collection.add(elementSerializer.deserialize(decoder));
        }
        return collection;
    }
}
