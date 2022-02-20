package com.github.pcimcioch.serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Allows serializing and deserializing objects into binary form. Note that each implementation should handle null
 * values
 *
 * @param <T> type to serialize
 */
public interface Serializer<T> {

    /**
     * Serialize given object into the binary form
     *
     * @param encoder encoder used to output resulting bits
     * @param object object to serialize. Can be null
     * @throws IOException if encoder failed to consume data
     */
    void serialize(DataOutput encoder, T object) throws IOException;

    /**
     * Deserialize object from the binary form
     *
     * @param decoder decoder used to read bits of data
     * @return new object. Can be null
     * @throws IOException if decoder failed to provide data
     */
    T deserialize(DataInput decoder) throws IOException;
}
