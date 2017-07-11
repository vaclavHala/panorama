package com.mygdx.game.common;

import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.ElevConfig;

public class CoordTransform {

    private final float offsetLon;
    private final float offsetLat;
    private final float offsetElev;

    private final float scalerLon;
    private final float scalerLat;
    private final float scalerElev;

    public CoordTransform(ElevConfig elevCfg, Vector3 userPosition) {
        this.scalerLon = elevCfg.scalerLon;
        this.scalerLat = elevCfg.scalerLat;
        this.scalerElev = elevCfg.scalerElev;

        this.offsetLon = userPosition.x * scalerLon;
        this.offsetLat = userPosition.y * scalerLat;
        this.offsetElev = userPosition.z * scalerElev;
    }

    public Vector3 toInternal(Vector3 external, Vector3 out) {
        out.set(this.toInternalLon(external.x),
                this.toInternalLat(external.y),
                this.toInternalElev(external.z));
        return out;
    }

    public Vector3 toInternal(float x, float y, float z) {
        return this.toInternal(new Vector3(x, y, z), new Vector3());
    }

    public float toInternalLon(float lon) {
        return lon * scalerLon - offsetLon;
    }

    public float toInternalLat(float lat) {
        return lat * scalerLat - offsetLat;
    }

    public float toInternalElev(float elev) {
        return elev * scalerElev - offsetElev;
    }

    public float toExternalLon(float lon) {
        return (lon + offsetLon) / scalerLon;
    }

    public float toExternalLat(float lat) {
        return (lat + offsetLat) / scalerLon;
    }

    public float toExternalElev(float elev) {
        return (elev + offsetElev) / scalerElev;
    }

}
