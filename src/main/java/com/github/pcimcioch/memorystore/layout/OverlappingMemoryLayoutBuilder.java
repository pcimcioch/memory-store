package com.github.pcimcioch.memorystore.layout;

import com.github.pcimcioch.memorystore.header.BitHeader;

import java.util.Map;

/**
 * Custom memory layout that can be predefined by the user. It allows to manually define position in memory layout for each header.
 *
 * Example:
 * <pre>
 *     BitHeader<?> header1 = ...;
 *     BitHeader<?> header2 = ...;
 *     BitHeader<?> header3 = ...;
 *
 *     OverlappingMemoryLayoutBuilder builder = new OverlappingMemoryLayoutBuilder(32, 2, Map.of(
 *         header1, new MemoryPosition(0, 0),
 *         header2, new MemoryPosition(1, 8),
 *         header3, new MemoryPosition(1, 24)
 *     ));
 * </pre>
 *
 * This will configure MemoryLayout with 32-bit word size and 2 words per record. <br>
 * {@code header1} will be located at 0th word in the record, at 0th bit <br>
 * {@code header2} will be located at 1st word in the record, at 8th bit <br>
 * {@code header3} will be located at 1st word in the record, at 24th bit <br>
 *
 * In this memory layout, headers can overlap. So it is possible to store different data in the same memory.
 * It can be used to create union types, where memory can hold multiple different types, but only one of them is used at
 * one time.
 *
 * Example:
 * <pre>
 *     BitHeader<?> header1 = ...;
 *     BitHeader<?> header2 = ...;
 *
 *     OverlappingMemoryLayoutBuilder builder = new OverlappingMemoryLayoutBuilder(32, 1, Map.of(
 *         header1, new MemoryPosition(0, 0),
 *         header2, new MemoryPosition(0, 0)
 *     ));
 * </pre>
 *
 * To create memory layout that does not allow overlapping, see {@link NonOverlappingMemoryLayoutBuilder}
 */
public class OverlappingMemoryLayoutBuilder extends CustomMemoryLayoutBuilder {

    /**
     * Build memory layout with default word size of 32 bits
     *
     * @param recordSize number of words per record
     * @param headerMemoryPositions map of headers to their positions in the memory
     */
    public OverlappingMemoryLayoutBuilder(int recordSize, Map<? extends BitHeader<?>, MemoryPosition> headerMemoryPositions) {
        super(recordSize, headerMemoryPositions);
    }

    /**
     * Build memory layout
     *
     * @param wordSize word size in bits
     * @param recordSize number of words per record
     * @param headerMemoryPositions map of headers to their positions in the memory
     */
    public OverlappingMemoryLayoutBuilder(int wordSize, int recordSize, Map<? extends BitHeader<?>, MemoryPosition> headerMemoryPositions) {
        super(wordSize, recordSize, headerMemoryPositions);
    }
}
