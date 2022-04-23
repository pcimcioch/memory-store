package com.github.pcimcioch.memorystore.header;

import com.github.pcimcioch.memorystore.encoder.ObjectPoolEncoder;
import com.github.pcimcioch.memorystore.encoder.UnsignedIntegerEncoder;

import java.util.Objects;

import static com.github.pcimcioch.memorystore.encoder.UnsignedIntegerEncoder.MAX_BIT_COUNT;
import static com.github.pcimcioch.memorystore.encoder.UnsignedIntegerEncoder.MAX_LAST_BIT;
import static com.github.pcimcioch.memorystore.encoder.UnsignedIntegerEncoder.MIN_BIT_COUNT;
import static com.github.pcimcioch.memorystore.util.Utils.assertArgument;
import static java.util.Objects.requireNonNull;

/**
 * ObjectPoolHeaders represent data that is stored in memory as java objects pooled so that equal objects are stored only once.
 * Each record stores the index to individual java object
 *
 * @param <T> ObjectPoolHeaders are supported by ObjectPoolEncoders
 */
public class ObjectPoolHeader<T> extends Header<ObjectPoolEncoder<T>> {

    private static final String POOL_INDEX_SUFFIX = "-index";

    private final PoolDefinition poolDefinition;
    private final BitHeader<UnsignedIntegerEncoder> poolIndexHeader;

    /**
     * Constructor
     *
     * @param name           header name
     * @param poolDefinition definition of object pool
     */
    public ObjectPoolHeader(String name, PoolDefinition poolDefinition) {
        super(name);

        this.poolDefinition = requireNonNull(poolDefinition);
        this.poolIndexHeader = new BitHeader<>(name + POOL_INDEX_SUFFIX, poolDefinition.poolBits, MAX_LAST_BIT, UnsignedIntegerEncoder::new);
    }

    /**
     * BitHeader that is used to store java object index on fixed number of bits
     *
     * @return header to store object index
     */
    public BitHeader<UnsignedIntegerEncoder> poolIndexHeader() {
        return poolIndexHeader;
    }

    /**
     * @return pool definition
     */
    public PoolDefinition poolDefinition() {
        return poolDefinition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ObjectPoolHeader<?> that = (ObjectPoolHeader<?>) o;
        return poolDefinition.equals(that.poolDefinition) && poolIndexHeader.equals(that.poolIndexHeader);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), poolDefinition, poolIndexHeader);
    }

    /**
     * Definition describing how to store java objects in the pool
     */
    public static final class PoolDefinition {
        private final String name;
        private final int poolBits;

        /**
         * Constructor
         *
         * @param name     name of the pool
         * @param poolBits how many bits should be used to store index of the object
         */
        public PoolDefinition(String name, int poolBits) {
            assertArgument(poolBits >= MIN_BIT_COUNT && poolBits <= MAX_BIT_COUNT,
                    "Pool Bits Count must be between %d and %d", MIN_BIT_COUNT, MAX_BIT_COUNT);

            this.name = requireNonNull(name);
            this.poolBits = poolBits;
        }

        /**
         * @return name of the pool
         */
        public String name() {
            return name;
        }

        /**
         * @return how many bits should be used to store index of the object
         */
        public int poolBits() {
            return poolBits;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PoolDefinition that = (PoolDefinition) o;
            return poolBits == that.poolBits && name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, poolBits);
        }
    }
}
