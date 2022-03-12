package com.github.pcimcioch.memorystore.persistence.binary.model;

import com.github.pcimcioch.memorystore.Table;
import com.github.pcimcioch.memorystore.encoder.BooleanEncoder;
import com.github.pcimcioch.memorystore.encoder.Encoder;
import com.github.pcimcioch.memorystore.encoder.EnumEncoder;
import com.github.pcimcioch.memorystore.encoder.LongEncoder;
import com.github.pcimcioch.memorystore.encoder.ObjectDirectEncoder;
import com.github.pcimcioch.memorystore.encoder.ObjectPoolEncoder;
import com.github.pcimcioch.memorystore.encoder.ShortEncoder;
import com.github.pcimcioch.memorystore.header.BitHeader;
import com.github.pcimcioch.memorystore.header.Header;
import com.github.pcimcioch.memorystore.header.ObjectDirectHeader;
import com.github.pcimcioch.memorystore.header.ObjectPoolHeader;
import com.github.pcimcioch.memorystore.persistence.binary.BinaryPersistence;
import com.github.pcimcioch.memorystore.persistence.binary.model.Entity.Color;
import com.github.pcimcioch.memorystore.persistence.binary.model.Point.PointSerializer;
import com.github.pcimcioch.memorystore.persistence.binary.model.Preferences.PreferencesSerializer;
import com.github.pcimcioch.serializer.Serializers;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import static com.github.pcimcioch.memorystore.header.Headers.bool;
import static com.github.pcimcioch.memorystore.header.Headers.long64;
import static com.github.pcimcioch.memorystore.header.Headers.nullableEnumType;
import static com.github.pcimcioch.memorystore.header.Headers.object;
import static com.github.pcimcioch.memorystore.header.Headers.objectPool;
import static com.github.pcimcioch.memorystore.header.Headers.poolOnBits;
import static com.github.pcimcioch.memorystore.header.Headers.short16;

public class EntityRepository {

    private final static ObjectPoolHeader.PoolDefinition PREFERENCES_POOL = poolOnBits("preferences", 5);

    private final static BitHeader<LongEncoder> ID_HEADER = long64("id");
    private final static BitHeader<ShortEncoder> SIZE_HEADER = short16("size");
    private final static BitHeader<BooleanEncoder> ACCESSIBLE_HEADER = bool("accessible");
    private final static BitHeader<EnumEncoder<Color>> COLOR_HEADER = nullableEnumType("color", Color.class);
    private final static ObjectDirectHeader<String> NAME_HEADER = object("name");
    private final static ObjectDirectHeader<Point> LOCATION_HEADER = object("location");
    private final static ObjectPoolHeader<Preferences> PREFERENCES_HEADER = objectPool("preferences", PREFERENCES_POOL);

    private final static List<Header<? extends Encoder>> ALL_HEADERS = List.of(
            ID_HEADER,
            SIZE_HEADER,
            ACCESSIBLE_HEADER,
            COLOR_HEADER,
            NAME_HEADER,
            LOCATION_HEADER,
            PREFERENCES_HEADER
    );

    private final static BinaryPersistence PERSISTENCE = BinaryPersistence.builder()
            .registerSerializer(NAME_HEADER, Serializers.string())
            .registerSerializer(LOCATION_HEADER, new PointSerializer())
            .registerSerializer(PREFERENCES_POOL, new PreferencesSerializer())
            .build();

    private final Table table;

    private final LongEncoder idEncoder;
    private final ShortEncoder sizeEncoder;
    private final BooleanEncoder accessibleEncoder;
    private final EnumEncoder<Color> colorEncoder;
    private final ObjectDirectEncoder<String> nameEncoder;
    private final ObjectDirectEncoder<Point> locationEncoder;
    private final ObjectPoolEncoder<Preferences> preferencesEncoder;

    public EntityRepository() {
        this(new Table(ALL_HEADERS));
    }

    public EntityRepository(DataInput stream) throws IOException {
        this(PERSISTENCE.load(stream, ALL_HEADERS));
    }

    private EntityRepository(Table table) {
        this.table = table;

        this.idEncoder = table.encoderFor(ID_HEADER);
        this.sizeEncoder = table.encoderFor(SIZE_HEADER);
        this.accessibleEncoder = table.encoderFor(ACCESSIBLE_HEADER);
        this.colorEncoder = table.encoderFor(COLOR_HEADER);
        this.nameEncoder = table.encoderFor(NAME_HEADER);
        this.locationEncoder = table.encoderFor(LOCATION_HEADER);
        this.preferencesEncoder = table.encoderFor(PREFERENCES_HEADER);
    }

    public void save(int index, Entity entity) {
        idEncoder.set(index, entity.id());
        sizeEncoder.set(index, entity.size());
        accessibleEncoder.set(index, entity.accessible());
        colorEncoder.set(index, entity.color());
        nameEncoder.set(index, entity.name());
        locationEncoder.set(index, entity.location());
        preferencesEncoder.set(index, entity.preferences());
    }

    public Entity load(int index) {
        return new Entity(
                idEncoder.get(index),
                sizeEncoder.get(index),
                accessibleEncoder.get(index),
                colorEncoder.get(index),
                nameEncoder.get(index),
                locationEncoder.get(index),
                preferencesEncoder.get(index)
        );
    }

    public void saveTable(DataOutput stream) throws IOException {
        PERSISTENCE.save(stream, table);
    }
}
