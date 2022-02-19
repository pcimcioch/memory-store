package com.github.pcimcioch.memorystore.persistence;

import com.github.pcimcioch.memorystore.persistence.serializer.Serializer;
import com.github.pcimcioch.memorystore.store.ObjectPoolStore;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

// TODO tests
class ObjectPoolStoreSerializer<T> implements Serializer<ObjectPoolStore<T>> {

    private final Serializer<T> elementSerializer;

    ObjectPoolStoreSerializer(Serializer<T> elementSerializer) {
        this.elementSerializer = elementSerializer;
    }

    @Override
    public void serialize(DataOutput encoder, ObjectPoolStore<T> store) throws IOException {
        if (store == null) {
            encoder.writeInt(-1);
        } else {
            int size = store.size();
            encoder.writeInt(size);
            for (int i = 0; i < size; i++) {
                elementSerializer.serialize(encoder, store.get(i));
            }
        }
    }

    @Override
    public ObjectPoolStore<T> deserialize(DataInput decoder) throws IOException {
        int size = decoder.readInt();
        if (size == -1) {
            return null;
        }

        ObjectPoolStore<T> store = new ObjectPoolStore<>();
        for (int i = 0; i < size; i++) {
            store.set(elementSerializer.deserialize(decoder));
        }
        return store;
    }
}
