package com.github.pcimcioch.memorystore.persistence;

import com.github.pcimcioch.memorystore.header.ObjectDirectHeader;
import com.github.pcimcioch.memorystore.header.ObjectPoolHeader.PoolDefinition;
import com.github.pcimcioch.memorystore.store.IntStore;
import com.github.pcimcioch.memorystore.store.ObjectPoolStore;
import com.github.pcimcioch.memorystore.store.ObjectStore;
import com.github.pcimcioch.memorystore.store.StoreFactory;

import java.util.Map;

@SuppressWarnings("unchecked")
class LoaderStoreFactory implements StoreFactory {

    private final IntStore intStore;
    private final Map<String, ObjectStore<?>> objectStores;
    private final Map<String, ObjectPoolStore<?>> poolStores;

    LoaderStoreFactory(IntStore intStore,
                       Map<String, ObjectStore<?>> objectStores,
                       Map<String, ObjectPoolStore<?>> poolStores) {
        this.intStore = intStore;
        this.objectStores = objectStores;
        this.poolStores = poolStores;
    }

    @Override
    public IntStore buildIntStore() {
        return intStore;
    }

    @Override
    public <T> ObjectStore<T> buildObjectStore(ObjectDirectHeader<T> header) {
        return (ObjectStore<T>) objectStores.get(header.name());
    }

    @Override
    public <T> ObjectPoolStore<T> buildObjectPoolStore(PoolDefinition poolDefinition) {
        return (ObjectPoolStore<T>) poolStores.get(poolDefinition.name());
    }
}
