package com.github.pcimcioch.memorystore.persistence.binary;

import com.github.pcimcioch.memorystore.store.IntStore;
import com.github.pcimcioch.memorystore.store.ObjectPoolStore;
import com.github.pcimcioch.memorystore.store.ObjectStore;
import com.github.pcimcioch.serializer.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

// TODO add tests
class StoreSerializers {

    private static final IntStoreSerializer INT_STORE_SERIALIZER = new IntStoreSerializer();

    private StoreSerializers() {
    }

    static IntStoreSerializer intStore() {
        return INT_STORE_SERIALIZER;
    }

    static <T> ObjectStoreSerializer<T> objectStore(Serializer<T> elementSerializer) {
        return new ObjectStoreSerializer<>(elementSerializer);
    }

    static <T> ObjectPoolStoreSerializer<T> objectPoolStore(Serializer<T> elementSerializer) {
        return new ObjectPoolStoreSerializer<>(elementSerializer);
    }

    static final class IntStoreSerializer implements Serializer<IntStore> {

        @Override
        public void serialize(DataOutput encoder, IntStore store) throws IOException {
            if (store == null) {
                encoder.writeLong(-1);
            } else {
                long size = store.size();
                long effectiveSize = effectiveSize(store);

                encoder.writeLong(size);
                encoder.writeLong(effectiveSize);
                for (long i = 0; i < effectiveSize; i++) {
                    encoder.writeInt(store.getInt(i));
                }
            }
        }

        @Override
        public IntStore deserialize(DataInput decoder) throws IOException {
            long size = decoder.readLong();
            if (size == -1) {
                return null;
            }
            long effectiveSize = decoder.readLong();

            IntStore store = new IntStore();
            if (size > 0) {
                store.setInt(size - 1, 0);
            }
            for (long i = 0; i < effectiveSize; i++) {
                store.setInt(i, decoder.readInt());
            }

            return store;
        }

        private long effectiveSize(IntStore store) {
            for (long lastIndex = store.size() - 1; lastIndex >= 0; lastIndex--) {
                if (store.getInt(lastIndex) != 0) {
                    return lastIndex + 1;
                }
            }

            return 0;
        }
    }

    static final class ObjectStoreSerializer<T> implements Serializer<ObjectStore<T>> {

        private final Serializer<T> elementSerializer;

        ObjectStoreSerializer(Serializer<T> elementSerializer) {
            this.elementSerializer = elementSerializer;
        }

        @Override
        public void serialize(DataOutput encoder, ObjectStore<T> store) throws IOException {
            if (store == null) {
                encoder.writeLong(-1);
            } else {
                long size = store.size();
                long effectiveSize = effectiveSize(store);

                encoder.writeLong(size);
                encoder.writeLong(effectiveSize);
                for (long i = 0; i < effectiveSize; i++) {
                    elementSerializer.serialize(encoder, store.get(i));
                }
            }
        }

        @Override
        public ObjectStore<T> deserialize(DataInput decoder) throws IOException {
            long size = decoder.readLong();
            if (size == -1) {
                return null;
            }
            long effectiveSize = decoder.readLong();

            ObjectStore<T> store = new ObjectStore<>();
            if (size > 0) {
                store.set(size - 1, null);
            }
            for (long i = 0; i < effectiveSize; i++) {
                store.set(i, elementSerializer.deserialize(decoder));
            }

            return store;
        }

        private long effectiveSize(ObjectStore<T> store) {
            for (long lastIndex = store.size() - 1; lastIndex >= 0; lastIndex--) {
                if (store.get(lastIndex) != null) {
                    return lastIndex + 1;
                }
            }

            return 0;
        }
    }

    static final class ObjectPoolStoreSerializer<T> implements Serializer<ObjectPoolStore<T>> {

        private final Serializer<T> elementSerializer;

        ObjectPoolStoreSerializer(Serializer<T> elementSerializer) {
            this.elementSerializer = elementSerializer;
        }

        @Override
        public void serialize(DataOutput encoder, ObjectPoolStore<T> store) throws IOException {
            if (store == null) {
                encoder.writeInt(-1);
            } else {
                int size = store.size();
                encoder.writeInt(size);
                for (int i = 0; i < size; i++) {
                    elementSerializer.serialize(encoder, store.get(i));
                }
            }
        }

        @Override
        public ObjectPoolStore<T> deserialize(DataInput decoder) throws IOException {
            int size = decoder.readInt();
            if (size == -1) {
                return null;
            }

            ObjectPoolStore<T> store = new ObjectPoolStore<>();
            for (int i = 0; i < size; i++) {
                store.set(elementSerializer.deserialize(decoder));
            }
            return store;
        }
    }

}
