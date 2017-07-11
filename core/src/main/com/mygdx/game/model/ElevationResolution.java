package com.mygdx.game.model;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.mygdx.game.common.CoordTransform;

public class ElevationResolution {

    private static final Vector3 DOWN = new Vector3(0, 0, -1);

    private final float[] vertices;
    private final short[] triIndices;
    private final int vertComponnets;
    private final CoordTransform coordTrans;

    public ElevationResolution(float[] vertices, short[] triIndices, int vertComponnets, CoordTransform coordTrans) {
        this.vertices = vertices;
        this.triIndices = triIndices;
        this.vertComponnets = vertComponnets;
        this.coordTrans = coordTrans;
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
        Vector3 origin = coordTrans.toInternal(lon, lat, 10000);
        Ray ray = new Ray(origin, DOWN);
        Vector3 intersection = new Vector3();
        boolean hit = Intersector.intersectRayTriangles(ray, this.vertices, this.triIndices, this.vertComponnets, intersection);
        if (!hit) {
            throw new IllegalArgumentException("Position " + lon + ", " + lat + " does not intersect landscape");
        }
        return coordTrans.toExternalElev(intersection.z);
    }

}
