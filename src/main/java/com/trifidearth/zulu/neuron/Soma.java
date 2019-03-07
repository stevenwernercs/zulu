/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trifidearth.zulu.neuron;

import com.trifidearth.zulu.coordinate.CoordinatePair;
import com.trifidearth.zulu.message.potiential.ElectricPotiential;
import com.trifidearth.zulu.message.potiential.ActionPotiential;

/**
 *
 * @author iSteve
 */
public class Soma extends CommunicationNode<ElectricPotiential, ActionPotiential> {

    private static final double RESTING_POTIENTIAL = -70D;
    private static final double THRESHOLD = -55D;
    private static final double DE_POLARIZATION = 40D;
    private static final double HYPERPOLARIZATION = -75D;
    ElectricPotiential potiential = new ElectricPotiential(RESTING_POTIENTIAL);
    
    public Soma(CoordinatePair coordinatePair) {
        super(coordinatePair);
    }
    
    public Soma(CoordinatePair coordinatePair, ElectricPotiential potiential) {
        super(coordinatePair);
        this.potiential = potiential;
    }
    
    //buffer to collect and propagate action potentials
    ActionPotiential summation() {
        ActionPotiential output = null;
        if(potiential.getPotientialVoltage() > THRESHOLD) {
            output = new ActionPotiential();
        }
        potiential.setPotientialVoltage(RESTING_POTIENTIAL);
        return output;
    }
    
    void generateTransmitters() {
    }
    
    void sendTranmitter() {
    }

    @Override
    public ActionPotiential propagate(ElectricPotiential input) {
        this.potiential.absorb(input);
        return summation();
    }
}
