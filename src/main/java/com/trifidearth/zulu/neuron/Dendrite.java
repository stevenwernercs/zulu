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
    
    private double wander = 2D;
    
    public Dendrite(Coordinate coordinate) {
        super(new CoordinatePair(coordinate));
    }
    
    public Dendrite(CoordinatePair coordinatePair) {
        super(coordinatePair);
    }

    @Override
    public ElectricPotiential propagate(Transmitters transmitters) {
        int recievedCount = transmitters.countNonZeroPotentials();
        if(recievedCount > 0){
            wander = Math.min(wander-recievedCount, 0);
        } else {
            wander += 1D;
        }
        
        ElectricPotiential ep = new ElectricPotiential(0);
        double sum = 0D;
        for(Transmitter each : transmitters.getTransmitters()){
            //ep.absorb(each.getElectricPotiential());
            sum += each.getElectricPotiential().getPotientialVoltage();
        }
        ep.setPotientialVoltage(sum);
        return ep;
    }

    @Override
    public void grow(CoordinateBounds bounds) {
        if(wander > 0) {
            getCoordinatePair().growRandom(Math.min(wander, .15D), bounds);
        }
    }
}
