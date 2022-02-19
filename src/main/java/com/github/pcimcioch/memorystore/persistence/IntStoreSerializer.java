package com.github.pcimcioch.memorystore.persistence;

import com.github.pcimcioch.memorystore.persistence.serializer.Serializer;
import com.github.pcimcioch.memorystore.store.IntStore;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

// TODO tests
class IntStoreSerializer implements Serializer<IntStore> {

    @Override
    public void serialize(DataOutput encoder, IntStore store) throws IOException {
        if (store == null) {
            encoder.writeLong(-1);
        } else {
            long size = store.size();
            encoder.writeLong(size);
            for (long i = 0; i < size; i++) {
                encoder.writeInt(store.getInt(i));
            }
        }
    }

    @Override
    public IntStore deserialize(DataInput decoder) throws IOException {
        long length = decoder.readLong();
        if (length == -1) {
            return null;
        }

        IntStore store = new IntStore();
        for (long i = 0; i < length; i++) {
            store.setInt(i, decoder.readInt());
        }

        return store;
    }
}
