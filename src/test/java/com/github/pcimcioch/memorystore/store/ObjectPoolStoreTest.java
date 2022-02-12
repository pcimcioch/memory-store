package com.github.pcimcioch.memorystore.store;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class ObjectPoolStoreTest {

    private final ObjectPoolStore<String> testee = new ObjectPoolStore<>();

    @Test
    void emptyStoreSize() {
        // when then
        assertThat(testee.elementsCount()).isZero();
    }

    @Test
    void emptyStoreGet() {
        // when
        Throwable thrown = catchThrowable(() -> testee.get(0));

        // then
        assertThat(thrown).isInstanceOf(IndexOutOfBoundsException.class);
        assertThat(testee.elementsCount()).isZero();
    }

    @Test
    void setAndGet() {
        // when set
        assertThat(testee.set("First")).isEqualTo(0);
        assertThat(testee.set("Second")).isEqualTo(1);
        assertThat(testee.set("Third")).isEqualTo(2);

        // when get
        assertThat(testee.get(0)).isEqualTo("First");
        assertThat(testee.get(1)).isEqualTo("Second");
        assertThat(testee.get(2)).isEqualTo("Third");

        assertThat(testee.elementsCount()).isEqualTo(3);
    }

    @Test
    void setPools() {
        // when set
        assertThat(testee.set("First")).isEqualTo(0);
        assertThat(testee.set("first")).isEqualTo(1);
        assertThat(testee.set("First")).isEqualTo(0);

        // when get
        assertThat(testee.get(0)).isEqualTo("First");
        assertThat(testee.get(1)).isEqualTo("first");

        assertThat(testee.elementsCount()).isEqualTo(2);
    }
}