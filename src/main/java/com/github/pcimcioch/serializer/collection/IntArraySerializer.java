package com.github.pcimcioch.serializer.collection;

import com.github.pcimcioch.serializer.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class IntArraySerializer implements Serializer<int[]> {

    @Override
    public void serialize(DataOutput encoder, int[] array) throws IOException {
        if (array == null) {
            encoder.writeInt(-1);
        } else {
            encoder.writeInt(array.length);
            for (int element : array) {
                encoder.writeInt(element);
            }
        }
    }

    @Override
    public int[] deserialize(DataInput decoder) throws IOException {
        int length = decoder.readInt();
        if (length == -1) {
            return null;
        }

        int[] array = new int[length];
        for (int i = 0; i < array.length; i++) {
            array[i] = decoder.readInt();
        }
        return array;
    }
}
