package com.github.pcimcioch.memorystore.encoder;

import com.github.pcimcioch.memorystore.store.ObjectPoolStore;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

// TODO javadocs
public class ObjectPoolEncoder<T> implements Encoder {

    private final ObjectPoolStore<T> store;
    private final UnsignedIntegerEncoder indexEncoder;

    public ObjectPoolEncoder(ObjectPoolStore<T> store, UnsignedIntegerEncoder indexEncoder) {
        this.store = requireNonNull(store, "Store cannot be null");
        this.indexEncoder = requireNonNull(indexEncoder, "Index Encoder cannot be null");
    }

    public void set(long index, T value) {
        int poolIndex = store.set(value);
        indexEncoder.set(index, poolIndex);
    }

    public T get(long index) {
        int poolIndex = indexEncoder.get(index);
        return store.get(poolIndex);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectPoolEncoder<?> that = (ObjectPoolEncoder<?>) o;
        return store.equals(that.store) && indexEncoder.equals(that.indexEncoder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(store, indexEncoder);
    }
}
