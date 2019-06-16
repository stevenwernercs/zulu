/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trifidearth.zulu.coordinate;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author iSteve
 */
public class CoordinateBounds {
    
    private Coordinate origin;
    private int radius;

    public CoordinateBounds(Coordinate point, int radius) {
        this.origin = point;
        this.radius = radius;
    }

    public CoordinateBounds(JSONObject bounds) {
        this.origin = new Coordinate(bounds.getJSONObject("origin"));
        this.radius = bounds.getInt("radius");
    }

    public int getRadius() {
        return radius;
    }

    @Override
    public String toString() {
        return "CoordinateBounds{" + "origin=" + origin + ", radius=" + radius + '}';
    }

    boolean outOf(Coordinate growPoint) {
        return origin.computeDistanceTo(growPoint) > radius;
    }


    public JSONObject toJson() {
        JSONObject object = new JSONObject();
        object.put("radius", radius);
        object.put("origin", origin.toJson());
        return object;
    }

    public String info() {
        return "x-"+origin.getX()+"_y-"+origin.getY()+"_z-"+origin.getZ()+"_r-"+getRadius();
    }
}
