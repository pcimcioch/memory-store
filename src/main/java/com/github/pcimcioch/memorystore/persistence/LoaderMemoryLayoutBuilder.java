package com.github.pcimcioch.memorystore.persistence;

import com.github.pcimcioch.memorystore.header.BitHeader;
import com.github.pcimcioch.memorystore.layout.MemoryLayoutBuilder;
import com.github.pcimcioch.memorystore.persistence.HeaderDefinition.BitHeaderDefinition;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.pcimcioch.memorystore.util.Utils.assertArgument;

class LoaderMemoryLayoutBuilder implements MemoryLayoutBuilder {

    private static final int WORD_SIZE = 32;

    private final int recordSize;
    private final Map<String, MemoryPosition> headerMemoryPositions;

    LoaderMemoryLayoutBuilder(List<BitHeaderDefinition> headerDefinitions) {
        this.recordSize = recordSize(headerDefinitions);
        this.headerMemoryPositions = headerMemoryPositions(headerDefinitions);
    }

    @Override
    public MemoryLayout compute(int wordSize, Collection<? extends BitHeader<?>> headers) {
        assertArgument(WORD_SIZE == wordSize, "This memory layout supports %d word size, but %d requested", WORD_SIZE, wordSize);

        Map<BitHeader<?>, MemoryPosition> memoryPositions = headers.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        this::findMemoryPosition
                ));

        return new MemoryLayout(recordSize, memoryPositions);
    }

    private MemoryPosition findMemoryPosition(BitHeader<?> header) {
        MemoryPosition memoryPosition = headerMemoryPositions.get(header.name());
        // TODO move this check up to Table
        assertArgument(memoryPosition != null, "Cannot find Memory Position for header %s", header.name());

        return memoryPosition;
    }

    private int recordSize(List<BitHeaderDefinition> headerDefinitions) {
        return headerDefinitions.stream()
                .map(BitHeaderDefinition::recordSize)
                .findFirst()
                .orElse(0);
    }

    private Map<String, MemoryPosition> headerMemoryPositions(List<BitHeaderDefinition> headerDefinitions) {
        return headerDefinitions.stream()
                .collect(Collectors.toMap(
                        HeaderDefinition::name,
                        this::memoryPosition
                ));
    }

    private MemoryPosition memoryPosition(BitHeaderDefinition headerDefinition) {
        return new MemoryPosition(headerDefinition.positionInRecord(), headerDefinition.bitShift());
    }
}
