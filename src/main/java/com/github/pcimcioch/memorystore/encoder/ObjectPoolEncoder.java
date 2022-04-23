package com.github.pcimcioch.memorystore.encoder;

import com.github.pcimcioch.memorystore.store.ObjectPoolStore;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * ObjectPoolEncoder Stores data in a pool of java objects, where the same java objects are stored only once in the memory
 *
 * @param <T> java object type encoded by this encoder
 */
public class ObjectPoolEncoder<T> implements Encoder {

    private final ObjectPoolStore<T> store;
    private final UnsignedIntegerEncoder indexEncoder;

    /**
     * Constructor
     *
     * @param store        structure to store java objects
     * @param indexEncoder encoder that is used to store object index
     */
    public ObjectPoolEncoder(ObjectPoolStore<T> store, UnsignedIntegerEncoder indexEncoder) {
        this.store = requireNonNull(store, "Store cannot be null");
        this.indexEncoder = requireNonNull(indexEncoder, "Index Encoder cannot be null");
    }

    /**
     * Sets given object under given index
     *
     * @param index the index
     * @param value object to store
     */
    public void set(long index, T value) {
        int poolIndex = store.set(value);
        indexEncoder.set(index, poolIndex);
    }

    /**
     * Returns object from given index
     *
     * @param index index of the object
     * @return object under given index
     */
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
