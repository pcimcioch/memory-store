package com.github.pcimcioch.memorystore.header;

import com.github.pcimcioch.memorystore.encoder.BitEncoder;

import java.util.Objects;
import java.util.function.Function;

import static com.github.pcimcioch.memorystore.util.Utils.assertArgument;
import static java.util.Objects.requireNonNull;

/**
 * BitHeaders represent data types that can be stored on fixed number of bits in the memory
 *
 * @param <T> BitHeaders are supported by BitEncoders that know how to encode and decode data on the bits
 */
public class BitHeader<T extends BitEncoder> extends Header<T> {

    private final int bitsCount;
    private final int maxLastBit;
    private final Function<BitEncoder.Config, T> encoderFactory;

    /**
     * Constructor
     *
     * @param name           name of the header
     * @param bitsCount      how many bits are used to store the data
     * @param maxLastBit     defined by underlying BitEncoder
     * @param encoderFactory how to create encoder supporting this header
     */
    public BitHeader(String name, int bitsCount, int maxLastBit, Function<BitEncoder.Config, T> encoderFactory) {
        super(name);

        assertArgument(bitsCount > 0, "Bits Count must be greater or equal zero");
        assertArgument(maxLastBit >= bitsCount, "Max Last Bit must not be less then Bits Count");

        this.bitsCount = bitsCount;
        this.maxLastBit = maxLastBit;
        this.encoderFactory = requireNonNull(encoderFactory, "Encoder Factory cannot be null");
    }

    /**
     * @return how many bits are used to store the data
     */
    public int bitsCount() {
        return bitsCount;
    }

    /**
     * @return value defined by underlying BitEncoder
     */
    public int maxLastBit() {
        return maxLastBit;
    }

    /**
     * @return how to create encoder supporting this header
     */
    public Function<BitEncoder.Config, T> encoderFactory() {
        return encoderFactory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BitHeader<?> bitHeader = (BitHeader<?>) o;
        return bitsCount == bitHeader.bitsCount && maxLastBit == bitHeader.maxLastBit && encoderFactory.equals(bitHeader.encoderFactory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), bitsCount, maxLastBit, encoderFactory);
    }
}
