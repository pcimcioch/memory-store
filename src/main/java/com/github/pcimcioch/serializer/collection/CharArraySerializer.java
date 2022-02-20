package com.github.pcimcioch.serializer.collection;

import com.github.pcimcioch.serializer.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class CharArraySerializer implements Serializer<char[]> {

    @Override
    public void serialize(DataOutput encoder, char[] array) throws IOException {
        if (array == null) {
            encoder.writeInt(-1);
        } else {
            encoder.writeInt(array.length);
            for (char element : array) {
                encoder.writeChar(element);
            }
        }
    }

    @Override
    public char[] deserialize(DataInput decoder) throws IOException {
        int length = decoder.readInt();
        if (length == -1) {
            return null;
        }

        char[] array = new char[length];
        for (int i = 0; i < array.length; i++) {
            array[i] = decoder.readChar();
        }
        return array;
    }
}
