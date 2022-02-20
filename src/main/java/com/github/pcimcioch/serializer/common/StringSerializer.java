package com.github.pcimcioch.serializer.common;

import com.github.pcimcioch.serializer.Serializer;
import com.github.pcimcioch.serializer.Serializers;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class StringSerializer implements Serializer<String> {

    @Override
    public void serialize(DataOutput encoder, String object) throws IOException {
        Serializers.byteArray().serialize(encoder, object == null ? null : object.getBytes(UTF_8));
    }

    @Override
    public String deserialize(DataInput decoder) throws IOException {
        byte[] chars = Serializers.byteArray().deserialize(decoder);
        return chars == null ? null : new String(chars, UTF_8);
    }
}
