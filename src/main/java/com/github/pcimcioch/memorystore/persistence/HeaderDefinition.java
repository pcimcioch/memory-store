package com.github.pcimcioch.memorystore.persistence;

import com.github.pcimcioch.memorystore.encoder.BitEncoder;
import com.github.pcimcioch.memorystore.encoder.Encoder;
import com.github.pcimcioch.memorystore.header.BitHeader;
import com.github.pcimcioch.memorystore.header.Header;
import com.github.pcimcioch.memorystore.header.ObjectDirectHeader;
import com.github.pcimcioch.memorystore.header.ObjectPoolHeader;
import com.github.pcimcioch.memorystore.persistence.serializer.Serializer;
import com.github.pcimcioch.memorystore.persistence.serializer.common.MultiTypeSerializer;
import com.github.pcimcioch.memorystore.persistence.serializer.common.MultiTypeSerializer.TypeMapping;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.github.pcimcioch.memorystore.persistence.serializer.Serializers.multiTypeOf;
import static com.github.pcimcioch.memorystore.persistence.serializer.Serializers.string;

// TODO add tests
abstract class HeaderDefinition {

    static final MultiTypeSerializer<HeaderDefinition> SERIALIZER = multiTypeOf(List.of(
            new TypeMapping<>(ObjectPoolHeaderDefinition.class, new ObjectPoolHeaderDefinitionSerializer()),
            new TypeMapping<>(ObjectDirectHeaderDefinition.class, new ObjectDirectHeaderDefinitionSerializer()),
            new TypeMapping<>(BitHeaderDefinition.class, new BitHeaderDefinitionSerializer())
    ));

    private final String name;

    private HeaderDefinition(String name) {
        this.name = name;
    }

    String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HeaderDefinition that = (HeaderDefinition) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    static HeaderDefinition from(Header<?> header, Encoder encoder) {
        if (header instanceof ObjectPoolHeader) {
            return new ObjectPoolHeaderDefinition((ObjectPoolHeader<?>) header);
        }
        if (header instanceof ObjectDirectHeader) {
            return new ObjectDirectHeaderDefinition((ObjectDirectHeader<?>) header);
        }
        if (header instanceof BitHeader && encoder instanceof BitEncoder) {
            return new BitHeaderDefinition((BitHeader<?>) header, (BitEncoder) encoder);
        }

        throw new IllegalArgumentException("Unable to build header definition from header " + header.getClass());
    }

    static List<HeaderDefinition> from(Map<Header<? extends Encoder>, Encoder> headerToEncoder) {
        return headerToEncoder.entrySet().stream()
                .map(e -> from(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    static final class ObjectPoolHeaderDefinition extends HeaderDefinition {
        private final String pool;
        private final int poolBits;

        private ObjectPoolHeaderDefinition(String name, String pool, int poolBits) {
            super(name);
            this.pool = pool;
            this.poolBits = poolBits;
        }

        private ObjectPoolHeaderDefinition(ObjectPoolHeader<?> header) {
            this(header.name(), header.poolDefinition().name(), header.poolDefinition().poolBits());
        }

        String pool() {
            return pool;
        }

        int poolBits() {
            return poolBits;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            ObjectPoolHeaderDefinition that = (ObjectPoolHeaderDefinition) o;
            return poolBits == that.poolBits && pool.equals(that.pool);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), pool, poolBits);
        }
    }

    static final class ObjectDirectHeaderDefinition extends HeaderDefinition {
        public ObjectDirectHeaderDefinition(String name) {
            super(name);
        }

        public ObjectDirectHeaderDefinition(ObjectDirectHeader<?> header) {
            this(header.name());
        }
    }

    static final class BitHeaderDefinition extends HeaderDefinition {
        private final int recordSize;
        private final int positionInRecord;
        private final int bitsCount;
        private final int bitShift;

        private BitHeaderDefinition(String name, int recordSize, int positionInRecord, int bitsCount, int bitShift) {
            super(name);
            this.recordSize = recordSize;
            this.positionInRecord = positionInRecord;
            this.bitsCount = bitsCount;
            this.bitShift = bitShift;
        }

        private BitHeaderDefinition(BitHeader<?> header, BitEncoder enc) {
            this(header.name(), enc.recordSize(), enc.positionInRecord(), enc.bitsCount(), enc.bitShift());
        }

        int recordSize() {
            return recordSize;
        }

        int positionInRecord() {
            return positionInRecord;
        }

        int bitsCount() {
            return bitsCount;
        }

        int bitShift() {
            return bitShift;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            BitHeaderDefinition that = (BitHeaderDefinition) o;
            return recordSize == that.recordSize && positionInRecord == that.positionInRecord && bitsCount == that.bitsCount && bitShift == that.bitShift;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), recordSize, positionInRecord, bitsCount, bitShift);
        }
    }

    private static final class ObjectPoolHeaderDefinitionSerializer implements Serializer<ObjectPoolHeaderDefinition> {
        @Override
        public void serialize(DataOutput encoder, ObjectPoolHeaderDefinition object) throws IOException {
            string().serialize(encoder, object.name());
            string().serialize(encoder, object.pool);
            encoder.writeInt(object.poolBits);
        }

        @Override
        public ObjectPoolHeaderDefinition deserialize(DataInput decoder) throws IOException {
            return new ObjectPoolHeaderDefinition(
                    string().deserialize(decoder),
                    string().deserialize(decoder),
                    decoder.readInt()
            );
        }
    }

    private static final class ObjectDirectHeaderDefinitionSerializer
            implements Serializer<ObjectDirectHeaderDefinition> {
        @Override
        public void serialize(DataOutput encoder, ObjectDirectHeaderDefinition object) throws IOException {
            string().serialize(encoder, object.name());
        }

        @Override
        public ObjectDirectHeaderDefinition deserialize(DataInput decoder) throws IOException {
            return new ObjectDirectHeaderDefinition(
                    string().deserialize(decoder)
            );
        }
    }

    private static final class BitHeaderDefinitionSerializer implements Serializer<BitHeaderDefinition> {
        @Override
        public void serialize(DataOutput encoder, BitHeaderDefinition object) throws IOException {
            string().serialize(encoder, object.name());
            encoder.writeInt(object.recordSize);
            encoder.writeInt(object.positionInRecord);
            encoder.writeInt(object.bitsCount);
            encoder.writeInt(object.bitShift);
        }

        @Override
        public BitHeaderDefinition deserialize(DataInput decoder) throws IOException {
            return new BitHeaderDefinition(
                    string().deserialize(decoder),
                    decoder.readInt(),
                    decoder.readInt(),
                    decoder.readInt(),
                    decoder.readInt()
            );
        }
    }
}
