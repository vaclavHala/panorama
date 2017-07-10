package com.mygdx.game.model;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import java.util.Arrays;

public class ElevationResolution {

    private static final Vector3 DOWN = new Vector3(0, -1, 0);

    private final float[] vertices;
    private final short[] triIndices;
    private final int vertComponnets;

    public ElevationResolution(float[] vertices, short[] triIndices, int vertComponnets) {
        this.vertices = vertices;
        this.triIndices = triIndices;
        this.vertComponnets = vertComponnets;
    }

    /**
     * Returns elevation at given point
     */
    public float projectToLandscape(Vector2 landscapePosition) {
        Vector3 origin = new Vector3(landscapePosition.x, 10, landscapePosition.y);
        Ray ray = new Ray(origin, DOWN);
        Vector3 intersection = new Vector3();
        boolean hit = Intersector.intersectRayTriangles(ray, this.vertices, this.triIndices, this.vertComponnets, intersection);
        if (!hit) {
            throw new IllegalArgumentException("Position " + landscapePosition + " does not intersect landscape");
        }
        return intersection.y;
    }

}
