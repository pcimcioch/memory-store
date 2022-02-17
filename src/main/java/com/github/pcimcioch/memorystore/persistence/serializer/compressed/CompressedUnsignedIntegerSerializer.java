package com.github.pcimcioch.memorystore.persistence.serializer.compressed;

import com.github.pcimcioch.memorystore.persistence.serializer.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

// TODO implement
// TODO tests
// TODO maybe implement compressed int array
public class CompressedUnsignedIntegerSerializer implements Serializer<Integer> {

    @Override
    public void serialize(DataOutput encoder, Integer object) throws IOException {

    }

    @Override
    public Integer deserialize(DataInput decoder) throws IOException {
        return null;
    }
}
