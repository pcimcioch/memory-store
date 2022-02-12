package com.github.pcimcioch.memorystore;

import com.github.pcimcioch.memorystore.encoder.BitEncoder;
import com.github.pcimcioch.memorystore.encoder.Encoder;
import com.github.pcimcioch.memorystore.encoder.ObjectDirectEncoder;
import com.github.pcimcioch.memorystore.encoder.ObjectPoolEncoder;
import com.github.pcimcioch.memorystore.encoder.UnsignedIntegerEncoder;
import com.github.pcimcioch.memorystore.header.BitHeader;
import com.github.pcimcioch.memorystore.header.Header;
import com.github.pcimcioch.memorystore.header.ObjectDirectHeader;
import com.github.pcimcioch.memorystore.header.ObjectPoolHeader;
import com.github.pcimcioch.memorystore.header.ObjectPoolHeader.PoolDefinition;
import com.github.pcimcioch.memorystore.layout.AutomaticMemoryLayoutBuilder;
import com.github.pcimcioch.memorystore.layout.MemoryLayoutBuilder;
import com.github.pcimcioch.memorystore.layout.MemoryLayoutBuilder.MemoryLayout;
import com.github.pcimcioch.memorystore.layout.MemoryLayoutBuilder.MemoryPosition;
import com.github.pcimcioch.memorystore.store.IntStore;
import com.github.pcimcioch.memorystore.store.ObjectPoolStore;
import com.github.pcimcioch.memorystore.store.ObjectStore;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.pcimcioch.memorystore.BitUtils.assertArgument;
import static java.util.Collections.unmodifiableSet;

/**
 * Used to create in memory table storing records of structure defined by passed headers.
 */
@SuppressWarnings("unchecked")
public class Table {

    private static final int WORD_SIZE = 32;

    private final Map<Header<? extends Encoder>, Encoder> encoders = new HashMap<>();
    private final Map<PoolDefinition, ObjectPoolStore<?>> objectPoolStores = new HashMap<>();
    private final Map<ObjectDirectHeader<?>, ObjectStore<?>> objectStores = new HashMap<>();
    private final IntStore intStore;

    /**
     * Create store for records with fields defined by passed headers
     *
     * @param headers headers
     */
    public Table(Collection<? extends Header<? extends Encoder>> headers) {
        this(new AutomaticMemoryLayoutBuilder(), headers);
    }

    /**
     * Create store for records with fields defined by passed headers
     *
     * @param memoryLayoutBuilder memory layout to use when storing bit headers
     * @param headers             headers
     */
    public Table(MemoryLayoutBuilder memoryLayoutBuilder, Collection<? extends Header<? extends Encoder>> headers) {
        this.intStore = initBitEncoders(headers, memoryLayoutBuilder);
        initObjectEncoders(headers);
        initObjectPoolEncoders(headers);
    }

    /**
     * Returns encoder for given header
     *
     * @param header header
     * @param <T>    type of encoder
     * @return encoder of null if no encoder exists for passed header
     */
    public <T extends Encoder> T encoderFor(Header<T> header) {
        return (T) encoders.get(header);
    }

    /**
     * Returns all the headers registered in this table
     *
     * @return all the headers
     */
    public Set<Header<? extends Encoder>> headers() {
        return unmodifiableSet(encoders.keySet());
    }

    IntStore intStore() {
        return intStore;
    }

    Map<ObjectDirectHeader<?>, ObjectStore<?>> objectStores() {
        return objectStores;
    }

    Map<PoolDefinition, ObjectPoolStore<?>> objectPoolStores() {
        return objectPoolStores;
    }

    private IntStore initBitEncoders(Collection<? extends Header<? extends Encoder>> allHeaders, MemoryLayoutBuilder memoryLayoutBuilder) {
        List<BitHeader<?>> headers = filterBitHeaders(allHeaders).collect(Collectors.toList());
        if (headers.isEmpty()) {
            return null;
        }

        IntStore store = new IntStore();
        MemoryLayout layout = memoryLayoutBuilder.compute(WORD_SIZE, headers);

        for (BitHeader<?> header : headers) {
            MemoryPosition memoryPosition = layout.memoryPositionFor(header);
            BitEncoder.Config config = new BitEncoder.Config(
                    store,
                    layout.recordSize(),
                    memoryPosition.positionInRecord(),
                    memoryPosition.bitShift(),
                    header.bitsCount()
            );
            addEncoder(header, header.encoderFactory().apply(config));
        }

        return store;
    }

    private void initObjectEncoders(Collection<? extends Header<? extends Encoder>> allHeaders) {
        List<ObjectDirectHeader<?>> headers = filterObjectHeaders(allHeaders).collect(Collectors.toList());
        if (headers.isEmpty()) {
            return;
        }

        for (ObjectDirectHeader<?> header : headers) {
            ObjectStore<?> store = addObjectStore(header);
            addEncoder(header, new ObjectDirectEncoder<>(store));
        }
    }

    private void initObjectPoolEncoders(Collection<? extends Header<? extends Encoder>> allHeaders) {
        List<ObjectPoolHeader<?>> headers = filterObjectPoolHeaders(allHeaders).collect(Collectors.toList());
        if (headers.isEmpty()) {
            return;
        }

        for (ObjectPoolHeader<?> header : headers) {
            ObjectPoolStore<?> store = addObjectPoolStore(header.poolDefinition());
            UnsignedIntegerEncoder indexEncoder = encoderFor(header.poolIndexHeader());

            addEncoder(header, new ObjectPoolEncoder<>(store, indexEncoder));
        }
    }

    private void addEncoder(Header<?> header, Encoder encoder) {
        encoders.keySet().stream()
                .map(Header::name)
                .forEach(headerName -> assertArgument(!header.name().equals(headerName), "Duplicated header name %s", headerName));

        encoders.put(header, encoder);
    }

    private ObjectStore<?> addObjectStore(ObjectDirectHeader<?> header) {
        ObjectStore<?> store = new ObjectStore<>();
        objectStores.put(header, store);

        return store;
    }

    private ObjectPoolStore<?> addObjectPoolStore(PoolDefinition poolDefinition) {
        ObjectPoolStore<?> store = objectPoolStores.get(poolDefinition);
        if (store != null) {
            return store;
        }

        objectPoolStores.keySet().stream()
                .map(PoolDefinition::name)
                .forEach(poolName -> assertArgument(!poolDefinition.name().equals(poolName), "Duplicated pool name %s", poolName));

        store = new ObjectPoolStore<>();
        objectPoolStores.put(poolDefinition, store);

        return store;
    }

    private static Stream<BitHeader<?>> filterBitHeaders(Collection<? extends Header<? extends Encoder>> headers) {
        Stream<BitHeader<?>> bitHeaders = headers.stream()
                .filter(BitHeader.class::isInstance)
                .map(BitHeader.class::cast);
        Stream<BitHeader<?>> poolIndexHeader = filterObjectPoolHeaders(headers).map(ObjectPoolHeader::poolIndexHeader);

        return Stream.concat(bitHeaders, poolIndexHeader);
    }

    private static Stream<ObjectDirectHeader<?>> filterObjectHeaders(Collection<? extends Header<? extends Encoder>> headers) {
        return headers.stream()
                .filter(ObjectDirectHeader.class::isInstance)
                .map(ObjectDirectHeader.class::cast);
    }

    private static Stream<ObjectPoolHeader<?>> filterObjectPoolHeaders(Collection<? extends Header<? extends Encoder>> allHeaders) {
        return allHeaders.stream()
                .filter(ObjectPoolHeader.class::isInstance)
                .map(ObjectPoolHeader.class::cast);
    }
}
