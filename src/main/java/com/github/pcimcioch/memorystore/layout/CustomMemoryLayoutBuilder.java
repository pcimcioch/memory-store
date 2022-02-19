package com.github.pcimcioch.memorystore.layout;

import com.github.pcimcioch.memorystore.header.BitHeader;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.pcimcioch.memorystore.util.Utils.assertArgument;

/**
 * Base class for Memory Layouts that can be completely customized by the user.
 *
 * It simply stores predefined header to memory position map.
 *
 * It does two validations when creating:
 * - check whether given header at given position will not violate record size
 * - check whether given header at given position will not violate its own max last bit
 *
 * All additional validations must be done by in the constructors of child classes
 */
public abstract class CustomMemoryLayoutBuilder implements MemoryLayoutBuilder {

    public static final int DEFAULT_WORD_SIZE = 32;

    protected final int wordSize;
    protected final int recordSize;
    private final Map<BitHeader<?>, MemoryPosition> headerMemoryPositions;

    protected CustomMemoryLayoutBuilder(int recordSize, Map<? extends BitHeader<?>, MemoryPosition> headerMemoryPositions) {
        this(DEFAULT_WORD_SIZE, recordSize, headerMemoryPositions);
    }

    protected CustomMemoryLayoutBuilder(int wordSize, int recordSize, Map<? extends BitHeader<?>, MemoryPosition> headerMemoryPositions) {
        assertArgument(wordSize > 0, "Word Size must be over 0");
        assertArgument(recordSize > 0, "Record Size must be over 0");

        this.wordSize = wordSize;
        this.recordSize = recordSize;
        this.headerMemoryPositions = new HashMap<>(headerMemoryPositions);

        this.headerMemoryPositions.forEach(this::validatePosition);
    }

    @Override
    public MemoryLayout compute(int wordSize, Collection<? extends BitHeader<?>> headers) {
        assertArgument(this.wordSize == wordSize, "This memory layout supports %d word size, but %d requested", this.wordSize, wordSize);

        Map<BitHeader<?>, MemoryPosition> memoryPositions = headers.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        this::findMemoryPosition
                ));

        return new MemoryLayout(recordSize, memoryPositions);
    }

    private MemoryPosition findMemoryPosition(BitHeader<?> header) {
        MemoryPosition memoryPosition = headerMemoryPositions.get(header);
        // TODO move this check up to Table
        assertArgument(memoryPosition != null, "Cannot find Memory Position for header %s", header.name());

        return memoryPosition;
    }

    private void validatePosition(BitHeader<?> header, MemoryPosition memoryPosition) {
        assertArgument(fitsIntoRecord(header, memoryPosition),
                "Memory Position for header %s is incorrect. It will not fit into defined Record Size", header.name());
        assertArgument(fitsInLastBit(header, memoryPosition),
                "Memory Position for header %s is incorrect. Maximum Last Bit for this header is violated", header.name());
    }

    private boolean fitsIntoRecord(BitHeader<?> header, MemoryPosition memoryPosition) {
        int bitsTaken = memoryPosition.bitShift() + header.bitsCount();
        int wordsTaken = (int) Math.ceil((double) bitsTaken / wordSize);
        int lastPositionInRecord = memoryPosition.positionInRecord() + wordsTaken - 1;

        return lastPositionInRecord < recordSize;
    }

    private boolean fitsInLastBit(BitHeader<?> header, MemoryPosition memoryPosition) {
        int bitsTaken = memoryPosition.bitShift() + header.bitsCount();
        int wordsTaken = (int) Math.ceil((double) bitsTaken / wordSize);

        return wordsTaken * wordSize <= header.maxLastBit();
    }
}
