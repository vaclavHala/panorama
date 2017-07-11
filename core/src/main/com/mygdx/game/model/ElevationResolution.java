package com.mygdx.game.model;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.mygdx.game.ElevConfig;

public class ElevationResolution {

    private static final Vector3 DOWN = new Vector3(0, 0, -1);

    private final float[] vertices;
    private final short[] triIndices;
    private final int vertComponnets;
    private final float scalerLon;
    private final float scalerLat;
    private final float scalerElev;

    public ElevationResolution(float[] vertices, short[] triIndices, int vertComponnets, ElevConfig elevConfig) {
        this.vertices = vertices;
        this.triIndices = triIndices;
        this.vertComponnets = vertComponnets;
        this.scalerLon = elevConfig.scalerLon;
        this.scalerLat = elevConfig.scalerLat;
        this.scalerElev = elevConfig.scalerElev;
    }

    public float projectToLandscape(Vector2 landscapePosition) {
        return projectToLandscape(landscapePosition.x, landscapePosition.y);
    }

    /**
     * Returns elevation at given point.
     * Input is expected in real world degrees of lon and lat.
     * Output is in meters above sea level.
     */
    public float projectToLandscape(float lon, float lat) {
        Vector3 origin = new Vector3(lon * this.scalerLon, lat * this.scalerLat, 10000 * this.scalerElev);
        Ray ray = new Ray(origin, DOWN);
        Vector3 intersection = new Vector3();
        boolean hit = Intersector.intersectRayTriangles(ray, this.vertices, this.triIndices, this.vertComponnets, intersection);
        if (!hit) {
            throw new IllegalArgumentException("Position " + lon + ", " + lat + " does not intersect landscape");
        }
        return intersection.z / this.scalerElev;
    }

}
