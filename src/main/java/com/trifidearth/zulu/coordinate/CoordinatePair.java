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
public class CoordinatePair {
    
    private Coordinate fixed;
    private Coordinate growing;

    public CoordinatePair(Coordinate point) {
        this.fixed = point;
        this.growing = point;
    }

    public CoordinatePair(Coordinate fixed, Coordinate growing) {
        this.fixed = fixed;
        this.growing = growing;
    }

    public Coordinate getFixed() {
        return fixed;
    }

    public void setInput(Coordinate fixed) {
        this.fixed = fixed;
    }

    public Coordinate getGrowing() {
        return growing;
    }

    public void setOutput(Coordinate growing) {
        this.growing = growing;
    }
    
    public double distance() {
        return fixed.computeDistanceTo(growing);
    }

    public void grow(int growDistance, double degreeVariance, CoordinateBounds bounds) {
        Coordinate growPoint = calculate(fixed, getDistance()+growDistance, 
                getThetaXY()+degreeVariance, 
                getThetaXZ()+degreeVariance);
        if(bounds.outOf(growPoint)) {
            throw new RuntimeException("Grown out of bounds: " + growPoint + ">" + bounds);
        }
        growing = growPoint;
    }
    
    public void growRandom(int growDistance, CoordinateBounds bounds) {
        Coordinate growPoint;
        do {
            growPoint = calculate(fixed, 
                getDistance()+(Math.random() * growDistance), 
                getThetaXY()+ (Math.random() * 360), 
                getThetaXZ()+ (Math.random() * 360));
        } while (bounds.outOf(growPoint));
        growing = growPoint;
    }

    private double getDistance() {
        return fixed.computeDistanceTo(growing);
    }

    private double getThetaXY() {
        double theta = Math.atan2(growing.getY() - fixed.getY(), 
                                  growing.getX() - fixed.getX());
        return theta < 0 ? theta + 360 : theta;
    }
    
    private double getThetaXZ() {
        double theta = Math.atan2(growing.getZ() - fixed.getZ(), 
                                  growing.getX() - fixed.getX());
        return theta < 0 ? theta + 360 : theta;
    }

    private static Coordinate calculate(Coordinate origin, double distance, double degreeXY, double degreeXZ) {
        double x = (Math.cos(Math.toRadians(degreeXZ))*distance) + origin.getX();
        double y = (Math.sin(Math.toRadians(degreeXY))*distance) + origin.getY();
        double z = (Math.sin(Math.toRadians(degreeXZ))*distance) + origin.getZ();
        
        return new Coordinate((int)x, (int)y, (int)z);
    }

    @Override
    public String toString() {
        return "<"+fixed.toString() + "," + growing.toString()+">";
    }
}
