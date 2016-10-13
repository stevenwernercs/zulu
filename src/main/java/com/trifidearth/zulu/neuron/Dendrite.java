/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trifidearth.zulu.neuron;

import com.trifidearth.zulu.coordinate.Coordinate;
import com.trifidearth.zulu.coordinate.CoordinateBounds;
import com.trifidearth.zulu.coordinate.CoordinatePair;
import com.trifidearth.zulu.message.potiential.ElectricPotiential;
import com.trifidearth.zulu.message.transmitter.Transmitter;
import com.trifidearth.zulu.message.transmitter.Transmitters;

/**
 *
 * @author iSteve
 */
public class Dendrite extends CommunicationNode<Transmitters, ElectricPotiential> implements Grows{
    
    public Dendrite(Coordinate coordinate) {
        super(new CoordinatePair(coordinate));
    }
    
    public Dendrite(CoordinatePair coordinatePair) {
        super(coordinatePair);
    }

    @Override
    public ElectricPotiential propagate(Transmitters trasmitters) {
        return new ElectricPotiential(trasmitters.getTransmitters().size());
    }

    @Override
    public void grow(CoordinateBounds bounds) {
        getCoordinatePair().growRandom(2, bounds);
    }
}
