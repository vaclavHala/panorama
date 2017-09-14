package com.mygdx.game.model;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

public class Visibility {

    private final float[] vertices;
    private final short[] triIndices;
    private final int vertComponnets;

    // if the intersection is closer than distance between form and to - eps
    // assume there is something blocking the view
    private final float epsPercent;

    // avoids unneccessary allocation
    private final Vector3 intersection;
    private final Vector3 dir;
    private final Ray ray;

    public Visibility(float[] vertices, short[] triIndices, int vertComponnets) {
        this.vertices = vertices;
        this.triIndices = triIndices;
        this.vertComponnets = vertComponnets;
        this.epsPercent = .01F;

        this.intersection = new Vector3();
        this.dir = new Vector3();
        this.ray = new Ray();
    }

    /**
     * Given some landscape, this determines if two points can see each other.
     * Expects parameters to be given in world banana units.
     * @return true if there is clean line of sight between the two points.
     */
    public boolean isVisibleFrom(Vector3 from, Vector3 to) {
        terrainIntersection(from, to, intersection);

        if (intersection == null) {
            return true;
        }

        float distFromTo = from.dst(to);
        float distFromIntersection = from.dst(intersection);

        return distFromTo * (1 - epsPercent) < distFromIntersection;
    }

    /**
     * Given some landscape, point of observer and target point that the observer is looking at
     * returns point on landscape where the landscape first intersects line of sight between the
     * two points, or null if no such point exists
     */
    public Vector3 terrainIntersection(Vector3 from, Vector3 to, Vector3 intersection) {
        dir.set(to).sub(from).nor();
        ray.set(from, dir);
        boolean hit = Intersector.intersectRayTriangles(ray, this.vertices, this.triIndices, this.vertComponnets, intersection);
        if (hit) {
            return intersection;
        } else {
            return null;
        }
    }
}
