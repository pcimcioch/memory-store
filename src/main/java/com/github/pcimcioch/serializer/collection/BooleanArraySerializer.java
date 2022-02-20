package com.github.pcimcioch.serializer.collection;

import com.github.pcimcioch.serializer.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class BooleanArraySerializer implements Serializer<boolean[]> {

    @Override
    public void serialize(DataOutput encoder, boolean[] array) throws IOException {
        if (array == null) {
            encoder.writeInt(-1);
        } else {
            encoder.writeInt(array.length);
            for (boolean element : array) {
                encoder.writeBoolean(element);
            }
        }
    }

    @Override
    public boolean[] deserialize(DataInput decoder) throws IOException {
        int length = decoder.readInt();
        if (length == -1) {
            return null;
        }

        boolean[] array = new boolean[length];
        for (int i = 0; i < array.length; i++) {
            array[i] = decoder.readBoolean();
        }
        return array;
    }
}
