/**
 * # COORDINATES
 *
 * ## elev data
 * [lon,lat,elev]
 *
 * ## libgdx
 * [width, height, depth]
 *
 * # SIZES OF STUFF
 *
 * to be able to triangulate using {@link com.badlogic.gdx.math.DelaunayTriangulator}
 * size of single mesh can be at most X vertices such that X * 2 is < 2^16
 * because x,y per point, all have to fit into short[] which overflows at ~32K
 * this means that side of the mesh is 120 vertices
 * 120 * 120 = 14400; 14400 * 2 = 28800 which is < 32K
 * in real world coordinates 120 vertices is ~10km
 * each cell is ~0.000833.. degrees = ~90 meters
 *
 * all of the above is highly hand-wavy, as time permits actual GIS stuff should be used ...
 * https://knowledge.safe.com/articles/725/calculating-accurate-length-in-meters-for-lat-long.html
 *
 * ## elev data
 * elevation data is stored as stream of elevation points only
 * size of cell is application wide and given as constant
 * dimensions of file in cells are also given as constant,
 * global position of the chunk represented by the elev file is given by its name
 *
 * after loading elev data model of the landscape is generated from it
 * custom loader (TODO) is used for this which works with the elevation
 * data directly, without having to transform it to some intermediary format (e.g. G3DJ) first
 */
/*
stahnout elev data z http://droppr.org/srtm/v4.1/5_5x5_ascii/
rozsekat na bloky 1x1 stupen
zazipovat, vse bude offline
    moznost stahnout si na lokal bloky ktere mne zajimaji

k blokum postahovat features z openstreetmap (tak aby na sebe pasovaly, bude se snaz nacitat)

http://overpass-api.de/api/interpreter?data=node(49.2, 14.95, 49.3, 15.0)[natural=peak];out;
typy popsane na http://wiki.openstreetmap.org/wiki/Map_Features zajimave jsou peak, stone, rock, reky, vesnicky etc.
 */
package com.mygdx.game;