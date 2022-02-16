package com.github.pcimcioch.memorystore.persistence.serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

// TODO javadocs
public interface Serializer<T> {

    void serialize(DataOutput encoder, T object) throws IOException;

    T deserialize(DataInput decoder) throws IOException;
}
