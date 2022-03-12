package com.github.pcimcioch.memorystore.persistence.binary;

import com.github.pcimcioch.memorystore.SerializerTestBase;
import com.github.pcimcioch.memorystore.persistence.binary.model.Entity;
import com.github.pcimcioch.memorystore.persistence.binary.model.Entity.Color;
import com.github.pcimcioch.memorystore.persistence.binary.model.EntityRepository;
import com.github.pcimcioch.memorystore.persistence.binary.model.Point;
import com.github.pcimcioch.memorystore.persistence.binary.model.Preferences;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class BinaryPersistenceTest extends SerializerTestBase {

    private static final Entity ENTITY_1 = new Entity(
            1L,
            (short) 10,
            true,
            Color.BLUE,
            "someName",
            new Point(20.222d, 30.333d),
            new Preferences(true, true, true, true, true)
    );
    private static final Entity ENTITY_2 = new Entity(
            2L,
            (short) 20,
            false,
            Color.GREEN,
            "other name",
            new Point(-23.0d, -787.907d),
            new Preferences(false, false, false, false, false)
    );
    private static final Entity ENTITY_3 = new Entity(
            3L,
            (short) -10,
            true,
            null,
            null,
            null,
            null
    );
    private static final Entity ENTITY_4 = new Entity(
            -10L,
            (short) 40,
            false,
            Color.RED,
            "",
            new Point(Double.MIN_VALUE, Double.MAX_VALUE),
            new Preferences(true, true, true, true, true)
    );

    @Test
    void persistEntities() throws IOException {
        // given
        EntityRepository repository = new EntityRepository();

        repository.save(0, ENTITY_1);
        repository.save(1, ENTITY_2);
        repository.save(2, ENTITY_3);
        repository.save(3, ENTITY_4);

        // when
        repository.saveTable(encoder());
        EntityRepository loadedRepository = new EntityRepository(decoder());

        // then
        assertThat(loadedRepository.load(0)).isEqualTo(ENTITY_1);
        assertThat(loadedRepository.load(1)).isEqualTo(ENTITY_2);
        assertThat(loadedRepository.load(2)).isEqualTo(ENTITY_3);
        assertThat(loadedRepository.load(3)).isEqualTo(ENTITY_4);
    }

}