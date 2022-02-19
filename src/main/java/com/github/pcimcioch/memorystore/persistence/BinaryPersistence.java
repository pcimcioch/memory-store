package com.github.pcimcioch.memorystore.persistence;

import com.github.pcimcioch.memorystore.Table;
import com.github.pcimcioch.memorystore.encoder.Encoder;
import com.github.pcimcioch.memorystore.header.Header;
import com.github.pcimcioch.memorystore.header.ObjectDirectHeader;
import com.github.pcimcioch.memorystore.layout.MemoryLayoutBuilder;
import com.github.pcimcioch.memorystore.persistence.HeaderDefinition.BitHeaderDefinition;
import com.github.pcimcioch.memorystore.persistence.serializer.Serializer;
import com.github.pcimcioch.memorystore.persistence.serializer.Serializers;
import com.github.pcimcioch.memorystore.persistence.serializer.collection.CollectionSerializer;
import com.github.pcimcioch.memorystore.store.IntStore;
import com.github.pcimcioch.memorystore.store.ObjectPoolStore;
import com.github.pcimcioch.memorystore.store.ObjectStore;
import com.github.pcimcioch.memorystore.store.StoreFactory;
import com.github.pcimcioch.memorystore.util.Utils;

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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.pcimcioch.memorystore.header.ObjectPoolHeader.PoolDefinition;
import static com.github.pcimcioch.memorystore.persistence.serializer.Serializers.listOf;

/**
 * Saves Table data to the stream of bytes.
 * <p>
 * Technically, the table will be serialized to binary using
 * {@link com.github.pcimcioch.memorystore.persistence.serializer.Serializer}
 */
// TODO javadoc
// TODO tests
public class BinaryPersistence extends Table.Accessor {

    private final CollectionSerializer<HeaderDefinition, List<HeaderDefinition>> headersSerializer;
    private final IntStoreSerializer intStoreSerializer;
    private final Serializer<Map<String, ObjectStore<?>>> objectStoresSerializer;
    private final Serializer<Map<String, ObjectPoolStore<?>>> poolStoresSerializer;

    @SuppressWarnings("unchecked")
    public BinaryPersistence(Map<ObjectDirectHeader<?>, Serializer<?>> objectSerializers,
                             Map<PoolDefinition, Serializer<?>> poolSerializers) {
        Map<String, Serializer<ObjectStore<?>>> objectStoreSerializers = Utils.remap(objectSerializers,
                Header::name,
                v -> new ObjectStoreSerializer(v)
        );
        Map<String, Serializer<ObjectPoolStore<?>>> poolStoreSerializers = Utils.remap(poolSerializers,
                PoolDefinition::name,
                v -> new ObjectPoolStoreSerializer(v)
        );

        this.headersSerializer = listOf(HeaderDefinition.SERIALIZER);
        this.intStoreSerializer = new IntStoreSerializer();
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
        headersSerializer.serialize(stream, HeaderDefinition.from(encoders(table)));
        intStoreSerializer.serialize(stream, intStore(table));
        objectStoresSerializer.serialize(stream, objectStoresByName(table));
        poolStoresSerializer.serialize(stream, poolStoresByName(table));
    }

    public Table load(DataInput stream, Collection<? extends Header<? extends Encoder>> headers) throws IOException {
        List<HeaderDefinition> headerDefinitions = headersSerializer.deserialize(stream);
        IntStore intStore = intStoreSerializer.deserialize(stream);
        Map<String, ObjectStore<?>> objectStores = objectStoresSerializer.deserialize(stream);
        Map<String, ObjectPoolStore<?>> poolStores = poolStoresSerializer.deserialize(stream);

        List<BitHeaderDefinition> bitHeaderDefinitions = filterBitHeaders(headerDefinitions);
        MemoryLayoutBuilder layoutBuilder = new LoaderMemoryLayoutBuilder(bitHeaderDefinitions);
        StoreFactory storeFactory = new LoaderStoreFactory(intStore, objectStores, poolStores);

        return buildTable(layoutBuilder, storeFactory, headers);
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

    private List<BitHeaderDefinition> filterBitHeaders(List<HeaderDefinition> headerDefinitions) {
        return headerDefinitions.stream()
                .filter(BitHeaderDefinition.class::isInstance)
                .map(BitHeaderDefinition.class::cast)
                .collect(Collectors.toList());
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