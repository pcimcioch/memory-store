package com.github.pcimcioch.memorystore.persistence;

import com.github.pcimcioch.memorystore.persistence.serializer.Serializer;
import com.github.pcimcioch.memorystore.store.ObjectStore;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

// TODO tests
class ObjectStoreSerializer<T> implements Serializer<ObjectStore<T>> {

    private final Serializer<T> elementSerializer;

    ObjectStoreSerializer(Serializer<T> elementSerializer) {
        this.elementSerializer = elementSerializer;
    }

    @Override
    public void serialize(DataOutput encoder, ObjectStore<T> store) throws IOException {
        if (store == null) {
            encoder.writeLong(-1);
        } else {
            long size = store.size();
            encoder.writeLong(size);
            for (long i = 0; i < size; i++) {
                elementSerializer.serialize(encoder, store.get(i));
            }
        }
    }

    @Override
    public ObjectStore<T> deserialize(DataInput decoder) throws IOException {
        long size = decoder.readLong();
        if (size == -1) {
            return null;
        }

        ObjectStore<T> store = new ObjectStore<>();
        for (long i = 0; i < size; i++) {
            store.set(i, elementSerializer.deserialize(decoder));
        }
        return store;
    }
}
