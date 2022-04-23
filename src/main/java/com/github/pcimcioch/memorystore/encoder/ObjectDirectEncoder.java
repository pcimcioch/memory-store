package com.github.pcimcioch.memorystore.encoder;

import com.github.pcimcioch.memorystore.store.ObjectStore;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

// TODO javadocs
public class ObjectDirectEncoder<T> implements Encoder {

    private final ObjectStore<T> store;

    public ObjectDirectEncoder(ObjectStore<T> store) {
        this.store = requireNonNull(store, "Store cannot be null");
    }

    public void set(long index, T value) {
        store.set(index, value);
    }

    public T get(long index) {
        return store.get(index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectDirectEncoder<?> that = (ObjectDirectEncoder<?>) o;
        return store.equals(that.store);
    }

    @Override
    public int hashCode() {
        return Objects.hash(store);
    }
}
