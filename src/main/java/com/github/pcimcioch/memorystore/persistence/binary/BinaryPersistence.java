package com.github.pcimcioch.memorystore.persistence.binary;

import com.github.pcimcioch.memorystore.Table;
import com.github.pcimcioch.memorystore.encoder.Encoder;
import com.github.pcimcioch.memorystore.header.Header;
import com.github.pcimcioch.memorystore.header.ObjectDirectHeader;
import com.github.pcimcioch.memorystore.persistence.binary.LoaderMemoryLayout.LoaderMemoryLayoutSerializer;
import com.github.pcimcioch.memorystore.persistence.binary.StoreSerializers.IntStoreSerializer;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.github.pcimcioch.memorystore.header.ObjectPoolHeader.PoolDefinition;
import static com.github.pcimcioch.memorystore.util.Utils.assertArgument;

/**
 * Saves Table data to the stream of bytes.
 * <p>
 * Technically, the table will be serialized to binary using
 * {@link Serializer}
 */
public class BinaryPersistence extends Table.Accessor {

    private final CollectionSerializer<HeaderDefinition, Set<HeaderDefinition>> headersSerializer;
    private final LoaderMemoryLayoutSerializer memoryLayoutSerializer;
    private final IntStoreSerializer intStoreSerializer;
    private final Serializer<Map<String, ObjectStore<?>>> objectStoresSerializer;
    private final Serializer<Map<String, ObjectPoolStore<?>>> poolStoresSerializer;

    /**
     * Creates persistence that allows saving {@link Table} as binary stream.
     * You can also use {@link #builder()} to create this persistence in more readable way
     *
     * @param objectSerializers serializers that should be used to serialize elements of object headers
     * @param poolSerializers   serializers that should be used to serialize elements of object pool headers
     */
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
        this.memoryLayoutSerializer = LoaderMemoryLayout.SERIALIZER;
        this.intStoreSerializer = StoreSerializers.intStore();
        this.objectStoresSerializer = Serializers.mapOf(Serializers.string(), objectStoreSerializers::get);
        this.poolStoresSerializer = Serializers.mapOf(Serializers.string(), poolStoreSerializers::get);
    }

    /**
     * Save table to the file
     *
     * @param path  file where to save
     * @param table table to save
     * @throws IOException if file operation failed
     */
    public void save(Path path, Table table) throws IOException {
        try (DataOutputStream output = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(path)))) {
            save(output, table);
        }
    }

    /**
     * Loads table from the file
     *
     * @param path    file from which to load
     * @param headers headers of the Table
     * @return table
     * @throws IOException if file operation failed
     */
    public Table load(Path path, Collection<? extends Header<? extends Encoder>> headers) throws IOException {
        try (DataInputStream input = new DataInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            return load(input, headers);
        }
    }

    /**
     * Saves table to the compressed file. This will be a zip file with one entry called "table"
     *
     * @param path  file where to save
     * @param table table to save
     * @throws IOException if file operation failed
     */
    public void saveCompressed(Path path, Table table) throws IOException {
        try (ZipOutputStream zipStream = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(path)))) {
            zipStream.putNextEntry(new ZipEntry("table"));
            try (DataOutputStream output = new DataOutputStream(zipStream)) {
                save(output, table);
            }
        }
    }

    /**
     * Loads table from the compressed file. This must be a zip file with one entry called "table"
     *
     * @param path    file from which to load
     * @param headers headers of the Table
     * @return table
     * @throws IOException if file operation failed
     */
    public Table loadCompressed(Path path, Collection<? extends Header<? extends Encoder>> headers) throws IOException {
        try (ZipInputStream zipStream = new ZipInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            ZipEntry entry = zipStream.getNextEntry();
            if (entry == null) {
                throw new IOException("Missing entry in zip file");
            }
            try (DataInputStream input = new DataInputStream(zipStream)) {
                return load(input, headers);
            }
        }
    }

    /**
     * Saves table to the data stream
     *
     * @param stream stream where save to
     * @param table  table to save
     * @throws IOException if stream operation failed
     */
    public void save(DataOutput stream, Table table) throws IOException {
        headersSerializer.serialize(stream, HeaderDefinition.from(table.headers()));
        memoryLayoutSerializer.serialize(stream, memoryLayout(table));
        intStoreSerializer.serialize(stream, intStore(table));
        objectStoresSerializer.serialize(stream, objectStoresByName(table));
        poolStoresSerializer.serialize(stream, poolStoresByName(table));
    }

    /**
     * Loads table from the data stream
     *
     * @param stream  stream to read from
     * @param headers headers of the Table
     * @return table
     * @throws IOException if stream operation failed
     */
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
        return new LoaderMemoryLayout(32, encoders(table));
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

    /**
     * Persistence builder
     *
     * @return new builder
     */
    public static BinaryStreamPersistenceBuilder builder() {
        return new BinaryStreamPersistenceBuilder();
    }

    /**
     * Used to build {@link BinaryPersistence} in more readable way
     */
    public static final class BinaryStreamPersistenceBuilder {
        private final Map<ObjectDirectHeader<?>, Serializer<?>> objectSerializers = new HashMap<>();
        private final Map<PoolDefinition, Serializer<?>> poolSerializers = new HashMap<>();

        /**
         * Registers serializer for elements of object header
         *
         * @param header     object header
         * @param serializer serializer of the element
         * @param <T>        type of the object
         * @return builder
         */
        public <T> BinaryStreamPersistenceBuilder registerSerializer(ObjectDirectHeader<T> header, Serializer<T> serializer) {
            objectSerializers.put(header, serializer);
            return this;
        }

        /**
         * Registers serializer for elements of pool header
         *
         * @param pool       pool definition
         * @param serializer serializer of the element
         * @return builder
         */
        public BinaryStreamPersistenceBuilder registerSerializer(PoolDefinition pool, Serializer<?> serializer) {
            poolSerializers.put(pool, serializer);
            return this;
        }

        /**
         * Build persistence
         *
         * @return new persistence
         */
        public BinaryPersistence build() {
            return new BinaryPersistence(objectSerializers, poolSerializers);
        }
    }
}