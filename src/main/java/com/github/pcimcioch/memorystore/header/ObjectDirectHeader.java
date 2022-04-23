package com.github.pcimcioch.memorystore.header;

import com.github.pcimcioch.memorystore.encoder.ObjectDirectEncoder;

// TODO javadocs
public class ObjectDirectHeader<T> extends Header<ObjectDirectEncoder<T>> {

    public ObjectDirectHeader(String name) {
        super(name);
    }
}
