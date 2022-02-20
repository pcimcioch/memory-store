package com.github.pcimcioch.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;

public abstract class SerializerTestBase {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    protected DataOutput encoder() {
        return new DataOutputStream(buffer);
    }

    protected DataInput decoder() {
        return new DataInputStream(new ByteArrayInputStream(buffer.toByteArray()));
    }
}
