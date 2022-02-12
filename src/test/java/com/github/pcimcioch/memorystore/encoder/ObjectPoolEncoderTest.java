package com.github.pcimcioch.memorystore.encoder;

import com.github.pcimcioch.memorystore.encoder.BitEncoder.Config;
import com.github.pcimcioch.memorystore.store.IntStore;
import com.github.pcimcioch.memorystore.store.ObjectPoolStore;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class ObjectPoolEncoderTest {

    private final ObjectPoolStore<String> store = new ObjectPoolStore<>();

    private final UnsignedIntegerEncoder indexEncoder = new UnsignedIntegerEncoder(new Config(
            new IntStore(),
            1, 0, 0, 31
    ));

    @Test
    void nullStore() {
        // when
        Throwable thrown = catchThrowable(() -> new ObjectPoolEncoder<>(null, indexEncoder));

        // then
        assertThat(thrown).isInstanceOf(NullPointerException.class);
    }

    @Test
    void nullIndexEncoder() {
        // when
        Throwable thrown = catchThrowable(() -> new ObjectPoolEncoder<>(store, null));

        // then
        assertThat(thrown).isInstanceOf(NullPointerException.class);
    }

    @Test
    void setGet() {
        // given
        ObjectPoolEncoder<String> testee = new ObjectPoolEncoder<>(store, indexEncoder);
        store.set("Some");
        store.set("Value");
        store.set("Second");

        // when
        testee.set(0, "First");
        testee.set(1, "Second");

        // then
        assertThat(testee.get(0)).isEqualTo("First");
        assertThat(testee.get(1)).isEqualTo("Second");
    }
}