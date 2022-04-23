package com.github.pcimcioch.memorystore.encoder;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.LongConsumer;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

import static com.github.pcimcioch.memorystore.util.Utils.assertArgument;

// TODO javadocs
// TODO add bit decoder tests
// TODO add to Headers
public class ListEncoder extends BitEncoder {

    public static final int MIN_BIT_COUNT = 1;
    public static final int MAX_BIT_COUNT = 31;
    public static final int MAX_LAST_BIT = 32;

    private final int maxValue;
    private final int mask;
    private final String incorrectValueException;

    public ListEncoder(BitEncoder.Config config) {
        super(config);
        this.maxValue = (1 << this.bitsCount) - 1;

        this.mask = this.maxValue << this.bitShift;
        this.incorrectValueException = String.format("Value must be between [0, %d]", this.maxValue);
    }

    public void init(long elementPosition) {
        setNext(elementPosition, elementPosition);
    }

    public void addNext(long listPosition, long nextElementPosition) {
        long next = next(listPosition);

        setNext(listPosition, nextElementPosition);
        setNext(nextElementPosition, next);
    }

    public void addPrevious(long listPosition, long previousElementPosition) {
        long previous = previous(listPosition);

        setNext(previous, previousElementPosition);
        setNext(previousElementPosition, listPosition);
    }

    public void remove(long listPosition) {
        long next = next(listPosition);
        long previous = previous(listPosition);

        setNext(previous, next);
        setNext(listPosition, listPosition);
    }

    public void merge(long firstListPosition, long secondListPosition) {
        long firstNext = next(firstListPosition);
        long secondPrevious = previous(secondListPosition);

        setNext(firstListPosition, secondListPosition);
        setNext(secondPrevious, firstNext);
    }

    public long next(long listPosition) {
        return getNext(listPosition);
    }

    public long previous(long listPosition) {
        long previous = listPosition;
        for (long i = next(listPosition); i != listPosition; i = next(i)) {
            previous = i;
        }

        return previous;
    }

    public ListIterator iterator(long listPosition) {
        return new ListIterator(listPosition);
    }

    public Iterable<Long> iterable(long listPosition) {
        return () -> iterator(listPosition);
    }

    public LongStream stream(long listPosition) {
        return StreamSupport.longStream(new ListSpliterator(listPosition), false);
    }

    private long getNext(long position) {
        return (store.getInt(storeIndex(position)) & mask) >>> bitShift;
    }

    private void setNext(long position, long value) {
        assertArgument(value >= 0 && value <= maxValue, incorrectValueException);
        store.setPartialInt(storeIndex(position), (int) value << bitShift, mask);
    }

    @Override
    protected int minBits() {
        return MIN_BIT_COUNT;
    }

    @Override
    protected int maxBits() {
        return MAX_BIT_COUNT;
    }

    @Override
    protected int maxLastBit() {
        return MAX_LAST_BIT;
    }

    public final class ListIterator implements Iterator<Long> {

        private long startingPosition;
        private long currentPosition;
        private long nextPosition;

        private ListIterator(long startingPosition) {
            this.startingPosition = startingPosition;
            this.currentPosition = -1;
            this.nextPosition = startingPosition;
        }

        @Override
        public boolean hasNext() {
            return currentPosition == -1 || nextPosition != startingPosition;
        }

        /**
         * @return next value
         * @deprecated If possible, use primitive version {@link #nextLong()}
         */
        @Override
        @Deprecated
        public Long next() {
            return nextLong();
        }

        public long nextLong() {
            currentPosition = nextPosition;
            nextPosition = ListEncoder.this.next(currentPosition);
            return currentPosition;
        }

        @Override
        public void remove() {
            if (currentPosition < 0) {
                throw new IllegalStateException();
            }

            ListEncoder.this.remove(currentPosition);

            if (currentPosition == startingPosition && hasNext()) {
                startingPosition = nextPosition;
                currentPosition = -1;
            }
        }
    }

    public final class ListSpliterator implements Spliterator.OfLong {

        private final long startingPosition;
        private long currentPosition;
        private long nextPosition;

        private ListSpliterator(long startingPosition) {
            this.startingPosition = startingPosition;
            this.currentPosition = -1L;
            this.nextPosition = startingPosition;
        }

        @Override
        public OfLong trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.NONNULL;
        }

        @Override
        public boolean tryAdvance(LongConsumer action) {
            if (currentPosition != -1 && nextPosition == startingPosition) {
                return false;
            }

            currentPosition = nextPosition;
            nextPosition = ListEncoder.this.next(currentPosition);

            action.accept(currentPosition);
            return true;
        }
    }
}
