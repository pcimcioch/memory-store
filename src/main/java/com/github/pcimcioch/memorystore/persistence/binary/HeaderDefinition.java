package com.github.pcimcioch.memorystore.persistence.binary;

import com.github.pcimcioch.memorystore.encoder.Encoder;
import com.github.pcimcioch.memorystore.header.BitHeader;
import com.github.pcimcioch.memorystore.header.Header;
import com.github.pcimcioch.memorystore.header.ObjectDirectHeader;
import com.github.pcimcioch.memorystore.header.ObjectPoolHeader;
import com.github.pcimcioch.serializer.Serializer;
import com.github.pcimcioch.serializer.common.MultiTypeSerializer;
import com.github.pcimcioch.serializer.common.MultiTypeSerializer.TypeMapping;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.pcimcioch.serializer.Serializers.multiTypeOf;
import static com.github.pcimcioch.serializer.Serializers.string;

// TODO add tests
abstract class HeaderDefinition {

    static final MultiTypeSerializer<HeaderDefinition> SERIALIZER = multiTypeOf(List.of(
            new TypeMapping<>(ObjectPoolHeaderDefinition.class, new ObjectPoolHeaderDefinitionSerializer()),
            new TypeMapping<>(ObjectDirectHeaderDefinition.class, new ObjectDirectHeaderDefinitionSerializer()),
            new TypeMapping<>(BitHeaderDefinition.class, new BitHeaderDefinitionSerializer())
    ));

    protected final String name;

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

    @Override
    public String toString() {
        return "HeaderDefinition{" +
                "name='" + name + '\'' +
                '}';
    }

    static HeaderDefinition from(Header<?> header) {
        if (header instanceof ObjectPoolHeader) {
            return new ObjectPoolHeaderDefinition((ObjectPoolHeader<?>) header);
        }
        if (header instanceof ObjectDirectHeader) {
            return new ObjectDirectHeaderDefinition((ObjectDirectHeader<?>) header);
        }
        if (header instanceof BitHeader) {
            return new BitHeaderDefinition((BitHeader<?>) header);
        }

        throw new IllegalArgumentException("Unable to build header definition from header " + header.getClass());
    }

    static Set<HeaderDefinition> from(Collection<? extends Header<? extends Encoder>> headers) {
        return headers.stream()
                .map(HeaderDefinition::from)
                .collect(Collectors.toSet());
    }

    static final class ObjectPoolHeaderDefinition extends HeaderDefinition {
        private final String pool;
        private final int poolBits;

        ObjectPoolHeaderDefinition(String name, String pool, int poolBits) {
            super(name);
            this.pool = pool;
            this.poolBits = poolBits;
        }

        ObjectPoolHeaderDefinition(ObjectPoolHeader<?> header) {
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

        @Override
        public String toString() {
            return "ObjectPoolHeaderDefinition{" +
                    "name='" + name + '\'' +
                    ", pool='" + pool + '\'' +
                    ", poolBits=" + poolBits +
                    '}';
        }
    }

    static final class ObjectDirectHeaderDefinition extends HeaderDefinition {
        ObjectDirectHeaderDefinition(String name) {
            super(name);
        }

        ObjectDirectHeaderDefinition(ObjectDirectHeader<?> header) {
            this(header.name());
        }

        @Override
        public String toString() {
            return "ObjectDirectHeaderDefinition{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

    static final class BitHeaderDefinition extends HeaderDefinition {
        private final int bitsCount;
        private final int maxLastBit;

        BitHeaderDefinition(String name, int bitsCount, int maxLastBit) {
            super(name);
            this.bitsCount = bitsCount;
            this.maxLastBit = maxLastBit;
        }

        BitHeaderDefinition(BitHeader<?> header) {
            this(header.name(), header.bitsCount(), header.maxLastBit());
        }

        int bitsCount() {
            return bitsCount;
        }

        int maxLastBit() {
            return maxLastBit;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            BitHeaderDefinition that = (BitHeaderDefinition) o;
            return bitsCount == that.bitsCount && maxLastBit == that.maxLastBit;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), bitsCount, maxLastBit);
        }

        @Override
        public String toString() {
            return "BitHeaderDefinition{" +
                    "name='" + name + '\'' +
                    ", bitsCount=" + bitsCount +
                    ", maxLastBit=" + maxLastBit +
                    '}';
        }
    }

    private static final class ObjectPoolHeaderDefinitionSerializer implements Serializer<ObjectPoolHeaderDefinition> {
        @Override
        public void serialize(DataOutput encoder, ObjectPoolHeaderDefinition object) throws IOException {
            string().serialize(encoder, object.name);
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

    private static final class ObjectDirectHeaderDefinitionSerializer implements Serializer<ObjectDirectHeaderDefinition> {
        @Override
        public void serialize(DataOutput encoder, ObjectDirectHeaderDefinition object) throws IOException {
            string().serialize(encoder, object.name);
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
            string().serialize(encoder, object.name);
            encoder.writeInt(object.bitsCount);
            encoder.writeInt(object.maxLastBit);
        }

        @Override
        public BitHeaderDefinition deserialize(DataInput decoder) throws IOException {
            return new BitHeaderDefinition(
                    string().deserialize(decoder),
                    decoder.readInt(),
                    decoder.readInt()
            );
        }
    }
}
