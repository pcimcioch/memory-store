package com.github.pcimcioch.memorystore.store;

import com.github.pcimcioch.memorystore.header.ObjectDirectHeader;
import com.github.pcimcioch.memorystore.header.ObjectPoolHeader;
import com.github.pcimcioch.memorystore.header.ObjectPoolHeader.PoolDefinition;

public interface StoreFactory {

    IntStore buildIntStore();

    <T> ObjectStore<T> buildObjectStore(ObjectDirectHeader<T> header);

    <T> ObjectPoolStore<T> buildObjectPoolStore(PoolDefinition poolDefinition);
}
