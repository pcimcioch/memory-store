package com.github.pcimcioch.memorystore;

import com.github.pcimcioch.memorystore.encoder.ByteEncoder;
import com.github.pcimcioch.memorystore.encoder.IntEncoder;
import com.github.pcimcioch.memorystore.encoder.ShortEncoder;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.pcimcioch.memorystore.header.Headers.byte8;
import static com.github.pcimcioch.memorystore.header.Headers.int32;
import static com.github.pcimcioch.memorystore.header.Headers.short16;

@BenchmarkMode(Mode.Throughput)
@Measurement(time = 10)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class TableBenchmark {

    private static final int MAX_SIZE = 1024;

    private long counter = 0;

    private final ObjectStore objectStore = new ObjectStore();
    private final TableStore tableStore = new TableStore();

    @Benchmark
    public void objectSet() {
        int index = (int) (counter++ % MAX_SIZE);

        objectStore.setVal1(index, (int) counter);
        objectStore.setVal2(index, (short) counter);
        objectStore.setVal3(index, (byte) counter);
    }

    @Benchmark
    public void tableSet() {
        int index = (int) (counter++ % MAX_SIZE);

        tableStore.setVal1(index, (int) counter);
        tableStore.setVal2(index, (short) counter);
        tableStore.setVal3(index, (byte) counter);
    }

    @Benchmark
    public void objectGet(Blackhole bh) {
        int index = (int) (counter++ % MAX_SIZE);

        bh.consume(objectStore.getVal1(index));
        bh.consume(objectStore.getVal2(index));
        bh.consume(objectStore.getVal3(index));
    }

    @Benchmark
    public void tableGet(Blackhole bh) {
        int index = (int) (counter++ % MAX_SIZE);

        bh.consume(tableStore.getVal1(index));
        bh.consume(tableStore.getVal2(index));
        bh.consume(tableStore.getVal3(index));
    }

    private static final class TableStore {
        private final IntEncoder val1;
        private final ShortEncoder val2;
        private final ByteEncoder val3;

        private TableStore() {
            Table table = new Table(List.of(
                    int32("val1"),
                    short16("val2"),
                    byte8("val3")
            ));

            this.val1 = table.encoderFor(int32("val1"));
            this.val2 = table.encoderFor(short16("val2"));
            this.val3 = table.encoderFor(byte8("val3"));

            for (int i = 0; i < MAX_SIZE; i++) {
                setVal1(i, 0);
                setVal2(i, (short) 0);
                setVal3(i, (byte) 0);
            }
        }

        private int getVal1(int index) {
            return val1.get(index);
        }

        private void setVal1(int index, int val1) {
            this.val1.set(index, val1);
        }

        private short getVal2(int index) {
            return val2.get(index);
        }

        private void setVal2(int index, short val2) {
            this.val2.set(index, val2);
        }

        private byte getVal3(int index) {
            return val3.get(index);
        }

        private void setVal3(int index, byte val3) {
            this.val3.set(index, val3);
        }
    }

    private static final class ObjectStore {
        private final TestObject[] store = new TestObject[MAX_SIZE];

        private ObjectStore() {
            for (int i = 0; i < MAX_SIZE; i++) {
                store[i] = new TestObject();
            }
        }

        private int getVal1(int index) {
            return store[index].getVal1();
        }

        private void setVal1(int index, int val1) {
            store[index].setVal1(val1);
        }

        private short getVal2(int index) {
            return store[index].getVal2();
        }

        private void setVal2(int index, short val2) {
            store[index].setVal2(val2);
        }

        private byte getVal3(int index) {
            return store[index].getVal3();
        }

        private void setVal3(int index, byte val3) {
            store[index].setVal3(val3);
        }
    }

    private static final class TestObject {
        private int val1;
        private short val2;
        private byte val3;

        private int getVal1() {
            return val1;
        }

        private void setVal1(int val1) {
            this.val1 = val1;
        }

        private short getVal2() {
            return val2;
        }

        private void setVal2(short val2) {
            this.val2 = val2;
        }

        private byte getVal3() {
            return val3;
        }

        private void setVal3(byte val3) {
            this.val3 = val3;
        }
    }
}
