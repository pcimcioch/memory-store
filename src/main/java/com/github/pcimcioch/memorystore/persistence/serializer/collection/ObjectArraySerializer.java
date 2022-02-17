package com.github.pcimcioch.memorystore.persistence.serializer.collection;

import com.github.pcimcioch.memorystore.persistence.serializer.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Array;

public class ObjectArraySerializer<T> implements Serializer<T[]> {

    private final Class<T> type;
    private final Serializer<T> elementSerializer;

    public ObjectArraySerializer(Class<T> type, Serializer<T> elementSerializer) {
        this.type = type;
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

        T[] objects = (T[]) Array.newInstance(type, length);;
        for (int i = 0; i < objects.length; i++) {
            objects[i] = elementSerializer.deserialize(decoder);
        }
        return objects;
    }
}
