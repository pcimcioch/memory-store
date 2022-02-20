package com.github.pcimcioch.memorystore.persistence.binary;

import com.github.pcimcioch.memorystore.Table;
import com.github.pcimcioch.memorystore.encoder.Encoder;
import com.github.pcimcioch.memorystore.header.Header;
import com.github.pcimcioch.memorystore.header.ObjectDirectHeader;
import com.github.pcimcioch.memorystore.persistence.binary.LoaderMemoryLayout.LoaderMemoryLayoutSerializer;
import com.github.pcimcioch.memorystore.persistence.binary.StoreSerializers.IntStoreSerializer;
import com.github.pcimcioch.memorystore.store.IntStore;
import com.github.pcimcioch.memorystore.store.ObjectPoolStore;
import com.github.pcimcioch.memorystore.store.ObjectStore;
import com.github.pcimcioch.memorystore.store.StoreFactory;
import com.github.pcimcioch.memorystore.util.Utils;
import com.github.pcimcioch.serializer.Serializer;
import com.github.pcimcioch.serializer.Serializers;
import com.github.pcimcioch.serializer.collection.CollectionSerializer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.github.pcimcioch.memorystore.header.ObjectPoolHeader.PoolDefinition;
import static com.github.pcimcioch.memorystore.util.Utils.assertArgument;

/**
 * Saves Table data to the stream of bytes.
 * <p>
 * Technically, the table will be serialized to binary using
 * {@link Serializer}
 */
// TODO javadoc
// TODO tests
public class BinaryPersistence extends Table.Accessor {

    private final CollectionSerializer<HeaderDefinition, Set<HeaderDefinition>> headersSerializer;
    private final LoaderMemoryLayoutSerializer memoryLayoutSerializer;
    private final IntStoreSerializer intStoreSerializer;
    private final Serializer<Map<String, ObjectStore<?>>> objectStoresSerializer;
    private final Serializer<Map<String, ObjectPoolStore<?>>> poolStoresSerializer;

    @SuppressWarnings("unchecked")
    public BinaryPersistence(Map<ObjectDirectHeader<?>, Serializer<?>> objectSerializers,
                             Map<PoolDefinition, Serializer<?>> poolSerializers) {
        Map<String, Serializer<ObjectStore<?>>> objectStoreSerializers = Utils.remap(objectSerializers,
                Header::name,
                v -> (Serializer) StoreSerializers.objectStore(v)
        );
        Map<String, Serializer<ObjectPoolStore<?>>> poolStoreSerializers = Utils.remap(poolSerializers,
                PoolDefinition::name,
                v -> (Serializer) StoreSerializers.objectPoolStore(v)
        );

        this.headersSerializer = Serializers.setOf(HeaderDefinition.SERIALIZER);
        this.memoryLayoutSerializer = LoaderMemoryLayout.serializer();
        this.intStoreSerializer = StoreSerializers.intStore();
        this.objectStoresSerializer = Serializers.mapOf(Serializers.string(), objectStoreSerializers::get);
        this.poolStoresSerializer = Serializers.mapOf(Serializers.string(), poolStoreSerializers::get);
    }

    public void save(Path path, Table table) throws IOException {
        try (DataOutputStream output = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(path)))) {
            save(output, table);
        }
    }

    public Table load(Path path, Collection<? extends Header<? extends Encoder>> headers) throws IOException {
        try (DataInputStream input = new DataInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            return load(input, headers);
        }
    }

    public void save(DataOutput stream, Table table) throws IOException {
        headersSerializer.serialize(stream, HeaderDefinition.from(table.headers()));
        memoryLayoutSerializer.serialize(stream, memoryLayout(table));
        intStoreSerializer.serialize(stream, intStore(table));
        objectStoresSerializer.serialize(stream, objectStoresByName(table));
        poolStoresSerializer.serialize(stream, poolStoresByName(table));
    }

    public Table load(DataInput stream, Collection<? extends Header<? extends Encoder>> headers) throws IOException {
        Set<HeaderDefinition> dataHeaders = headersSerializer.deserialize(stream);
        Set<HeaderDefinition> tableHeaders = HeaderDefinition.from(headers);
        assertArgument(dataHeaders.equals(tableHeaders),
                "Incorrect headers. Data contains %s but provided %s", dataHeaders, tableHeaders);

        LoaderMemoryLayout memoryLayout = memoryLayoutSerializer.deserialize(stream);

        StoreFactory storeFactory = new LoaderStoreFactory(
                intStoreSerializer.deserialize(stream),
                objectStoresSerializer.deserialize(stream),
                poolStoresSerializer.deserialize(stream)
        );

        return buildTable(memoryLayout, storeFactory, headers);
    }

    private LoaderMemoryLayout memoryLayout(Table table) {
        return new LoaderMemoryLayout(encoders(table));
    }

    private Map<String, ObjectStore<?>> objectStoresByName(Table table) {
        return Utils.remap(objectStores(table),
                Header::name,
                Function.identity()
        );
    }

    private Map<String, ObjectPoolStore<?>> poolStoresByName(Table table) {
        return Utils.remap(objectPoolStores(table),
                PoolDefinition::name,
                Function.identity()
        );
    }

    public static BinaryStreamPersistenceBuilder builder() {
        return new BinaryStreamPersistenceBuilder();
    }

    public static final class BinaryStreamPersistenceBuilder {
        private final Map<ObjectDirectHeader<?>, Serializer<?>> objectSerializers = new HashMap<>();
        private final Map<PoolDefinition, Serializer<?>> poolSerializers = new HashMap<>();

        public <T> BinaryStreamPersistenceBuilder registerSerializer(ObjectDirectHeader<T> header, Serializer<T> serializer) {
            objectSerializers.put(header, serializer);
            return this;
        }

        public BinaryStreamPersistenceBuilder registerSerializer(PoolDefinition pool, Serializer<?> serializer) {
            poolSerializers.put(pool, serializer);
            return this;
        }

        public BinaryPersistence build() {
            return new BinaryPersistence(objectSerializers, poolSerializers);
        }
    }
}