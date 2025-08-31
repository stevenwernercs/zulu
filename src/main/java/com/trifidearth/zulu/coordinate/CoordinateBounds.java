/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trifidearth.zulu.coordinate;

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

    public int getRadius() {
        return radius;
    }

    public Coordinate getOrigin() {
        return origin;
    }

    @Override
    public String toString() {
        return "CoordinateBounds{" + "origin=" + origin + ", radius=" + radius + '}';
    }

    boolean outOf(Coordinate growPoint) {
        return origin.computeDistanceTo(growPoint) > radius;
    }
    
    
}
