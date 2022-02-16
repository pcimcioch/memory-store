package com.github.pcimcioch.memorystore.persistence.serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class FloatArraySerializer implements Serializer<float[]> {

    @Override
    public void serialize(DataOutput encoder, float[] array) throws IOException {
        if (array == null) {
            encoder.writeInt(-1);
        } else {
            encoder.writeInt(array.length);
            for (float element : array) {
                encoder.writeFloat(element);
            }
        }
    }

    @Override
    public float[] deserialize(DataInput decoder) throws IOException {
        int length = decoder.readInt();
        if (length == -1) {
            return null;
        }

        float[] array = new float[length];
        for (int i = 0; i < array.length; i++) {
            array[i] = decoder.readFloat();
        }
        return array;
    }
}
