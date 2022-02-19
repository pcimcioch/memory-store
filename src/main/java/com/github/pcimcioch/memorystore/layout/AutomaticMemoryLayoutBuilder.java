package com.github.pcimcioch.memorystore.layout;

import com.github.pcimcioch.memorystore.header.BitHeader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * Automatically creates semi-optimal memory layout based on given headers.
 * The layout is not really optimal, as such problem is NP-complete, but it's pretty good estimation that would be
 * optimal in most of the cases
 */
public class AutomaticMemoryLayoutBuilder implements MemoryLayoutBuilder {

    private Words words;
    private List<BitHeader<?>> headers;

    @Override
    public MemoryLayout compute(int wordSize, Collection<? extends BitHeader<?>> headers) {
        init(wordSize, headers);

        Map<BitHeader<?>, MemoryPosition> memoryPositions = this.headers.stream()
                .collect(toMap(
                        identity(),
                        this::placeIntoBucket
                ));

        return new MemoryLayout(words.size(), memoryPositions);
    }

    private void init(int wordSize, Collection<? extends BitHeader<?>> headers) {
        this.words = new Words(wordSize);
        this.headers = headers.stream()
                .sorted(comparing(BitHeader::bitsCount, reverseOrder()))
                .collect(Collectors.toList());
    }

    private MemoryPosition placeIntoBucket(BitHeader<?> header) {
        for (int i = 0; i <= words.size(); i++) {
            if (fitsInBucket(header, i)) {
                return placeIntoBucket(header, i);
            }
        }

        throw new IllegalArgumentException("Header " + header.name() + " cannot be fitted into memory layout");
    }

    private boolean fitsInBucket(BitHeader<?> header, int bucketIndex) {
        int bitsCount = header.bitsCount();

        for (int lastBit = words.wordSize; lastBit <= header.maxLastBit(); lastBit += words.wordSize, bucketIndex++) {
            int freeBits = words.freeBits(bucketIndex);
            if (freeBits == 0) {
                return false;
            }

            if (bitsCount != header.bitsCount() && !words.isFree(bucketIndex)) {
                return false;
            }

            bitsCount -= freeBits;
            if (bitsCount <= 0) {
                return true;
            }
        }

        return false;
    }

    private MemoryPosition placeIntoBucket(BitHeader<?> header, int bucketIndex) {
        MemoryPosition position = new MemoryPosition(bucketIndex, words.filledBits(bucketIndex));

        for (int bitsCount = header.bitsCount(); bitsCount > 0; bucketIndex++) {
            bitsCount = words.fill(bucketIndex, bitsCount);
        }

        return position;
    }

    private static final class Words {
        private final int wordSize;
        private final List<Integer> words;

        private Words(int wordSize) {
            this.wordSize = wordSize;
            this.words = new ArrayList<>();
        }

        private int size() {
            return words.size();
        }

        private int filledBits(int index) {
            return index < words.size() ? words.get(index) : 0;
        }

        private int freeBits(int index) {
            return wordSize - filledBits(index);
        }

        private boolean isFree(int index) {
            return freeBits(index) == wordSize;
        }

        private int fill(int index, int toAdd) {
            if (index >= words.size()) {
                words.add(0);
            }

            if (freeBits(index) >= toAdd) {
                words.set(index, filledBits(index) + toAdd);
                return 0;
            }

            int rest = wordSize - filledBits(index);
            words.set(index, wordSize);
            return rest;
        }
    }
}
