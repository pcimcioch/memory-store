package com.github.pcimcioch.memorystore.encoder;

import com.github.pcimcioch.memorystore.store.IntStore;

import java.util.Objects;

import static com.github.pcimcioch.memorystore.util.Utils.assertArgument;

/**
 * BitEncoder stores data on fixed number of bits in the memory
 */
public abstract class BitEncoder implements Encoder {

    protected final IntStore store;
    protected final int recordSize;
    protected final int positionInRecord;
    protected final int bitsCount;
    protected final int bitShift;

    /**
     * Constructor
     *
     * @param config configuration describing memory layout
     */
    protected BitEncoder(Config config) {
        assertArgument(config.bitsCount() >= minBits() && config.bitsCount() <= maxBits(), "Bits Count outside of defined bounds");
        assertArgument(config.bitShift() + config.bitsCount() <= maxLastBit(), "Bit Shift over a limit");

        this.store = config.store();
        this.recordSize = config.recordSize();
        this.positionInRecord = config.positionInRecord();
        this.bitsCount = config.bitsCount();
        this.bitShift = config.bitShift();
    }

    /**
     * Size of the whole record in bytes
     *
     * @return record size
     */
    public int recordSize() {
        return recordSize;
    }

    /**
     * Position of the first byte in the record belonging to this encoder. 0-indexed
     *
     * @return position of the first byte in the record
     */
    public int positionInRecord() {
        return positionInRecord;
    }

    /**
     * How many bits are occupied by this encoder to store data
     *
     * @return bits count
     */
    public int bitsCount() {
        return bitsCount;
    }

    /**
     * Position of the first bit in the first byte belonging to this encoder. 0-indexed
     *
     * @return position of the first bit in the first byte
     */
    public int bitShift() {
        return bitShift;
    }

    protected long storeIndex(long position) {
        return position * recordSize + positionInRecord;
    }

    protected abstract int minBits();

    protected abstract int maxBits();

    protected abstract int maxLastBit();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BitEncoder that = (BitEncoder) o;
        return recordSize == that.recordSize && positionInRecord == that.positionInRecord && bitsCount == that.bitsCount && bitShift == that.bitShift && store.equals(that.store);
    }

    @Override
    public int hashCode() {
        return Objects.hash(store, recordSize, positionInRecord, bitsCount, bitShift);
    }

    /**
     * Let's say our full record contains four bytes and our data is located on second byte on bits indexed [12, 16]
     * <pre>
     *      ||&lt;-- 1 byte --&gt;||&lt;-- 12 bits --&gt;&lt;-- 5 bits of our data --&gt;&lt;-- 15 bits --&gt;||&lt;-- 1 byte --&gt;||&lt;-- 1 byte --&gt;||
     *
     *      recordSize = 4
     *      positionInRecord = 1
     *      bitShift = 12
     *      bitsCount = 5
     * </pre>
     */
    public static final class Config {
        private final IntStore store;
        private final int recordSize;
        private final int positionInRecord;
        private final int bitShift;
        private final int bitsCount;

        /**
         * Constructor
         *
         * @param store            where data is stored
         * @param recordSize       size of the whole record in bytes
         * @param positionInRecord position of the first byte in the record belonging to this encoder. 0-indexed
         * @param bitShift         position of the first bit in the first byte belonging to this encoder. 0-indexed
         * @param bitsCount        how many bits are occupied by this encoder to store data
         */
        public Config(IntStore store, int recordSize, int positionInRecord, int bitShift, int bitsCount) {
            assertArgument(recordSize > 0, "Record size must be greater then 0");
            assertArgument(positionInRecord >= 0, "Position in record must be greater or equal zero");
            assertArgument(bitShift >= 0, "Bit Shift must be greater or equal zero");
            assertArgument(bitsCount > 0, "Bits Count must be greater or equal zero");
            assertArgument(positionInRecord * 32 + bitShift + bitsCount <= recordSize * 32, "Configured data will not fit into Record Size");

            this.store = Objects.requireNonNull(store, "Store cannot be null");
            this.recordSize = recordSize;
            this.positionInRecord = positionInRecord;
            this.bitShift = bitShift;
            this.bitsCount = bitsCount;
        }

        /**
         * @return where data is stored
         */
        public IntStore store() {
            return store;
        }

        /**
         * Size of the whole record in bytes
         *
         * @return record size
         */
        public int recordSize() {
            return recordSize;
        }

        /**
         * Position of the first byte in the record belonging to this encoder. 0-indexed
         *
         * @return position of the first byte in the record
         */
        public int positionInRecord() {
            return positionInRecord;
        }

        /**
         * Position of the first bit in the first byte belonging to this encoder. 0-indexed
         *
         * @return position of the first bit in the first byte
         */
        public int bitShift() {
            return bitShift;
        }

        /**
         * How many bits are occupied by this encoder to store data
         *
         * @return bits count
         */
        public int bitsCount() {
            return bitsCount;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Config config = (Config) o;
            return recordSize == config.recordSize && positionInRecord == config.positionInRecord && bitShift == config.bitShift && bitsCount == config.bitsCount && store.equals(config.store);
        }

        @Override
        public int hashCode() {
            return Objects.hash(store, recordSize, positionInRecord, bitShift, bitsCount);
        }
    }
}
