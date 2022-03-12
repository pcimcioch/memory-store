package com.github.pcimcioch.memorystore.persistence.binary.model;

import com.github.pcimcioch.serializer.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public final class Point {
    private final double x;
    private final double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return Double.compare(point.x, x) == 0 && Double.compare(point.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public static final class PointSerializer implements Serializer<Point> {

        @Override
        public void serialize(DataOutput encoder, Point object) throws IOException {
            if (object == null) {
                encoder.writeDouble(Double.NaN);
            } else {
                encoder.writeDouble(object.x);
                encoder.writeDouble(object.y);
            }
        }

        @Override
        public Point deserialize(DataInput decoder) throws IOException {
            double x = decoder.readDouble();
            if (Double.isNaN(x)) {
                return null;
            }
            double y = decoder.readDouble();

            return new Point(x, y);
        }
    }
}
