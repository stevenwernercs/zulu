/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trifidearth.zulu.message.transmitter;

import com.trifidearth.zulu.message.Message;
import com.trifidearth.zulu.message.potiential.ElectricPotiential;
import org.apache.log4j.Logger;

/**
 *
 * @author iSteve
 */
public abstract class Transmitter extends Message {

    private static final Logger log = Logger.getLogger(Transmitter.class);
    
    final ElectricPotiential potiential;
    protected long lifespan = System.currentTimeMillis()+1000L; //implementer must set

    public Transmitter(double potiential) {
        this.potiential = new ElectricPotiential(potiential);
    }
    
    public void checkDissolved() {
        long systemTime = System.currentTimeMillis();
        if(lifespan < systemTime) {
            log.trace("lifespan = "+ (this.lifespan-System.currentTimeMillis()));
            potiential.setPotientialVoltage(0D);
        }
    }
    
    public ElectricPotiential getElectricPotiential(){
        return this.potiential;
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
