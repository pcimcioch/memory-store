package com.github.pcimcioch.memorystore.layout;

import com.github.pcimcioch.memorystore.header.BitHeader;
import com.github.pcimcioch.memorystore.util.Utils;

import java.util.BitSet;
import java.util.Map;

import static com.github.pcimcioch.memorystore.util.Utils.assertArgument;

/**
 * Custom memory layout that can be predefined by the user. It allows to manually define position in memory layout for each header.
 *
 * Example:
 * <pre>
 *     BitHeader&lt;?&gt; header1 = ...;
 *     BitHeader&lt;?&gt; header2 = ...;
 *     BitHeader&lt;?&gt; header3 = ...;
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
 * In this memory layout, headers can not overlap. Constructor makes sure that all headers will have enough space to
 * store data, and they will not override other header's data.
 *
 * To create memory layout that does  allow overlapping, see {@link OverlappingMemoryLayoutBuilder}
 */
public class NonOverlappingMemoryLayoutBuilder extends CustomMemoryLayoutBuilder {

    /**
     * Build memory layout with default word size of 32 bits
     *
     * @param recordSize number of words per record
     * @param headerMemoryPositions map of headers to their positions in the memory
     */
    public NonOverlappingMemoryLayoutBuilder(int recordSize, Map<? extends BitHeader<?>, MemoryPosition> headerMemoryPositions) {
        this(DEFAULT_WORD_SIZE, recordSize, headerMemoryPositions);
    }

    /**
     * Build memory layout
     *
     * @param wordSize word size in bits
     * @param recordSize number of words per record
     * @param headerMemoryPositions map of headers to their positions in the memory
     */
    public NonOverlappingMemoryLayoutBuilder(int wordSize, int recordSize, Map<? extends BitHeader<?>, MemoryPosition> headerMemoryPositions) {
        super(wordSize, recordSize, headerMemoryPositions);

        validateHeadersNotOverlapping(headerMemoryPositions);
    }

    private void validateHeadersNotOverlapping(Map<? extends BitHeader<?>, MemoryPosition> headerMemoryPositions) {
        OverlapValidator validator = new OverlapValidator(wordSize, recordSize);
        headerMemoryPositions.forEach(validator::registerPosition);
    }

    private static final class OverlapValidator {
        private final int wordSize;
        private final BitSet[] words;

        private OverlapValidator(int wordSize, int recordSize) {
            this.wordSize = wordSize;
            this.words = new BitSet[recordSize];
            for (int i = 0; i < this.words.length; i++) {
                this.words[i] = new BitSet();
            }
        }

        private void registerPosition(BitHeader<?> header, MemoryPosition position) {
            int bitsCount = header.bitsCount();
            int bitShift = position.bitShift();
            for (int positionInRecord = position.positionInRecord(); bitsCount > 0; positionInRecord++) {
                int count = (bitsCount + bitShift) > wordSize ? wordSize - bitShift : bitsCount;
                setBitsTaken(positionInRecord, bitShift, count);
                bitsCount -= count;
                bitShift = 0;
            }
        }

        private void setBitsTaken(int positionInRecord, int bitShift, int bitsCount) {
            for (int i = 0; i < bitsCount; i++) {
                int bit = bitShift + i;
                BitSet word = words[positionInRecord];

                assertArgument(!word.get(bit), "Incorrect configuration. Headers are overlapping");
                word.set(bit);
            }
        }
    }
}
