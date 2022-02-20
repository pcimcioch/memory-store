package com.github.pcimcioch.memorystore.store;

import com.github.pcimcioch.memorystore.util.Utils;

import java.util.Arrays;

import static com.github.pcimcioch.memorystore.util.Utils.assertArgument;
import static com.github.pcimcioch.memorystore.util.Utils.buildLong;

public class IntStore {

    private static final int DEFAULT_BLOCK_SIZE = 131072; // 128 KB

    private final int blockSize;
    private final int numberOfIndexBits;
    private final int indexMask;

    private int[][] blocks = new int[0][];

    public IntStore() {
        this(DEFAULT_BLOCK_SIZE);
    }

    public IntStore(int minBlockSize) {
        this.numberOfIndexBits = countBits(minBlockSize);       // 10
        this.blockSize = 1 << this.numberOfIndexBits;           // 1024
        this.indexMask = this.blockSize - 1;                    // 0x000...001111111111
    }

    public void setInt(long index, int value) {
        ensureSize(index);

        int block = (int) (index >>> numberOfIndexBits);
        int element = (int) (index & indexMask);

        blocks[block][element] = value;
    }

    public void setPartialInt(long index, int value, int mask) {
        ensureSize(index);

        int block = (int) (index >>> numberOfIndexBits);
        int element = (int) (index & indexMask);

        blocks[block][element] = (value & mask) | (blocks[block][element] & ~mask);
    }

    public void setLong(long index, long value) {
        ensureSize(index + 1);

        int block = (int) (index >>> numberOfIndexBits);
        int element = (int) (index & indexMask);

        int big = (int) (value >> 32);
        int little = (int) value;

        if (element != indexMask) {
            blocks[block][element] = big;
            blocks[block][element + 1] = little;
        } else {
            blocks[block][element] = big;
            blocks[block + 1][0] = little;
        }
    }

    public int getInt(long index) {
        int block = (int) (index >>> numberOfIndexBits);
        int element = (int) (index & indexMask);

        return blocks[block][element];
    }

    public long getLong(long index) {
        int block = (int) (index >>> numberOfIndexBits);
        int element = (int) (index & indexMask);

        return element != indexMask
                ? buildLong(blocks[block][element], blocks[block][element + 1])
                : buildLong(blocks[block][element], blocks[block + 1][0]);
    }

    private void ensureSize(long size) {
        int block = (int) (size >>> numberOfIndexBits);
        if (blocks.length > block) {
            return;
        }

        int currentBlocksSize = blocks.length;
        blocks = Arrays.copyOf(blocks, block + 1);
        for (int i = currentBlocksSize; i < blocks.length; i++) {
            blocks[i] = new int[blockSize];
        }
    }

    public int blockSize() {
        return blockSize;
    }

    public int numberOfIndexBits() {
        return numberOfIndexBits;
    }

    public int indexMask() {
        return indexMask;
    }

    public int blocksCount() {
        return blocks.length;
    }

    public long size() {
        return (long) blockSize * blocks.length;
    }

    private static int countBits(int blockSize) {
        assertArgument(blockSize >= 1024 && blockSize <= 33554432, "Block size must be between 1024 (1KB) and 33554432 (32MB)");
        return Utils.countBits(blockSize);
    }
}
