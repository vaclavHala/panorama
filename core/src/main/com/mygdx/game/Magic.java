
package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import com.mygdx.game.model.MapFeature;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Magic {

    private final LocationService location;
    private final MapFeaturesService featuresFinder;
    private final HeightFieldSurveyor surveyor;
    private final HeightFieldService heightService;
    private final PanoramaRenderer renderer;

    public Magic(
            final LocationService location,
            final MapFeaturesService featuresFinder,
            final HeightFieldSurveyor surveyor,
            final HeightFieldService heightService,
            final PanoramaRenderer renderer) {
        this.location = location;
        this.featuresFinder = featuresFinder;
        this.surveyor = surveyor;
        this.heightService = heightService;
        this.renderer = renderer;
    }

    public void foo(){
        Vector2 here = this.location.findCurrentLocation();
        List<MapFeature> features = this.featuresFinder.findNear(here);
        List<Vector2> heightField = this.surveyor.fieldFrom(here, features);
        List<Float> heights = this.heightService.getFor(heightField);
        List<Vector3> surface = this.assembleSurface(heightField, heights);
        renderer.render(features, surface);
    }

    private List<Vector3> assembleSurface(List<Vector2> xys, List<Float> zs) {
        if(xys.size() != zs.size())
            throw new IllegalArgumentException("fiels.size ("+xys.size()+") != heights.size ("+zs.size()+")");
        List<Vector3> surface = new ArrayList<Vector3>(xys.size());
        Iterator<Float> z = zs.iterator();
        for(Vector2 xy: xys){
            surface.add(new Vector3(xy.x, xy.y, z.next()));
        }
        return surface;
    }

}
