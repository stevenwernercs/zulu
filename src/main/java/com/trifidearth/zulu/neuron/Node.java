/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trifidearth.zulu.neuron;

import com.trifidearth.zulu.coordinate.Coordinate;

/**
 *
 * @author iSteve
 */
public abstract class Node {
    
    private final Coordinate fixedPoint;

    public Node(Coordinate fixedPoint) {
        this.fixedPoint = fixedPoint;
    }

    public Coordinate getfixedPoint() {
        return fixedPoint;
    }
    
}
