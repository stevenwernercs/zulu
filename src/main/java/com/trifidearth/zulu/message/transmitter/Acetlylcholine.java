/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trifidearth.zulu.message.transmitter;

import org.apache.log4j.Logger;



/**
 *
 * @author iSteve
 */
public class Acetlylcholine extends Transmitter{

    private static final Logger log = Logger.getLogger(Acetlylcholine.class);
    
    public Acetlylcholine() {
        super(8.4D);
        this.lifespan +=10000L;
        log.info("lifespan = "+ (lifespan-System.currentTimeMillis()));
    }
    
}
