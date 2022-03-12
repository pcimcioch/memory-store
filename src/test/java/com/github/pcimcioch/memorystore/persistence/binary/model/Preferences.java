package com.github.pcimcioch.memorystore.persistence.binary.model;

import com.github.pcimcioch.serializer.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public final class Preferences {
    private final boolean acceptsA;
    private final boolean acceptsB;
    private final boolean acceptsC;
    private final boolean acceptsD;
    private final boolean acceptsE;

    public Preferences(boolean acceptsA, boolean acceptsB, boolean acceptsC, boolean acceptsD, boolean acceptsE) {
        this.acceptsA = acceptsA;
        this.acceptsB = acceptsB;
        this.acceptsC = acceptsC;
        this.acceptsD = acceptsD;
        this.acceptsE = acceptsE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Preferences that = (Preferences) o;
        return acceptsA == that.acceptsA && acceptsB == that.acceptsB && acceptsC == that.acceptsC && acceptsD == that.acceptsD && acceptsE == that.acceptsE;
    }

    @Override
    public int hashCode() {
        return Objects.hash(acceptsA, acceptsB, acceptsC, acceptsD, acceptsE);
    }

    public static final class PreferencesSerializer implements Serializer<Preferences> {

        @Override
        public void serialize(DataOutput encoder, Preferences object) throws IOException {
            if (object == null) {
                encoder.writeBoolean(true);
            } else {
                encoder.writeBoolean(false);

                encoder.writeBoolean(object.acceptsA);
                encoder.writeBoolean(object.acceptsB);
                encoder.writeBoolean(object.acceptsC);
                encoder.writeBoolean(object.acceptsD);
                encoder.writeBoolean(object.acceptsE);
            }
        }

        @Override
        public Preferences deserialize(DataInput decoder) throws IOException {
            boolean isNull = decoder.readBoolean();
            if (isNull) {
                return null;
            }

            return new Preferences(
                    decoder.readBoolean(),
                    decoder.readBoolean(),
                    decoder.readBoolean(),
                    decoder.readBoolean(),
                    decoder.readBoolean()
            );
        }
    }
}
