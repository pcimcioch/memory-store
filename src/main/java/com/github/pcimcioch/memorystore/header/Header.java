package com.github.pcimcioch.memorystore.header;

import com.github.pcimcioch.memorystore.encoder.Encoder;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Header is a representation of a data type
 *
 * @param <T> Each header instance is supported by and encoder that encoder given type in the memory store
 */
public abstract class Header<T extends Encoder> {

    private final String name;

    protected Header(String name) {
        this.name = requireNonNull(name);
    }

    /**
     * @return name of the header
     */
    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Header<?> header = (Header<?>) o;
        return name.equals(header.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
