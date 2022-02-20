package com.github.pcimcioch.serializer.collection;

import com.github.pcimcioch.serializer.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class DoubleArraySerializer implements Serializer<double[]> {

    @Override
    public void serialize(DataOutput encoder, double[] array) throws IOException {
        if (array == null) {
            encoder.writeInt(-1);
        } else {
            encoder.writeInt(array.length);
            for (double element : array) {
                encoder.writeDouble(element);
            }
        }
    }

    @Override
    public double[] deserialize(DataInput decoder) throws IOException {
        int length = decoder.readInt();
        if (length == -1) {
            return null;
        }

        double[] array = new double[length];
        for (int i = 0; i < array.length; i++) {
            array[i] = decoder.readDouble();
        }
        return array;
    }
}
