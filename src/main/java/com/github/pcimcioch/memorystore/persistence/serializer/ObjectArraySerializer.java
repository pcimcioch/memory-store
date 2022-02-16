package com.github.pcimcioch.memorystore.persistence.serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

// TODO tests
public class ObjectArraySerializer<T> implements Serializer<T[]> {

    private final Serializer<T> elementSerializer;

    public ObjectArraySerializer(Serializer<T> elementSerializer) {
        this.elementSerializer = elementSerializer;
    }

    @Override
    public void serialize(DataOutput encoder, T[] array) throws IOException {
        if (array == null) {
            encoder.writeInt(-1);
        } else {
            encoder.writeInt(array.length);
            for (T element : array) {
                elementSerializer.serialize(encoder, element);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T[] deserialize(DataInput decoder) throws IOException {
        int length = decoder.readInt();
        if (length == -1) {
            return null;
        }

        T[] objects = (T[]) new Object[length];
        for (int i = 0; i < objects.length; i++) {
            objects[i] = elementSerializer.deserialize(decoder);
        }
        return objects;
    }
}
