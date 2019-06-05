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
import org.json.JSONObject;

/**
 *
 * @author iSteve
 */
public class Synapse extends CommunicationNode<ActionPotiential, Transmitters> implements Grows {

    private double wander = 5D;
    private Neuron neuron;
    
    public Synapse(Coordinate coordinate, Neuron neuron) {
        super(new CoordinatePair(coordinate));
        this.neuron = neuron;
    }
    
    @Override
    public Transmitters propagate(ActionPotiential input) {
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
        int aliveNearby = nearby.countZeroPotentials();
        if (deadNearby > 0) {
            wander=5D;
        } else if(aliveNearby==0) {
            wander=Math.min(wander-1D, 0D);
        } else {
            wander-=1D;
        }
    }
}
