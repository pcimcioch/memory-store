package com.github.pcimcioch.memorystore.store;

import com.github.pcimcioch.memorystore.header.ObjectDirectHeader;
import com.github.pcimcioch.memorystore.header.ObjectPoolHeader;

public class DefaultStoreFactory implements StoreFactory {

    @Override
    public IntStore buildIntStore() {
        return new IntStore();
    }

    @Override
    public <T> ObjectStore<T> buildObjectStore(ObjectDirectHeader<T> header) {
        return new ObjectStore<>();
    }

    @Override
    public <T> ObjectPoolStore<T> buildObjectPoolStore(ObjectPoolHeader.PoolDefinition poolDefinition) {
        return new ObjectPoolStore<>();
    }
}
