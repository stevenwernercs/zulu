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
import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 *
 * @author iSteve
 */
public class Axon extends CommunicationNode<ActionPotiential, ActionPotiential> implements Grows{
    
    private static final Logger log = Logger.getLogger(Axon.class);
    
    double distance;

    public Axon(CoordinatePair coordinatesPair) {
        super(coordinatesPair);
        this.distance = coordinatesPair.distance();
    }

    public Axon(Coordinate somaPoint, JSONObject jsonObject) {
        super(new CoordinatePair(somaPoint, new Coordinate(jsonObject.getJSONObject("endpoint"))));
        this.distance = jsonObject.getDouble("distance");
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
            log.error("Interrupted my sleep", ex);
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

    @Override
    public JSONObject toJson() {
        JSONObject axon = new JSONObject();
        axon.put("distance", this.distance);
        axon.put("endpoint", this.getCoordinatePair().getGrowing().toJson());
        return axon;
    }
}
