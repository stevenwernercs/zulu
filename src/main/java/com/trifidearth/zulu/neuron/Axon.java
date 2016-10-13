/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trifidearth.zulu.neuron;

import com.trifidearth.zulu.coordinate.CoordinateBounds;
import com.trifidearth.zulu.coordinate.CoordinatePair;
import com.trifidearth.zulu.message.potiential.ActionPotiential;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author iSteve
 */
public class Axon extends CommunicationNode<ActionPotiential, ActionPotiential> implements Grows{
    
    double distance;

    public Axon(CoordinatePair coordinatesPair) {
        super(coordinatesPair);
        this.distance = coordinatesPair.distance();
    }
    
    /**
     *
     * @param ap
     */
    @Override
    public ActionPotiential propagate (ActionPotiential ap) {
        delay();
        return ap;
    }

    public void delay() {
        try {
            Thread.sleep((int)(distance*100));
        } catch (InterruptedException ex) {
            Logger.getLogger(Axon.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void grow(CoordinateBounds bounds) {
        //TODO function of bounds
        if(getFixed().equals(getGrowing())) {
            getCoordinatePair().growRandom(10, bounds);
            distance = getCoordinatePair().distance();
        } else {
            throw new RuntimeException("can only grow an axon once");
        }
        
    }
}
