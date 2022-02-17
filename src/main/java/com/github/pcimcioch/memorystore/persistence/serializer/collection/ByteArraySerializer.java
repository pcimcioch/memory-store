package com.github.pcimcioch.memorystore.persistence.serializer.collection;

import com.github.pcimcioch.memorystore.persistence.serializer.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ByteArraySerializer implements Serializer<byte[]> {

    @Override
    public void serialize(DataOutput encoder, byte[] array) throws IOException {
        if (array == null) {
            encoder.writeInt(-1);
        } else {
            encoder.writeInt(array.length);
            encoder.write(array);
        }
    }

    @Override
    public byte[] deserialize(DataInput decoder) throws IOException {
        int length = decoder.readInt();
        if (length == -1) {
            return null;
        }

        byte[] array = new byte[length];
        decoder.readFully(array);
        return array;
    }
}
