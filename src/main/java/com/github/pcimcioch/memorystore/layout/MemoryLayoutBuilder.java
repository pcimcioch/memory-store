package com.github.pcimcioch.memorystore.layout;

import com.github.pcimcioch.memorystore.header.BitHeader;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Used to determine how the static size data will be stored in the memory
 */
public interface MemoryLayoutBuilder {

    /**
     * Builds memory layout based on the passed headers
     *
     * @param wordSize size of the word
     * @param headers  headers
     * @return memory layout
     */
    MemoryLayout compute(int wordSize, Collection<? extends BitHeader<?>> headers);

    /**
     * Used to define position in the memory of multiple headers
     */
    final class MemoryLayout {
        private final int recordSize;
        private final Map<BitHeader<?>, MemoryPosition> memoryPositions;

        /**
         * Constructs memory layout
         *
         * @param recordSize      number of words in one record
         * @param memoryPositions header to its memory position map
         */
        public MemoryLayout(int recordSize, Map<BitHeader<?>, MemoryPosition> memoryPositions) {
            this.recordSize = recordSize;
            this.memoryPositions = new HashMap<>(memoryPositions);
        }

        /**
         * Number of words in one record
         *
         * @return record size
         */
        public int recordSize() {
            return recordSize;
        }

        /**
         * The memory position for given header
         *
         * @param header header
         * @return memory position or null if given header does not belong to the layout
         */
        public MemoryPosition memoryPositionFor(BitHeader<?> header) {
            return memoryPositions.get(header);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MemoryLayout that = (MemoryLayout) o;
            return recordSize == that.recordSize && memoryPositions.equals(that.memoryPositions);
        }

        @Override
        public int hashCode() {
            return Objects.hash(recordSize, memoryPositions);
        }
    }

    /**
     * Position in the memory
     */
    final class MemoryPosition {
        private final int positionInRecord;
        private final int bitShift;

        /**
         * Constructs memory position
         *
         * @param positionInRecord first word in the record occupied by the data. Indexed form 0
         * @param bitShift         first bit in the first word in the record occupied by the data. Indexed form 0
         */
        public MemoryPosition(int positionInRecord, int bitShift) {
            this.positionInRecord = positionInRecord;
            this.bitShift = bitShift;
        }

        /**
         * First word in the record occupied by the data. Indexed form 0
         *
         * @return position in record
         */
        public int positionInRecord() {
            return positionInRecord;
        }

        /**
         * First bit in the first word in the record occupied by the data. Indexed form 0. If data takes more than one
         * word in the record, all following records are occupied from 0th bit
         *
         * @return bit shift
         */
        public int bitShift() {
            return bitShift;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MemoryPosition that = (MemoryPosition) o;
            return positionInRecord == that.positionInRecord && bitShift == that.bitShift;
        }

        @Override
        public int hashCode() {
            return Objects.hash(positionInRecord, bitShift);
        }
    }
}
