package com.github.pcimcioch.memorystore.header;

import com.github.pcimcioch.memorystore.encoder.ObjectPoolEncoder;
import com.github.pcimcioch.memorystore.encoder.UnsignedIntegerEncoder;

import java.util.Objects;

import static com.github.pcimcioch.memorystore.util.Utils.assertArgument;
import static com.github.pcimcioch.memorystore.encoder.UnsignedIntegerEncoder.MAX_BIT_COUNT;
import static com.github.pcimcioch.memorystore.encoder.UnsignedIntegerEncoder.MAX_LAST_BIT;
import static com.github.pcimcioch.memorystore.encoder.UnsignedIntegerEncoder.MIN_BIT_COUNT;
import static java.util.Objects.requireNonNull;

public class ObjectPoolHeader<T> extends Header<ObjectPoolEncoder<T>> {

    private static final String POOL_INDEX_SUFFIX = "-index";

    private final PoolDefinition poolDefinition;
    private final BitHeader<UnsignedIntegerEncoder> poolIndexHeader;

    public ObjectPoolHeader(String name, PoolDefinition poolDefinition) {
        super(name);

        this.poolDefinition = requireNonNull(poolDefinition);
        this.poolIndexHeader = new BitHeader<>(name + POOL_INDEX_SUFFIX, poolDefinition.poolBits, MAX_LAST_BIT, UnsignedIntegerEncoder::new);
    }

    public BitHeader<UnsignedIntegerEncoder> poolIndexHeader() {
        return poolIndexHeader;
    }

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

    public static final class PoolDefinition {
        private final String name;
        private final int poolBits;

        public PoolDefinition(String name, int poolBits) {
            assertArgument(poolBits >= MIN_BIT_COUNT && poolBits <= MAX_BIT_COUNT,
                    "Pool Bits Count must be between %d and %d", MIN_BIT_COUNT, MAX_BIT_COUNT);

            this.name = requireNonNull(name);
            this.poolBits = poolBits;
        }

        public String name() {
            return name;
        }

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
