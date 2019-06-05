/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trifidearth.zulu.coordinate;

import org.json.JSONObject;

/**
 *
 * @author iSteve
 */
public class Coordinate implements Comparable {

    public static Coordinate getTopLeftFront(CoordinateBounds bounds) {
        int maxRadius = bounds.getRadius();
        int minRadius = maxRadius*-1;
        return new Coordinate(minRadius, maxRadius, maxRadius);
    }
    
    private int x;
    private int y;
    private int z;
    
    public Coordinate(int x, int y, int z) {
        this.x = x;
        this.y = y; 
        this.z = 0; //TODO use z
    }

    public Coordinate(JSONObject origin) {
        this.x = origin.getInt("x");
        this.y = origin.getInt("y");
        this.z = origin.getInt("z");
    }


    public Coordinate(CoordinateBounds bounds) {
        x = (int)(getRandom() * bounds.getRadius());
        y = (int)(getRandom() * bounds.getRadius());
        z = (int)(getRandom() * bounds.getRadius());
    }
    
    public Coordinate(Coordinate coordinate, int radius) {
        x = coordinate.x + (int)(getRandom() * radius);
        y = coordinate.y + (int)(getRandom() * radius);
        z = coordinate.z + (int)(getRandom() * radius);
    }
    
    public double computeDistanceTo(Coordinate that) {
        return  Math.pow(Math.pow(this.x-that.x,2)+
                                Math.pow(this.z-that.z,2)+
                                Math.pow(this.z-that.z,2), 
                              .5);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + this.x;
        hash = 67 * hash + this.y;
        hash = 67 * hash + this.z;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Coordinate other = (Coordinate) obj;
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        if (this.z != other.z) {
            return false;
        }
        return true;
    }
    
    /**
     * 
     * @return random number from (-1,1) exclusive
     */
    private static float getRandom() {
        return ((float)(Math.random()*2.0D)) - 1F;
    }

    @Override
    public int compareTo(Object o) {
        Coordinate that = (Coordinate)o;
        int ydiff = this.y - that.y;
        return ydiff == 0 ? that.x - this.x : ydiff;
    }

    @Override
    public String toString() {
        return "<"+x+","+y+","+z+">";
    }

    public JSONObject toJson() {
        JSONObject object = new JSONObject();
        object.put("x", x);
        object.put("y", y);
        object.put("z", z);
        return object;
    }
}
