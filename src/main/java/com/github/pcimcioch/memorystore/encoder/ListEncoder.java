package com.github.pcimcioch.memorystore.encoder;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.LongConsumer;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

import static com.github.pcimcioch.memorystore.util.Utils.assertArgument;

/**
 * Allows to connect records in an ordered cyclic list.
 * Elements are kept in order. There is no distinct beginning and end of the list. It is circular.
 * Traversing can start from any element, and it ends when you reach the starting element.
 * Because of that, each element can belong to only one list, and it can't be repeated.
 * <br>
 * <p>
 * It is very important that each new record must be either initialized using {@link #init(long)} method, or added to the
 * existing list using {@link #addNext(long, long)} or {@link #addPrevious(long, long)}
 */
public class ListEncoder extends BitEncoder {

    public static final int MIN_BIT_COUNT = 1;
    public static final int MAX_BIT_COUNT = 31;
    public static final int MAX_LAST_BIT = 32;

    private final int maxValue;
    private final int mask;
    private final String incorrectValueException;

    /**
     * {@inheritDoc}
     */
    public ListEncoder(BitEncoder.Config config) {
        super(config);
        this.maxValue = (1 << this.bitsCount) - 1;

        this.mask = this.maxValue << this.bitShift;
        this.incorrectValueException = String.format("Value must be between [0, %d]", this.maxValue);
    }

    /**
     * Initializes new list. It will be one element list.
     *
     * @param elementPosition position to initialize
     */
    public void init(long elementPosition) {
        setNext(elementPosition, elementPosition);
    }

    /**
     * Adds new element to the list on next position.
     * Element that is added to the list doesn't need to be initialized.
     * Element that is added to the list can't be part of another list. It can only be uninitialized or be in one-element list.
     * If you need to move an element from one list to another, first remove it using {@link #remove(long)}.
     * <br>
     * <p>
     * Adding as next is more efficient than adding as previous using
     * {@link #addPrevious(long, long)}
     * <br>
     * <p>
     * For example, for list:
     * <pre>
     *     0 -&gt; 1 -&gt; 2 -&gt; 3 -&gt; 4 -&gt; 5
     *     addNext(2, 6)
     *     </pre>
     * Will create list:
     * <pre>
     *     0 -&gt; 1 -&gt; 2 -&gt; 6 -&gt; 3 -&gt; 4 -&gt; 5
     * </pre>
     *
     * @param listPosition        position of existing list
     * @param nextElementPosition element to add
     */
    public void addNext(long listPosition, long nextElementPosition) {
        long next = next(listPosition);

        setNext(listPosition, nextElementPosition);
        setNext(nextElementPosition, next);
    }

    /**
     * Adds new element to the list on previous position.
     * Element that is added to the list doesn't need to be initialized.
     * Element that is added to the list can't be part of another list. If you need to move an element from one list to another,
     * first remove it using {@link #remove(long)}.
     * <br>
     * <p>
     * Adding as previous is less efficient than adding as next using
     * {@link #addNext(long, long)}
     * <br>
     * <p>
     * For example, for list:
     * <pre>
     *     0 -&gt; 1 -&gt; 2 -&gt; 3 -&gt; 4 -&gt; 5
     *     addPrevious(2, 6)
     * </pre>
     * Will create list:
     * <pre>
     *     0 -&gt; 1 -&gt; 6 -&gt; 2 -&gt; 3 -&gt; 4 -&gt; 5
     * </pre>
     *
     * @param listPosition            position of existing list
     * @param previousElementPosition element to add
     */
    public void addPrevious(long listPosition, long previousElementPosition) {
        long previous = previous(listPosition);

        setNext(previous, previousElementPosition);
        setNext(previousElementPosition, listPosition);
    }

    /**
     * Removes element from its current list. The removed element will create new, one element list.
     * After element was removed it can be safely added to different list.
     * <br>
     * <p>
     * For example, for list:
     * <pre>
     *     0 -&gt; 1 -&gt; 2 -&gt; 3 -&gt; 4 -&gt; 5
     *     remove(2)
     * </pre>
     * Will create two lists:
     * <pre>
     *     0 -&gt; 1 -&gt; 3 -&gt; 4 -&gt; 5
     *     2
     * </pre>
     *
     * @param listPosition element to remove
     */
    public void remove(long listPosition) {
        long next = next(listPosition);
        long previous = previous(listPosition);

        setNext(previous, next);
        setNext(listPosition, listPosition);
    }

    /**
     * Merges two lists.
     * <br>
     * <p>
     * For example, for lists:
     * <pre>
     *     0 -&gt; 1 -&gt; 2
     *     3 -&gt; 4
     *     merge(1, 3)
     * </pre>
     * Will create one list:
     * <pre>
     *     0 -&gt; 1 -&gt; 3 -&gt; 4 -&gt; 2
     * </pre>
     *
     * @param firstListPosition  first list element
     * @param secondListPosition second list element
     */
    public void merge(long firstListPosition, long secondListPosition) {
        long firstNext = next(firstListPosition);
        long secondPrevious = previous(secondListPosition);

        setNext(firstListPosition, secondListPosition);
        setNext(secondPrevious, firstNext);
    }

    /**
     * Return next element in the list. For one element lists, it will return passed listPosition.
     * For uninitialized elements, behaviour is undefined
     * <br>
     * <p>
     * For example, for lists:
     * <pre>
     *     0 -&gt; 1 -&gt; 2
     *     3
     *
     *     assert next(0) == 1
     *     assert next(1) == 2
     *     assert next(2) == 0
     *     assert next(3) == 3
     * </pre>
     *
     * @param listPosition element position
     * @return next element
     */
    public long next(long listPosition) {
        return getNext(listPosition);
    }

    /**
     * Return previous element in the list. For one element lists, it will return passed listPosition.
     * For uninitialized elements, behaviour is undefined
     * <br>
     * <p>
     * For example, for lists:
     * <pre>
     *     0 -&gt; 1 -&gt; 2
     *     3
     *
     *     assert previous(0) == 2
     *     assert previous(1) == 0
     *     assert previous(2) == 1
     *     assert previous(3) == 3
     * </pre>
     *
     * @param listPosition element position
     * @return next element
     */
    public long previous(long listPosition) {
        long previous = listPosition;
        for (long i = next(listPosition); i != listPosition; i = next(i)) {
            previous = i;
        }

        return previous;
    }

    /**
     * Returns iterator that can be used to traverse whole list. Even though the list is circular, the iterator will stop
     * after traversing all elements.
     * This iterator supports deleting elements from the list. See {@link #remove(long)} for details on what it means to
     * remove an element from the list.
     *
     * @param listPosition position to start iteration
     * @return iterator
     */
    public ListIterator iterator(long listPosition) {
        return new ListIterator(listPosition);
    }

    /**
     * Returns iterable that can be used to traverse whole list. Even though the list is circular, the iterator
     * returned by this iterable will stop after traversing all elements.
     *
     * @param listPosition position to start iteration
     * @return iterable
     */
    public Iterable<Long> iterable(long listPosition) {
        return () -> iterator(listPosition);
    }

    /**
     * Returns stream that can be used to traverse whole list. Even though the list is circular, the stream
     * will stop after traversing all elements.
     *
     * @param listPosition position to start iteration
     * @return stream
     */
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
