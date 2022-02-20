package com.github.pcimcioch.serializer.compressed;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Saves integer values in range [-2^63, 2^63-1] in compressed manner. The closer to 0 the value is, the fewer
 * bytes it will take in memory.
 * <br><br>
 *
 * Let's take positive number and represent it in binary format as 63 bits:
 * <pre>
 *     b62, b62, ..., b2, b1, b0
 * </pre>
 * Where b0 is the least significant bit
 * <br><br>
 *
 * If value is negative, then just negate it, so it's positive and represent in the same way. Note it is not "two's
 * complement" that is used in java to represent negative integer numbers.
 * <br><br>
 *
 * Then, if number fits into 6 bits, save it on one byte:
 * <pre>
 *     0, 0, b5, b4, b3, b2, b1, b0
 * </pre>
 * First bit says that there is no more bytes to read. Second bit says it is positive number. For negative numbers
 * it would be set to 1
 * <br><br>
 *
 * If number fits into 13 bits, save it on two bytes:
 * <pre>
 *     1,   0,  b5,  b4, b3, b2, b1, b0
 *     0, b12, b11, b10, b9, b8, b7, b6
 * </pre>
 * First bit in first byte is now 1 indicating that next byte should be read as well.
 * First bit in second byte is 0 indicating that this is the last bit to set.
 * <br><br>
 *
 * For numbers fitting into 20 bits it would be:
 * <pre>
 *      1,   0,  b5,  b4,  b3,  b2,  b1,  b0
 *      1, b12, b11, b10,  b9,  b8,  b7,  b6
 *      0, b19, b18, b17, b16, b15, b14, b13
 * </pre>
 * Etc...
 */
public class CompressedIntegerSerializer {

    private static final int HAS_NEXT_BIT_MASK = 1 << 7;
    private static final int IS_NEGATIVE_BIT_MASK = 1 << 6;

    public void serialize(DataOutput encoder, long value) throws IOException {
        int dataByte = value < 0 ? IS_NEGATIVE_BIT_MASK : 0;
        int availableBits = 6;
        long toSave = value < 0 ? -value : value;

        do {
            dataByte |= getBits(toSave, availableBits);
            toSave >>>= availableBits;

            dataByte = toSave > 0 ? (dataByte | HAS_NEXT_BIT_MASK) : dataByte;

            encoder.writeByte(dataByte);
            dataByte = 0;
            availableBits = 7;
        } while (toSave > 0);
    }

    public long deserialize(DataInput decoder) throws IOException {
        int dataByte;
        int availableBits = 6;
        long result = 0L;
        boolean isNegative = false;
        int usedBits = 0;

        do {
            dataByte = decoder.readByte();
            if (availableBits == 6 && (dataByte & IS_NEGATIVE_BIT_MASK) != 0) {
                isNegative = true;
            }

            result |= (getBits(dataByte, availableBits) << usedBits);

            usedBits += availableBits;
            availableBits = 7;
        } while ((dataByte & HAS_NEXT_BIT_MASK) != 0);

        return isNegative ? -result : result;
    }

    private long getBits(long value, int numberOfBits) {
        long mask = (1L << numberOfBits) - 1;
        return value & mask;
    }
}
