/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trifidearth.zulu.message.transmitter;

import com.trifidearth.zulu.message.Message;
import com.trifidearth.zulu.message.potiential.ElectricPotiential;

/**
 *
 * @author iSteve
 */
public abstract class Transmitter extends Message {

    ElectricPotiential potiential;

    public Transmitter(double potiential) {
        this.potiential = new ElectricPotiential(potiential);
    }
    
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
