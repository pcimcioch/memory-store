package com.github.pcimcioch.memorystore.store;

import com.github.pcimcioch.memorystore.BitUtils;

import java.util.Arrays;

import static com.github.pcimcioch.memorystore.BitUtils.assertArgument;

@SuppressWarnings("unchecked")
public class ObjectStore<T> {

    private static final int DEFAULT_BLOCK_SIZE = 131072;

    private final int blockSize;
    private final int numberOfIndexBits;
    private final int indexMask;

    private T[][] blocks = (T[][]) new Object[0][];

    public ObjectStore() {
        this(DEFAULT_BLOCK_SIZE);
    }

    public ObjectStore(int minBlockSize) {
        this.numberOfIndexBits = countBits(minBlockSize);       // 10
        this.blockSize = 1 << this.numberOfIndexBits;           // 1024
        this.indexMask = this.blockSize - 1;                    // 0x000...001111111111
    }

    public void set(long index, T value) {
        ensureSize(index);

        int block = (int) (index >>> numberOfIndexBits);
        int element = (int) (index & indexMask);

        blocks[block][element] = value;
    }

    public T get(long index) {
        int block = (int) (index >>> numberOfIndexBits);
        int element = (int) (index & indexMask);

        return blocks[block][element];
    }

    private void ensureSize(long size) {
        int block = (int) (size >>> numberOfIndexBits);
        if (blocks.length > block) {
            return;
        }

        int currentBlocksSize = blocks.length;
        blocks = Arrays.copyOf(blocks, block + 1);
        for (int i = currentBlocksSize; i < blocks.length; i++) {
            blocks[i] = (T[]) new Object[blockSize];
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

    private static int countBits(int blockSize) {
        assertArgument(blockSize >= 1024 && blockSize <= 33554432, "Block size must be between 1024 and 33554432");
        return BitUtils.countBits(blockSize);
    }
}
