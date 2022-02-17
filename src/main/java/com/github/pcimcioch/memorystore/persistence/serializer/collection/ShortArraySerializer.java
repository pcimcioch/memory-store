package com.github.pcimcioch.memorystore.persistence.serializer.collection;

import com.github.pcimcioch.memorystore.persistence.serializer.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ShortArraySerializer implements Serializer<short[]> {

    @Override
    public void serialize(DataOutput encoder, short[] array) throws IOException {
        if (array == null) {
            encoder.writeInt(-1);
        } else {
            encoder.writeInt(array.length);
            for (short element : array) {
                encoder.writeShort(element);
            }
        }
    }

    @Override
    public short[] deserialize(DataInput decoder) throws IOException {
        int length = decoder.readInt();
        if (length == -1) {
            return null;
        }

        short[] array = new short[length];
        for (int i = 0; i < array.length; i++) {
            array[i] = decoder.readShort();
        }
        return array;
    }
}
