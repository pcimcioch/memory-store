package com.github.pcimcioch.memorystore.store;

import java.util.ArrayList;
import java.util.List;

public class ObjectPoolStore<T> {

    private final List<T> elements = new ArrayList<>();

    public int set(T value) {
        int index = elements.indexOf(value);
        if (index >= 0) {
            return index;
        }

        elements.add(value);
        return elements.size() - 1;
    }

    public T get(int index) {
        return elements.get(index);
    }

    public int elementsCount() {
        return elements.size();
    }
}
