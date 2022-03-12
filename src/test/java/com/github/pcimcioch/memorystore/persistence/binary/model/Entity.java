package com.github.pcimcioch.memorystore.persistence.binary.model;

import java.util.Objects;

public final class Entity {
    private final long id;
    private final short size;
    private final boolean accessible;
    private final Color color;
    private final String name;
    private final Point location;
    private final Preferences preferences;

    public enum Color {
        RED,
        GREEN,
        BLUE
    }

    public Entity(long id, short size, boolean accessible, Color color, String name, Point location, Preferences preferences) {
        this.id = id;
        this.size = size;
        this.accessible = accessible;
        this.color = color;
        this.name = name;
        this.location = location;
        this.preferences = preferences;
    }

    public long id() {
        return id;
    }

    public short size() {
        return size;
    }

    public boolean accessible() {
        return accessible;
    }

    public Color color() {
        return color;
    }

    public String name() {
        return name;
    }

    public Point location() {
        return location;
    }

    public Preferences preferences() {
        return preferences;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return id == entity.id && size == entity.size && accessible == entity.accessible && color == entity.color && Objects.equals(name, entity.name) && Objects.equals(location, entity.location) && Objects.equals(preferences, entity.preferences);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, size, accessible, color, name, location, preferences);
    }
}
