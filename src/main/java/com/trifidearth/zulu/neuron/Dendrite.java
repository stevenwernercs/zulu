/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trifidearth.zulu.neuron;

import com.trifidearth.zulu.coordinate.Coordinate;
import com.trifidearth.zulu.coordinate.CoordinateBounds;
import com.trifidearth.zulu.coordinate.CoordinatePair;
import com.trifidearth.zulu.message.potential.ElectricPotential;
import com.trifidearth.zulu.message.transmitter.Transmitter;
import com.trifidearth.zulu.message.transmitter.Transmitters;

/**
 *
 * @author iSteve
 */
public class Dendrite extends CommunicationNode<Transmitters, ElectricPotential> implements Grows{
    
    private static final double WANDER_MIN = 0.0D;
    private static final double WANDER_MAX = 5.0D;
    private static final double WANDER_INC = 0.05D;
    private static final double WANDER_DEC_PER_SIGNAL = 0.2D;
    private double wander = 2D;
    
    public Dendrite(Coordinate coordinate) {
        super(new CoordinatePair(coordinate));
    }
    
    public Dendrite(CoordinatePair coordinatePair) {
        super(coordinatePair);
    }

    @Override
    public ElectricPotential propagate(Transmitters transmitters) {
        int receivedCount = transmitters.countNonZeroPotentials();
        if(receivedCount > 0){
            // Reduce wander proportional to activity, bounded
            wander -= Math.min(receivedCount, 5) * WANDER_DEC_PER_SIGNAL;
        } else {
            // Slowly increase wander in absence of input
            wander += WANDER_INC;
        }
        // clamp
        if (wander < WANDER_MIN) wander = WANDER_MIN;
        if (wander > WANDER_MAX) wander = WANDER_MAX;
        
        ElectricPotential ep = new ElectricPotential(0);
        double sum = 0D;
        for(Transmitter each : transmitters.getTransmitters()){
            //ep.absorb(each.getElectricPotential());
            sum += each.getElectricPotential().getPotentialVoltage();
        }
        ep.setPotentialVoltage(sum);
        return ep;
    }

    @Override
    public void grow(CoordinateBounds bounds) {
        if(wander > 0) {
            getCoordinatePair().growRandom(Math.min(wander, .15D), bounds);
        }
    }
}
