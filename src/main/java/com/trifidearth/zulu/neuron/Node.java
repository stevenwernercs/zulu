/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trifidearth.zulu.neuron;

import com.trifidearth.zulu.coordinate.Coordinate;
import org.json.JSONObject;

/**
 *
 * @author iSteve
 */
public abstract class Node {
    
    private final Coordinate fixedPoint;
    protected boolean isAlive = false;

    public void kill() {
        this.isAlive = false;
    }
    
    public void bringToLife(){
        this.isAlive = true;
    }

    public Node(Coordinate fixedPoint) {
        this.fixedPoint = fixedPoint;
    }

    public Coordinate getfixedPoint() {
        return fixedPoint;
    }
    
    public boolean isAlive() {
        return isAlive;
    }

    public abstract JSONObject toJson();

}
