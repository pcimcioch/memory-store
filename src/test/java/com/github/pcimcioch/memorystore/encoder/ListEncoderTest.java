package com.github.pcimcioch.memorystore.encoder;

import com.github.pcimcioch.memorystore.encoder.ListEncoder.ListIterator;
import com.github.pcimcioch.memorystore.store.IntStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class ListEncoderTest {

    private static final String BITS_COUNT_EX = "Bits Count outside of defined bounds";
    private static final String BIT_SHIFT_EX = "Bit Shift over a limit";

    private final IntStore store = new IntStore();

    @ParameterizedTest
    @MethodSource("incorrectConfigs")
    void incorrectConfig(int bitShift, int bitsCount, String message) {
        // given
        BitEncoder.Config config = new BitEncoder.Config(store, 2, 0, bitShift, bitsCount);

        // when
        Throwable thrown = catchThrowable(() -> new ListEncoder(config));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);
    }

    private static Stream<Arguments> incorrectConfigs() {
        return Stream.of(
                Arguments.of(15, 18, BIT_SHIFT_EX),
                Arguments.of(2, 31, BIT_SHIFT_EX),
                Arguments.of(32, 1, BIT_SHIFT_EX),
                Arguments.of(0, 64, BITS_COUNT_EX),
                Arguments.of(0, 32, BITS_COUNT_EX)
        );
    }

    @ParameterizedTest
    @MethodSource("correctConfigs")
    void correctConfig(int bitShift, int bitsCount) {
        // given
        BitEncoder.Config config = new BitEncoder.Config(store, 2, 0, bitShift, bitsCount);

        // when
        new ListEncoder(config);

        // then
        // no exception thrown
    }

    private static Stream<Arguments> correctConfigs() {
        return Stream.of(
                Arguments.of(15, 17),
                Arguments.of(1, 31),
                Arguments.of(0, 31),
                Arguments.of(31, 1)
        );
    }

    @Test
    void init() {
        // given
        ListEncoder testee = new ListEncoder(new BitEncoder.Config(store, 1, 0, 0, 16));

        // when
        testee.init(0);
        testee.init(1);
        testee.init(2);

        // then
        assertPrevNext(testee, 0, 0, 0);
        assertPrevNext(testee, 1, 1, 1);
        assertPrevNext(testee, 2, 2, 2);
    }

    @Test
    void addNext() {
        // given
        ListEncoder testee = new ListEncoder(new BitEncoder.Config(store, 1, 0, 0, 16));
        testee.init(0);

        // when
        testee.addNext(0, 2);
        testee.addNext(2, 4);
        testee.addNext(2, 3);

        // then
        // 0 -> 2 -> 3 -> 4
        assertPrevNext(testee, 0, 4, 2);
        assertPrevNext(testee, 2, 0, 3);
        assertPrevNext(testee, 3, 2, 4);
        assertPrevNext(testee, 4, 3, 0);
    }

    @Test
    void addPrevious() {
        // given
        ListEncoder testee = new ListEncoder(new BitEncoder.Config(store, 1, 0, 0, 16));
        testee.init(3);

        // when
        testee.addPrevious(3, 2);
        testee.addPrevious(3, 5);
        testee.addPrevious(2, 1);

        // then
        // 1 -> 2 -> 5 -> 3
        assertPrevNext(testee, 1, 3, 2);
        assertPrevNext(testee, 2, 1, 5);
        assertPrevNext(testee, 5, 2, 3);
        assertPrevNext(testee, 3, 5, 1);
    }

    @Test
    void remove() {
        // given
        ListEncoder testee = new ListEncoder(new BitEncoder.Config(store, 1, 0, 0, 16));
        // 0 -> 1 -> 2 -> 3 -> 4
        testee.init(0);
        testee.addNext(0, 1);
        testee.addNext(1, 2);
        testee.addNext(2, 3);
        testee.addNext(3, 4);

        // when
        testee.remove(1);

        // then
        // 0 -> 2 -> 3 -> 4
        assertPrevNext(testee, 0, 4, 2);
        assertPrevNext(testee, 2, 0, 3);
        assertPrevNext(testee, 3, 2, 4);
        assertPrevNext(testee, 4, 3, 0);
        // 1 -> 1
        assertPrevNext(testee, 1, 1, 1);
    }

    @Test
    void remove_singleElementList() {
        // given
        ListEncoder testee = new ListEncoder(new BitEncoder.Config(store, 1, 0, 0, 16));
        testee.init(0);

        // when
        testee.remove(0);

        // then
        assertPrevNext(testee, 0, 0, 0);
    }

    @Test
    void merge_singleElementLists() {
        // given
        ListEncoder testee = new ListEncoder(new BitEncoder.Config(store, 1, 0, 0, 16));
        testee.init(0);
        testee.init(2);

        // when
        testee.merge(0, 2);

        // then
        // 0 -> 2
        assertPrevNext(testee, 0, 2, 2);
        assertPrevNext(testee, 2, 0, 0);
    }

    @Test
    void merge() {
        // given
        ListEncoder testee = new ListEncoder(new BitEncoder.Config(store, 1, 0, 0, 16));
        // 0 -> 1 -> 2
        testee.init(0);
        testee.addNext(0, 1);
        testee.addPrevious(0, 2);
        // 3 -> 4
        testee.init(3);
        testee.addNext(3, 4);

        // when
        testee.merge(1, 4);

        // then
        // 0 -> 1 -> 4 -> 3 -> 2
        assertPrevNext(testee, 0, 2, 1);
        assertPrevNext(testee, 1, 0, 4);
        assertPrevNext(testee, 4, 1, 3);
        assertPrevNext(testee, 3, 4, 2);
        assertPrevNext(testee, 2, 3, 0);
    }

    @Test
    void iterator_singleElementList() {
        // given
        ListEncoder testee = new ListEncoder(new BitEncoder.Config(store, 1, 0, 0, 16));
        testee.init(1);

        // when
        ListIterator iterator = testee.iterator(1);

        // then
        assertThat(iterator).toIterable().containsExactly(1L);
    }

    @Test
    void iterator() {
        // given
        ListEncoder testee = new ListEncoder(new BitEncoder.Config(store, 1, 0, 0, 16));
        // 1 -> 2 -> 3 -> 4 -> 5
        testee.init(1);
        testee.addNext(1, 2);
        testee.addNext(2, 3);
        testee.addNext(3, 4);
        testee.addNext(4, 5);

        // when
        ListIterator iterator1 = testee.iterator(1);
        ListIterator iterator4 = testee.iterator(4);

        // then
        assertThat(toList(iterator1)).containsExactly(1L, 2L, 3L, 4L, 5L);
        assertThat(toList(iterator4)).containsExactly(4L, 5L, 1L, 2L, 3L);
    }

    @Test
    void iterator_remove_singleElementList() {
        // given
        ListEncoder testee = new ListEncoder(new BitEncoder.Config(store, 1, 0, 0, 16));
        testee.init(1);

        // when
        ListIterator iterator = testee.iterator(1);
        while (iterator.hasNext()) {
            long value = iterator.nextLong();
            if (value == 1) {
                iterator.remove();
            }
        }

        // then
        assertPrevNext(testee, 1, 1, 1);
    }

    @Test
    void iterator_removeTwice_singleElementList() {
        // given
        ListEncoder testee = new ListEncoder(new BitEncoder.Config(store, 1, 0, 0, 16));
        testee.init(1);

        // when
        ListIterator iterator = testee.iterator(1);
        while (iterator.hasNext()) {
            long value = iterator.nextLong();
            if (value == 1) {
                iterator.remove();
                iterator.remove();
            }
        }

        // then
        assertPrevNext(testee, 1, 1, 1);
    }

    @Test
    void iterator_removeWithoutNext_singleElementList() {
        // given
        ListEncoder testee = new ListEncoder(new BitEncoder.Config(store, 1, 0, 0, 16));
        testee.init(1);

        // when
        ListIterator iterator = testee.iterator(1);
        Throwable thrown = catchThrowable(iterator::remove);

        // then
        assertThat(thrown).isInstanceOf(IllegalStateException.class);
        assertPrevNext(testee, 1, 1, 1);
    }

    @Test
    void iterator_removeTwice() {
        // given
        ListEncoder testee = new ListEncoder(new BitEncoder.Config(store, 1, 0, 0, 16));
        // 1 -> 2 -> 3
        testee.init(1);
        testee.addNext(1, 2);
        testee.addNext(2, 3);

        // when
        ListIterator iterator = testee.iterator(1);
        while (iterator.hasNext()) {
            long value = iterator.nextLong();
            if (value == 2) {
                iterator.remove();
                iterator.remove();
            }
        }

        // then
        // 1 -> 3
        assertPrevNext(testee, 1, 3, 3);
        assertPrevNext(testee, 3, 1, 1);
        // 2 -> 2
        assertPrevNext(testee, 2, 2, 2);
    }

    @Test
    void iterator_removeFromTheMiddle() {
        // given
        ListEncoder testee = new ListEncoder(new BitEncoder.Config(store, 1, 0, 0, 16));
        // 1 -> 2 -> 3
        testee.init(1);
        testee.addNext(1, 2);
        testee.addNext(2, 3);

        // when
        ListIterator iterator = testee.iterator(1);
        while (iterator.hasNext()) {
            long value = iterator.nextLong();
            if (value == 2) {
                iterator.remove();
            }
        }

        // then
        // 1 -> 3
        assertPrevNext(testee, 1, 3, 3);
        assertPrevNext(testee, 3, 1, 1);
        // 2 -> 2
        assertPrevNext(testee, 2, 2, 2);
    }

    @Test
    void iterator_removeNextToEachOtherInTheMiddle() {
        // given
        ListEncoder testee = new ListEncoder(new BitEncoder.Config(store, 1, 0, 0, 16));
        // 1 -> 2 -> 3 -> 4
        testee.init(1);
        testee.addNext(1, 2);
        testee.addNext(2, 3);
        testee.addNext(3, 4);

        // when
        ListIterator iterator = testee.iterator(1);
        while (iterator.hasNext()) {
            long value = iterator.nextLong();
            if (value == 2 || value == 3) {
                iterator.remove();
            }
        }

        // then
        // 1 -> 4
        assertPrevNext(testee, 1, 4, 4);
        assertPrevNext(testee, 4, 1, 1);
        // 2 -> 2
        assertPrevNext(testee, 2, 2, 2);
        // 3 -> 3
        assertPrevNext(testee, 3, 3, 3);
    }

    @Test
    void iterator_removeAtTheEnd() {
        // given
        ListEncoder testee = new ListEncoder(new BitEncoder.Config(store, 1, 0, 0, 16));
        // 1 -> 2 -> 3 -> 4
        testee.init(1);
        testee.addNext(1, 2);
        testee.addNext(2, 3);
        testee.addNext(3, 4);

        // when
        ListIterator iterator = testee.iterator(1);
        while (iterator.hasNext()) {
            long value = iterator.nextLong();
            if (value == 4) {
                iterator.remove();
            }
        }

        // then
        // 1 -> 2 -> 3
        assertPrevNext(testee, 1, 3, 2);
        assertPrevNext(testee, 2, 1, 3);
        assertPrevNext(testee, 3, 2, 1);
        // 4 -> 4
        assertPrevNext(testee, 4, 4, 4);
    }

    @Test
    void iterator_removeNextToEachOtherAtTheEnd() {
        // given
        ListEncoder testee = new ListEncoder(new BitEncoder.Config(store, 1, 0, 0, 16));
        // 1 -> 2 -> 3 -> 4
        testee.init(1);
        testee.addNext(1, 2);
        testee.addNext(2, 3);
        testee.addNext(3, 4);

        // when
        ListIterator iterator = testee.iterator(1);
        while (iterator.hasNext()) {
            long value = iterator.nextLong();
            if (value == 3 || value == 4) {
                iterator.remove();
            }
        }

        // then
        // 1 -> 2
        assertPrevNext(testee, 1, 2, 2);
        assertPrevNext(testee, 2, 1, 1);
        // 3 -> 3
        assertPrevNext(testee, 3, 3, 3);
        // 4 -> 4
        assertPrevNext(testee, 4, 4, 4);
    }

    @Test
    void iterator_removeAtTheBeginning() {
        // given
        ListEncoder testee = new ListEncoder(new BitEncoder.Config(store, 1, 0, 0, 16));
        // 1 -> 2 -> 3 -> 4
        testee.init(1);
        testee.addNext(1, 2);
        testee.addNext(2, 3);
        testee.addNext(3, 4);

        // when
        ListIterator iterator = testee.iterator(1);
        while (iterator.hasNext()) {
            long value = iterator.nextLong();
            if (value == 1) {
                iterator.remove();
            }
        }

        // then
        // 2 -> 3 -> 4
        assertPrevNext(testee, 2, 4, 3);
        assertPrevNext(testee, 3, 2, 4);
        assertPrevNext(testee, 4, 3, 2);
        // 1 -> 1
        assertPrevNext(testee, 1, 1, 1);
    }

    @Test
    void iterator_removeNextToEachOtherAtTheBeginning() {
        // given
        ListEncoder testee = new ListEncoder(new BitEncoder.Config(store, 1, 0, 0, 16));
        // 1 -> 2 -> 3 -> 4
        testee.init(1);
        testee.addNext(1, 2);
        testee.addNext(2, 3);
        testee.addNext(3, 4);

        // when
        ListIterator iterator = testee.iterator(1);
        while (iterator.hasNext()) {
            long value = iterator.nextLong();
            if (value == 1 || value == 2) {
                iterator.remove();
            }
        }

        // then
        // 3 -> 4
        assertPrevNext(testee, 3, 4, 4);
        assertPrevNext(testee, 4, 3, 3);
        // 1 -> 1
        assertPrevNext(testee, 1, 1, 1);
        // 2 -> 2
        assertPrevNext(testee, 2, 2, 2);
    }

    @Test
    void iterator_removeAll() {
        // given
        ListEncoder testee = new ListEncoder(new BitEncoder.Config(store, 1, 0, 0, 16));
        // 1 -> 2 -> 3
        testee.init(1);
        testee.addNext(1, 2);
        testee.addNext(2, 3);

        // when
        ListIterator iterator = testee.iterator(1);
        while (iterator.hasNext()) {
            iterator.nextLong();
            iterator.remove();
        }

        // then
        assertPrevNext(testee, 1, 1, 1);
        assertPrevNext(testee, 2, 2, 2);
        assertPrevNext(testee, 3, 3, 3);
    }

    @Test
    void iterator_removeDifferent() {
        // given
        ListEncoder testee = new ListEncoder(new BitEncoder.Config(store, 1, 0, 0, 16));
        // 1 -> 2 -> 3 -> 4 -> 5 -> 6 -> 7 -> 8 -> 9
        testee.init(1);
        testee.addNext(1, 2);
        testee.addNext(2, 3);
        testee.addNext(3, 4);
        testee.addNext(4, 5);
        testee.addNext(5, 6);
        testee.addNext(6, 7);
        testee.addNext(7, 8);
        testee.addNext(8, 9);

        // when
        ListIterator iterator = testee.iterator(1);
        while (iterator.hasNext()) {
            long value = iterator.nextLong();
            if (value == 1 || value == 2 || value == 5 || value == 8 || value == 9) {
                iterator.remove();
            }
        }

        // then
        // 3 -> 4 -> 6 -> 7
        assertPrevNext(testee, 3, 7, 4);
        assertPrevNext(testee, 4, 3, 6);
        assertPrevNext(testee, 6, 4, 7);
        assertPrevNext(testee, 7, 6, 3);
        // removed
        assertPrevNext(testee, 1, 1, 1);
        assertPrevNext(testee, 2, 2, 2);
        assertPrevNext(testee, 5, 5, 5);
        assertPrevNext(testee, 8, 8, 8);
        assertPrevNext(testee, 9, 9, 9);
    }

    @Test
    void iterable_singleElementList() {
        // given
        ListEncoder testee = new ListEncoder(new BitEncoder.Config(store, 1, 0, 0, 16));
        testee.init(1);

        // when
        Iterable<Long> iterable = testee.iterable(1);

        // then
        assertThat(iterable).containsExactly(1L);
    }

    @Test
    void iterable() {
        // given
        ListEncoder testee = new ListEncoder(new BitEncoder.Config(store, 1, 0, 0, 16));
        // 1 -> 2 -> 3 -> 4 -> 5
        testee.init(1);
        testee.addNext(1, 2);
        testee.addNext(2, 3);
        testee.addNext(3, 4);
        testee.addNext(4, 5);

        // when
        Iterable<Long> iterable1 = testee.iterable(1);
        Iterable<Long> iterable4 = testee.iterable(4);

        // then
        assertThat(iterable1).containsExactly(1L, 2L, 3L, 4L, 5L);
        assertThat(iterable4).containsExactly(4L, 5L, 1L, 2L, 3L);
    }

    @Test
    void stream_singleElementList() {
        // given
        ListEncoder testee = new ListEncoder(new BitEncoder.Config(store, 1, 0, 0, 16));
        testee.init(1);

        // when
        LongStream stream = testee.stream(1);

        // then
        assertThat(stream).containsExactly(1L);
    }

    @Test
    void stream() {
        // given
        ListEncoder testee = new ListEncoder(new BitEncoder.Config(store, 1, 0, 0, 16));
        // 1 -> 2 -> 3 -> 4
        testee.init(1);
        testee.addNext(1, 2);
        testee.addNext(2, 3);
        testee.addNext(3, 4);

        // when
        LongStream stream = testee.stream(3);

        // then
        assertThat(stream).containsExactly(3L, 4L, 1L, 2L);
    }

    private static void assertPrevNext(ListEncoder testee, int listPosition, int previous, int next) {
        assertThat(testee.previous(listPosition)).isEqualTo(previous);
        assertThat(testee.next(listPosition)).isEqualTo(next);
    }

    private static <T> List<T> toList(Iterator<T> iterator) {
        List<T> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }

        return list;
    }
}