package com.github.pcimcioch.memorystore.encoder;

import com.github.pcimcioch.memorystore.store.ObjectStore;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class ObjectDirectEncoderTest {

    private final ObjectStore<String> store = new ObjectStore<>();

    @Test
    void nullStore() {
        // when
        Throwable thrown = catchThrowable(() -> new ObjectDirectEncoder<>(null));

        // then
        assertThat(thrown).isInstanceOf(NullPointerException.class);
    }

    @Test
    void setGet() {
        // given
        ObjectDirectEncoder<String> testee = new ObjectDirectEncoder<String>(store);

        // when
        store.set(0, "First");
        store.set(1, "Second");

        // then
        assertThat(store.get(0)).isEqualTo("First");
        assertThat(store.get(1)).isEqualTo("Second");
    }

}