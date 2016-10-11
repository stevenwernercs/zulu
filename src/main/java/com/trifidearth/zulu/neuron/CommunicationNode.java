/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trifidearth.zulu.neuron;

import com.trifidearth.zulu.coordinate.CoordinatePair;
import com.trifidearth.zulu.coordinate.Coordinate;
import com.trifidearth.zulu.message.Message;

/**
 *
 * @author iSteve
 * @param <I>
 * @param <O>
 */
public abstract class CommunicationNode <I extends Message, O extends Message> extends Node {

    private CoordinatePair coordinatePair;

    public CommunicationNode(CoordinatePair coordinatePair) {
        super(coordinatePair.getFixed());
        this.coordinatePair = coordinatePair;
    }
        
    public CoordinatePair getCoordinatePair() {
        return coordinatePair;
    }
    
    public Coordinate getFixed() {
        return coordinatePair.getFixed();
    }
    
    public Coordinate getGrowing() {
        return coordinatePair.getGrowing();
    }
    
    public abstract O propagate(I input);
}
