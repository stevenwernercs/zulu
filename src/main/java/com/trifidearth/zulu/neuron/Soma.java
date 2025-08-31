/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trifidearth.zulu.neuron;

import com.trifidearth.zulu.coordinate.CoordinatePair;
import com.trifidearth.zulu.message.potential.ElectricPotential;
import com.trifidearth.zulu.message.potential.ActionPotential;

/**
 *
 * @author iSteve
 */
public class Soma extends CommunicationNode<ElectricPotential, ActionPotential> {

    private static final double RESTING_POTENTIAL = -70D;
    private static final double THRESHOLD = -55D;
    private static final double DE_POLARIZATION = 40D;
    private static final double HYPERPOLARIZATION = -75D;
    ElectricPotential potential = new ElectricPotential(RESTING_POTENTIAL);
    
    public Soma(CoordinatePair coordinatePair) {
        super(coordinatePair);
    }
    
    public Soma(CoordinatePair coordinatePair, ElectricPotential potential) {
        super(coordinatePair);
        this.potential = potential;
    }
    
    //buffer to collect and propagate action potentials
    ActionPotential summation() {
        ActionPotential output = null;
        if(potential.getPotentialVoltage() > THRESHOLD) {
            output = new ActionPotential();
        }
        potential.setPotentialVoltage(RESTING_POTENTIAL);
        return output;
    }
    
    void generateTransmitters() {
    }
    
    void sendTranmitter() {
    }

    @Override
    public ActionPotential propagate(ElectricPotential input) {
        this.potential.absorb(input);
        return summation();
    }
}
