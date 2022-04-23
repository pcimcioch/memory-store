package com.github.pcimcioch.memorystore.header;

import com.github.pcimcioch.memorystore.encoder.ObjectDirectEncoder;

/**
 * ObjectDirectHeaders represent data that is stored in memory as individual java objects
 *
 * @param <T> ObjectDirectHeaders are supported by ObjectDirectEncoder
 */
public class ObjectDirectHeader<T> extends Header<ObjectDirectEncoder<T>> {

    /**
     * Constructor
     *
     * @param name header name
     */
    public ObjectDirectHeader(String name) {
        super(name);
    }
}
