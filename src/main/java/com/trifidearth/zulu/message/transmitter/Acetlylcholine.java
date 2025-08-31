/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trifidearth.zulu.message.transmitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 *
 * @author iSteve
 */
public class Acetlylcholine extends Transmitter{

    private static final Logger log = LoggerFactory.getLogger(Acetlylcholine.class);
    
    public Acetlylcholine() {
        super("Acetlylcholine", 18.4D, 10, 60);
    }
    
}
