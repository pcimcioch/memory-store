package com.github.pcimcioch.memorystore;

import com.github.pcimcioch.memorystore.encoder.BooleanEncoder;
import com.github.pcimcioch.memorystore.encoder.ByteEncoder;
import com.github.pcimcioch.memorystore.encoder.Encoder;
import com.github.pcimcioch.memorystore.encoder.IntEncoder;
import com.github.pcimcioch.memorystore.encoder.ObjectDirectEncoder;
import com.github.pcimcioch.memorystore.encoder.ObjectPoolEncoder;
import com.github.pcimcioch.memorystore.encoder.ShortEncoder;
import com.github.pcimcioch.memorystore.header.Header;
import com.github.pcimcioch.memorystore.header.ObjectDirectHeader;
import com.github.pcimcioch.memorystore.header.ObjectPoolHeader;
import com.github.pcimcioch.memorystore.layout.MemoryLayoutBuilder;
import com.github.pcimcioch.memorystore.layout.MemoryLayoutBuilder.MemoryPosition;
import com.github.pcimcioch.memorystore.layout.NonOverlappingMemoryLayoutBuilder;
import com.github.pcimcioch.memorystore.store.IntStore;
import com.github.pcimcioch.memorystore.store.ObjectPoolStore;
import com.github.pcimcioch.memorystore.store.ObjectStore;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.github.pcimcioch.memorystore.header.Headers.bool;
import static com.github.pcimcioch.memorystore.header.Headers.byte8;
import static com.github.pcimcioch.memorystore.header.Headers.char16;
import static com.github.pcimcioch.memorystore.header.Headers.int32;
import static com.github.pcimcioch.memorystore.header.Headers.long64;
import static com.github.pcimcioch.memorystore.header.Headers.object;
import static com.github.pcimcioch.memorystore.header.Headers.objectPool;
import static com.github.pcimcioch.memorystore.header.Headers.poolOfSize;
import static com.github.pcimcioch.memorystore.header.Headers.poolOnBits;
import static com.github.pcimcioch.memorystore.header.Headers.short16;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class TableTest {

    @Test
    void tableStoreHeaders() {
        // when
        Table testee = new Table(List.of(
                int32("header1"),
                byte8("header2"),
                char16("header3"),
                bool("header4")
        ));

        // then
        assertHeaders(testee,
                int32("header1"),
                byte8("header2"),
                char16("header3"),
                bool("header4")
        );
        assertThat(testee.encoderFor(long64("missing"))).isNull();
    }

    @Test
    void createTableWithAutomaticMemoryLayout() {
        // given
        Table testee = new Table(List.of(
                int32("header1"),
                byte8("header2"),
                short16("header3"),
                bool("header4")
        ));
        IntEncoder intEncoder = testee.encoderFor(int32("header1"));
        ByteEncoder byteEncoder = testee.encoderFor(byte8("header2"));
        ShortEncoder shortEncoder = testee.encoderFor(short16("header3"));
        BooleanEncoder boolEncoder = testee.encoderFor(bool("header4"));

        // when
        // 0th record
        intEncoder.set(0, 12345);
        shortEncoder.set(0, (short) 1234);
        byteEncoder.set(0, (byte) 12);
        boolEncoder.set(0, true);

        // 1st record
        intEncoder.set(1, 54321);
        shortEncoder.set(1, (short) 4321);
        byteEncoder.set(1, (byte) 21);
        boolEncoder.set(1, false);

        // then
        assertIntStore(testee,
                // 0th record
                12345,
                1234 | (12 << 16) | (1 << 24),
                // 1st record
                54321,
                4321 | (21 << 16),
                // 2nd record empty
                0,
                0
        );

        assertThat(intEncoder.get(0)).isEqualTo(12345);
        assertThat(shortEncoder.get(0)).isEqualTo((short) 1234);
        assertThat(byteEncoder.get(0)).isEqualTo((byte) 12);
        assertThat(boolEncoder.get(0)).isTrue();

        assertThat(intEncoder.get(1)).isEqualTo(54321);
        assertThat(shortEncoder.get(1)).isEqualTo((short) 4321);
        assertThat(byteEncoder.get(1)).isEqualTo((byte) 21);
        assertThat(boolEncoder.get(1)).isFalse();
    }

    @Test
    void createTableWithCustomMemoryLayout() {
        // given
        MemoryLayoutBuilder builder = new NonOverlappingMemoryLayoutBuilder(32, 3, Map.of(
                int32("header1"), new MemoryPosition(1, 0),
                byte8("header2"), new MemoryPosition(0, 0),
                short16("header3"), new MemoryPosition(0, 10),
                bool("header4"), new MemoryPosition(2, 1)
        ));

        Table testee = new Table(builder, List.of(
                int32("header1"),
                byte8("header2"),
                short16("header3"),
                bool("header4")
        ));
        IntEncoder intEncoder = testee.encoderFor(int32("header1"));
        ByteEncoder byteEncoder = testee.encoderFor(byte8("header2"));
        ShortEncoder shortEncoder = testee.encoderFor(short16("header3"));
        BooleanEncoder boolEncoder = testee.encoderFor(bool("header4"));

        // when
        // 0th record
        intEncoder.set(0, 12345);
        shortEncoder.set(0, (short) 1234);
        byteEncoder.set(0, (byte) 12);
        boolEncoder.set(0, true);

        // 1st record
        intEncoder.set(1, 54321);
        shortEncoder.set(1, (short) 4321);
        byteEncoder.set(1, (byte) 21);
        boolEncoder.set(1, false);

        // then
        assertIntStore(testee,
                // 0th record
                12 | (1234 << 10),
                12345,
                1 << 1,
                // 1st record
                21 | (4321 << 10),
                54321,
                0,
                // 2nd record empty
                0,
                0,
                0
        );

        assertThat(intEncoder.get(0)).isEqualTo(12345);
        assertThat(shortEncoder.get(0)).isEqualTo((short) 1234);
        assertThat(byteEncoder.get(0)).isEqualTo((byte) 12);
        assertThat(boolEncoder.get(0)).isTrue();

        assertThat(intEncoder.get(1)).isEqualTo(54321);
        assertThat(shortEncoder.get(1)).isEqualTo((short) 4321);
        assertThat(byteEncoder.get(1)).isEqualTo((byte) 21);
        assertThat(boolEncoder.get(1)).isFalse();
    }

    @Test
    void createObjectStores() {
        // given
        Table testee = new Table(List.of(
                object("header1"),
                object("header2")
        ));

        ObjectDirectEncoder<TestObject> obj1Encoder = testee.encoderFor(object("header1"));
        ObjectDirectEncoder<TestObject> obj2Encoder = testee.encoderFor(object("header2"));

        TestObject val1 = new TestObject(1);
        TestObject val2 = new TestObject(2);
        TestObject val3 = new TestObject(1);
        TestObject val4 = new TestObject(10);

        // when
        obj1Encoder.set(0, val1);
        obj1Encoder.set(1, val2);
        obj1Encoder.set(2, val3);

        obj2Encoder.set(1, val4);
        obj2Encoder.set(2, val3);

        // then
        assertObjectStore(testee, object("header1"), val1, val2, val3, null);
        assertObjectStore(testee, object("header2"), null, val4, val3, null);

        assertThat(obj1Encoder.get(0)).isSameAs(val1);
        assertThat(obj1Encoder.get(1)).isSameAs(val2);
        assertThat(obj1Encoder.get(2)).isSameAs(val3);

        assertThat(obj2Encoder.get(1)).isSameAs(val4);
        assertThat(obj2Encoder.get(2)).isSameAs(val3);
    }

    @Test
    void createObjectPoolStores() {
        // given
        ObjectPoolHeader<TestObject> header1 = objectPool("header1", poolOfSize("pool1", 10));
        ObjectPoolHeader<TestObject> header2 = objectPool("header2", poolOnBits("pool2", 4));

        Table testee = new Table(List.of(
                header1,
                header2
        ));

        ObjectPoolEncoder<TestObject> obj1Encoder = testee.encoderFor(header1);
        ObjectPoolEncoder<TestObject> obj2Encoder = testee.encoderFor(header2);

        TestObject val1 = new TestObject(1);
        TestObject val2 = new TestObject(2);
        TestObject val3 = new TestObject(1);
        TestObject val4 = new TestObject(10);

        // when
        obj1Encoder.set(0, val1);
        obj1Encoder.set(1, val2);
        obj1Encoder.set(2, val3);

        obj2Encoder.set(1, val4);
        obj2Encoder.set(2, val3);

        // then
        assertObjectPoolStore(testee, header1, val1, val2);
        assertObjectPoolStore(testee, header2, val4, val3);

        assertThat(obj1Encoder.get(0)).isSameAs(val1);
        assertThat(obj1Encoder.get(1)).isSameAs(val2);
        assertThat(obj1Encoder.get(2)).isSameAs(val1);

        assertThat(obj2Encoder.get(1)).isSameAs(val4);
        assertThat(obj2Encoder.get(2)).isSameAs(val3);
    }

    @Test
    void duplicatePoolName() {
        // when
        Throwable thrown = catchThrowable(() -> new Table(List.of(
                objectPool("header1", poolOnBits("pool", 4)),
                objectPool("header2", poolOnBits("pool", 3))
        )));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Duplicated pool name pool");
    }

    @Test
    void duplicateEncoderName() {
        // when
        Throwable thrown = catchThrowable(() -> new Table(List.of(
                int32("header"),
                long64("header")
        )));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Duplicated header name header");
    }

    @SafeVarargs
    private static void assertHeaders(Table table, Header<? extends Encoder>... headers) {
        assertThat(table.headers()).isEqualTo(Set.of(headers));
        for (Header<? extends Encoder> header : headers) {
            assertThat(table.encoderFor(header)).isNotNull();
        }
    }

    private static void assertIntStore(Table table, int... values) {
        IntStore store = table.intStore();
        for (int i = 0; i < values.length; i++) {
            assertThat(store.getInt(i)).isEqualTo(values[i]);
        }
    }

    private static void assertObjectStore(Table testee, ObjectDirectHeader<?> header, Object... values) {
        ObjectStore<?> store = testee.objectStores().get(header);
        for (int i = 0; i < values.length; i++) {
            assertThat(store.get(i)).isSameAs(values[i]);
        }
    }

    private static void assertObjectPoolStore(Table testee, ObjectPoolHeader<?> header, Object... values) {
        ObjectPoolStore<?> store = testee.objectPoolStores().get(header.poolDefinition());
        assertThat(store.elementsCount()).isEqualTo(values.length);
        for (int i = 0; i < values.length; i++) {
            assertThat(store.get(i)).isSameAs(values[i]);
        }
    }

    private static final class TestObject {
        private final int value;

        private TestObject(int value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestObject that = (TestObject) o;
            return value == that.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }
}