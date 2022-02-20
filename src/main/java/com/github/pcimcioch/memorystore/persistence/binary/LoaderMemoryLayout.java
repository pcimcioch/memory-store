package com.github.pcimcioch.memorystore.persistence.binary;

import com.github.pcimcioch.memorystore.encoder.BitEncoder;
import com.github.pcimcioch.memorystore.encoder.Encoder;
import com.github.pcimcioch.memorystore.header.BitHeader;
import com.github.pcimcioch.memorystore.header.Header;
import com.github.pcimcioch.memorystore.layout.MemoryLayoutBuilder;
import com.github.pcimcioch.serializer.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.pcimcioch.memorystore.util.Utils.assertArgument;
import static com.github.pcimcioch.serializer.Serializers.mapOf;
import static com.github.pcimcioch.serializer.Serializers.string;

//TODO tests
class LoaderMemoryLayout implements MemoryLayoutBuilder {

    private static final int WORD_SIZE = 32;

    static final LoaderMemoryLayoutSerializer SERIALIZER = new LoaderMemoryLayoutSerializer();

    private final int recordSize;
    private final Map<String, MemoryPosition> headerMemoryPositions;

    LoaderMemoryLayout(Map<Header<? extends Encoder>, Encoder> encoders) {
        this(recordSize(encoders), headerMemoryPositions(encoders));
    }

    LoaderMemoryLayout(int recordSize, Map<String, MemoryPosition> headerMemoryPositions) {
        this.recordSize = recordSize;
        this.headerMemoryPositions = headerMemoryPositions;
    }

    @Override
    public MemoryLayout compute(int wordSize, Collection<? extends BitHeader<?>> headers) {
        assertArgument(WORD_SIZE == wordSize, "This memory layout supports %d word size, but %d requested", WORD_SIZE, wordSize);

        Map<BitHeader<?>, MemoryPosition> memoryPositions = headers.stream()
                .collect(
                        HashMap::new,
                        (m, v) -> m.put(v, headerMemoryPositions.get(v.name())),
                        HashMap::putAll
                );

        return new MemoryLayout(recordSize, memoryPositions);
    }

    private static int recordSize(Map<Header<? extends Encoder>, Encoder> encoders) {
        return encoders.values().stream()
                .filter(BitEncoder.class::isInstance)
                .map(BitEncoder.class::cast)
                .mapToInt(BitEncoder::recordSize)
                .findFirst()
                .orElse(0);
    }

    private static Map<String, MemoryPosition> headerMemoryPositions(Map<Header<? extends Encoder>, Encoder> encoders) {
        return encoders.entrySet().stream()
                .filter(e -> e.getValue() instanceof BitEncoder)
                .collect(Collectors.toMap(
                        e -> e.getKey().name(),
                        e -> memoryPosition((BitEncoder) e.getValue())
                ));
    }

    private static MemoryPosition memoryPosition(BitEncoder encoder) {
        return new MemoryPosition(encoder.positionInRecord(), encoder.bitShift());
    }

    static final class LoaderMemoryLayoutSerializer implements Serializer<LoaderMemoryLayout> {
        private final Serializer<Map<String, MemoryPosition>> positionsSerializer = mapOf(
                string(),
                new MemoryPositionSerializer()
        );

        @Override
        public void serialize(DataOutput encoder, LoaderMemoryLayout layout) throws IOException {
            if (layout == null) {
                encoder.writeInt(-1);
            } else {
                encoder.writeInt(layout.recordSize);
                positionsSerializer.serialize(encoder, layout.headerMemoryPositions);
            }
        }

        @Override
        public LoaderMemoryLayout deserialize(DataInput decoder) throws IOException {
            int recordSize = decoder.readInt();
            if (recordSize == -1) {
                return null;
            }
            Map<String, MemoryPosition> positions = positionsSerializer.deserialize(decoder);

            return new LoaderMemoryLayout(recordSize, positions);
        }
    }

    static final class MemoryPositionSerializer implements Serializer<MemoryPosition> {
        @Override
        public void serialize(DataOutput encoder, MemoryPosition memoryPosition) throws IOException {
            if (memoryPosition == null) {
                encoder.writeInt(-1);
            } else {
                encoder.writeInt(memoryPosition.positionInRecord());
                encoder.writeInt(memoryPosition.bitShift());
            }
        }

        @Override
        public MemoryPosition deserialize(DataInput decoder) throws IOException {
            int positionInRecord = decoder.readInt();
            if (positionInRecord == -1) {
                return null;
            }
            int bitShift = decoder.readInt();

            return new MemoryPosition(positionInRecord, bitShift);
        }
    }
}
