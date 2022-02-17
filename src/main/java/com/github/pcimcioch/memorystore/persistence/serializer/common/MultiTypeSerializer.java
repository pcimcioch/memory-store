package com.github.pcimcioch.memorystore.persistence.serializer.common;

import com.github.pcimcioch.memorystore.persistence.serializer.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// TODO tests
public class MultiTypeSerializer<T> implements Serializer<T> {

    private final List<TypeMapping<? extends T>> mappings;

    public static final class TypeMapping<K> {
        private final Class<K> type;
        private final Serializer<K> serializer;

        public TypeMapping(Class<K> type, Serializer<K> serializer) {
            this.type = type;
            this.serializer = serializer;
        }

        public boolean matches(Class<?> clazz) {
            return type.isAssignableFrom(clazz);
        }
    }

    public MultiTypeSerializer(List<TypeMapping<? extends T>> mappings) {
        if (mappings.size() > 255) {
            throw new IllegalArgumentException("Too many mappings. This serializer supports up to 255 mappings");
        }

        this.mappings = new ArrayList<>(mappings);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void serialize(DataOutput encoder, T object) throws IOException {
        if (object == null) {
            encoder.writeByte(0);
        } else {
            int index = getMappingIndex(object.getClass());
            if (index == -1) {
                throw new IllegalArgumentException("Class " + object.getClass() + " not supported");
            }
            encoder.writeByte(index + 1);

            TypeMapping mapping = mappings.get(index);
            mapping.serializer.serialize(encoder, object);
        }
    }

    @Override
    public T deserialize(DataInput decoder) throws IOException {
        int typeIndex = decoder.readUnsignedByte();
        if (typeIndex == 0) {
            return null;
        }

        TypeMapping<? extends T> mapping = mappings.get(typeIndex - 1);
        return mapping.serializer.deserialize(decoder);
    }

    private int getMappingIndex(Class<?> type) {
        for (int i = 0; i < mappings.size(); i++) {
            if (mappings.get(i).matches(type)) {
                return i;
            }
        }

        return -1;
    }
}
