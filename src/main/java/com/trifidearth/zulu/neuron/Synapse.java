/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trifidearth.zulu.neuron;

import com.trifidearth.zulu.coordinate.Coordinate;
import com.trifidearth.zulu.coordinate.CoordinateBounds;
import com.trifidearth.zulu.coordinate.CoordinatePair;
import com.trifidearth.zulu.message.potiential.ActionPotiential;
import com.trifidearth.zulu.message.transmitter.Transmitters;

/**
 *
 * @author iSteve
 */
public class Synapse extends CommunicationNode<ActionPotiential, Transmitters> implements grows {

    public Synapse(Coordinate coordinate) {
        super(new CoordinatePair(coordinate));
    }
    
    public Synapse(CoordinatePair coordinatePair) {
        super(coordinatePair);
    }
    
    public void delay() {
        
    }

    @Override
    public Transmitters propagate(ActionPotiential input) {
        if(input!=null){
            return Transmitters.getRandomTransmitters();
        }
        return null;
    }

    @Override
    public void grow(CoordinateBounds bounds) {
        getCoordinatePair().growRandom(2, bounds);
    }
}
