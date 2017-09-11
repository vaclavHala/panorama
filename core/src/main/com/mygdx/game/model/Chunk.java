package com.mygdx.game.model;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Chunk implements Comparable<Chunk> {

    public static final Pattern NAME_READ_PATTERN = Pattern.compile("chunk_([ns])([\\d]+)_([ew])([\\d]+)");

    public final int lat;
    public final int lon;
    public final String name;

    public Chunk(int lat, int lon) {
        this.lat = lat;
        this.lon = lon;
        if (this.lat < -90 || this.lat > 90) {
            throw new IllegalArgumentException("Invalid lat: " + lat);
        }
        if (this.lon < -180 || this.lon > 180) {
            throw new IllegalArgumentException("Invalid lon: " + lon);
        }
        this.name = format("chunk_%c%d_%c%d",
                           lat < 0 ? 's' : 'n', Math.abs(lat),
                           lon < 0 ? 'w' : 'e', Math.abs(lon));
    }

    public static Chunk fromName(String name) throws IllegalArgumentException {
        Matcher m = NAME_READ_PATTERN.matcher(name);
        if (!m.find()) {
            throw new IllegalArgumentException("Unrecognized name: " + name);
        }

        int lat = parseInt(m.group(2));
        if (m.group(1).equals("s")) {
            lat *= -1;
        }
        int lon = parseInt(m.group(4));
        if (m.group(3).equals("w")) {
            lon *= -1;
        }
        return new Chunk(lat, lon);
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public int compareTo(Chunk other) {
        int latComp = other.lat - this.lat;
        if (latComp != 0) {
            return latComp;
        }
        return other.lon - this.lon;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Chunk)) {
            return false;
        }
        Chunk other = (Chunk) obj;
        return other.lon == this.lon && other.lat == this.lat;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.lon, this.lat);
    }
}
