package com.github.pcimcioch.memorystore.persistence.serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class LongArraySerializer implements Serializer<long[]> {

    @Override
    public void serialize(DataOutput encoder, long[] array) throws IOException {
        if (array == null) {
            encoder.writeInt(-1);
        } else {
            encoder.writeInt(array.length);
            for (long element : array) {
                encoder.writeLong(element);
            }
        }
    }

    @Override
    public long[] deserialize(DataInput decoder) throws IOException {
        int length = decoder.readInt();
        if (length == -1) {
            return null;
        }

        long[] array = new long[length];
        for (int i = 0; i < array.length; i++) {
            array[i] = decoder.readLong();
        }
        return array;
    }
}
