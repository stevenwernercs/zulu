/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trifidearth.zulu.neuron;

import com.trifidearth.zulu.coordinate.Coordinate;
import com.trifidearth.zulu.coordinate.CoordinateBounds;
import com.trifidearth.zulu.coordinate.CoordinatePair;
import com.trifidearth.zulu.message.potential.ActionPotential;
import com.trifidearth.zulu.message.transmitter.Transmitters;

/**
 *
 * @author iSteve
 */
public class Synapse extends CommunicationNode<ActionPotential, Transmitters> implements Grows {

    private static final double WANDER_MIN = 0.0D;
    private static final double WANDER_MAX = 10.0D;
    private static final double WANDER_INC_NEAR_DEAD = 0.1D;
    private static final double WANDER_DEC_NO_ACTIVITY = 0.05D;
    private static final double WANDER_DEC_ACTIVE = 0.2D;
    private double wander = 5D;
    private Neuron neuron;
    
    public Synapse(Coordinate coordinate, Neuron neuron) {
        super(new CoordinatePair(coordinate));
        this.neuron = neuron;
    }
    
    public Synapse(CoordinatePair coordinatePair) {
        super(coordinatePair);
    }

    @Override
    public Transmitters propagate(ActionPotential input) {
        if(input!=null){
            return Transmitters.getRandomTransmitters();
        }
        checkSurroundings();
        return null;
    }

    @Override
    public void grow(CoordinateBounds bounds) {
        if(wander > 0) {
            getCoordinatePair().growRandom(Math.min(wander,.1D), bounds);
        }
    }
    
    private void checkSurroundings(){
        Transmitters nearby = neuron.brain.pollNearByTransmitters(this.getGrowing());
        int deadNearby = nearby.countZeroPotentials();
        int aliveNearby = nearby.countNonZeroPotentials();
        if (deadNearby > 0) {
            wander += WANDER_INC_NEAR_DEAD;
        } else if (aliveNearby == 0) {
            wander -= WANDER_DEC_NO_ACTIVITY;
        } else {
            // Favor stabilizing where there is activity
            wander -= Math.min(aliveNearby, 5) * WANDER_DEC_ACTIVE;
        }
        if (wander < WANDER_MIN) wander = WANDER_MIN;
        if (wander > WANDER_MAX) wander = WANDER_MAX;
    }
}
