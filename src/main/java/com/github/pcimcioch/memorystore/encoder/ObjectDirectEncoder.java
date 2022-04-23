package com.github.pcimcioch.memorystore.encoder;

import com.github.pcimcioch.memorystore.store.ObjectStore;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * ObjectDirectEncoder stores data directly as a java object
 *
 * @param <T> java object type encoded by this encoder
 */
public class ObjectDirectEncoder<T> implements Encoder {

    private final ObjectStore<T> store;

    /**
     * Constructor
     *
     * @param store structure to store java objects
     */
    public ObjectDirectEncoder(ObjectStore<T> store) {
        this.store = requireNonNull(store, "Store cannot be null");
    }

    /**
     * Sets given object under given index
     *
     * @param index the index
     * @param value object to store
     */
    public void set(long index, T value) {
        store.set(index, value);
    }

    /**
     * Returns object from given index
     *
     * @param index index of the object
     * @return object under given index
     */
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
