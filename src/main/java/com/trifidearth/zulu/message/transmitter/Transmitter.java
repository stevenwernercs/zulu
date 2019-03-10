/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trifidearth.zulu.message.transmitter;

import com.trifidearth.zulu.message.Message;
import com.trifidearth.zulu.message.potiential.ElectricPotiential;
import com.trifidearth.zulu.utils.Utils;
import org.apache.log4j.Logger;

/**
 *
 * @author iSteve
 */
public abstract class Transmitter extends Message {

    private static final Logger log = Logger.getLogger(Transmitter.class);

    String name;
    final ElectricPotiential potiential;
    float lifespanSeconds;
    float decayspanSeconds;
    protected long dieTime;
    protected long decayTime;

    public Transmitter(String name, double potiential, float lifespanSeconds, float decayspanSeconds) {
        this.name = name;
        this.potiential = new ElectricPotiential(potiential);
        this.lifespanSeconds = lifespanSeconds;
        this.decayspanSeconds = decayspanSeconds;
        this.dieTime = System.currentTimeMillis()+(long)(lifespanSeconds*1000L);
        this.decayTime = System.currentTimeMillis()+(long)(decayspanSeconds*1000L);
    }
    
    public boolean checkDissolved() {
        long systemTime = System.currentTimeMillis();
        if(dieTime < systemTime) {
            potiential.setPotientialVoltage(0D);
            if(decayTime < systemTime) {
                log.trace(name + "'s decayspan of "+ decayspanSeconds + " second(s) is overdue: = "+ Utils.getSecondOfMillis(System.currentTimeMillis() - decayTime) + " second(s)");
                return true;
            }
            log.trace(name + "'s lifespan of "+ lifespanSeconds + " second(s) is overdue: "+ Utils.getSecondOfMillis(System.currentTimeMillis() - dieTime) + " second(s)");
            potiential.setPotientialVoltage(0D);
        }
        return false;
    }
    
    public ElectricPotiential getElectricPotiential(){
        return this.potiential;
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
