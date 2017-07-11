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

    public Visibility(float[] vertices, short[] triIndices, int vertComponnets) {
        this.vertices = vertices;
        this.triIndices = triIndices;
        this.vertComponnets = vertComponnets;
        this.epsPercent = .01F;
    }

    public static Vector3 intersection;
    public static Ray ray;

    /**
     * Given some landscape, this determines if two points can see each other.
     * Expects parameters to be given in world banana units.
     * @return true if there is clean line of sight between the two points.
     */
    public boolean isVisibleFrom(Vector3 from, Vector3 to) {
        Vector3 dir = to.cpy().sub(from).nor();

        //        Ray ray = new Ray(from, dir);
        ray = new Ray(from, dir);
        //        Vector3 intersection = new Vector3();
        intersection = new Vector3();
        boolean hit = Intersector.intersectRayTriangles(ray, this.vertices, this.triIndices, this.vertComponnets, intersection);
        //        System.out.println("INTER: " + hit + ": " + from + ", " + to + ", " + intersection);
        if (!hit) {
            return true;
        }

        float distFromTo = from.dst(to);
        float distFromIntersection = from.dst(intersection);

        return distFromTo * (1 - epsPercent) < distFromIntersection;
    }

}
