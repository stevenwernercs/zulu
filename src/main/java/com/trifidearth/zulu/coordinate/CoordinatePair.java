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

    private static boolean FORCE_2D = false;

    public static void setForce2D(boolean force) { FORCE_2D = force; }
    public static boolean isForce2D() { return FORCE_2D; }

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
    
    public void growRandom(double growDistance, CoordinateBounds bounds) {
        Coordinate candidate;
        int attempts = 0;
        do {
            candidate = calculate(fixed,
                    getDistance() + (Math.random() * growDistance),
                    getThetaXY() + (Math.random() * 360),
                    getThetaXZ() + (Math.random() * 360));
            attempts++;
            if (attempts > 500) break;
        } while (bounds.outOf(candidate));
        if (bounds.outOf(candidate)) {
            // Smooth fallback: clamp to the sphere along the ray from bounds.origin to candidate
            candidate = clampToSphere(bounds.getOrigin(), candidate, bounds.getRadius());
        }
        growing = candidate;
    }

    private static Coordinate clampToSphere(Coordinate origin, Coordinate point, int radius) {
        double dx = point.getX() - origin.getX();
        double dy = point.getY() - origin.getY();
        double dz = point.getZ() - origin.getZ();
        double len = Math.sqrt(dx*dx + dy*dy + dz*dz);
        if (len == 0) return new Coordinate(origin.getX(), origin.getY(), origin.getZ());
        double scale = Math.min(1.0, (radius - 1.0) / len);
        int nx = origin.getX() + (int)Math.round(dx * scale);
        int ny = origin.getY() + (int)Math.round(dy * scale);
        int nz = origin.getZ() + (int)Math.round(dz * scale);
        return new Coordinate(nx, ny, nz);
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
        double z = FORCE_2D ? origin.getZ() : (Math.sin(Math.toRadians(degreeXZ))*distance) + origin.getZ();
        return new Coordinate((int)x, (int)y, (int)z);
    }

    @Override
    public String toString() {
        return "<"+fixed.toString() + "," + growing.toString()+">";
    }
}
