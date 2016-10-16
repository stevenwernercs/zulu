/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trifidearth.zulu.message.transmitter;

/**
 *
 * @author iSteve
 */
public class Dopamine extends Transmitter{
    
    public Dopamine() {
        super(-6D);
        this.lifespan+=12000L;
    }
}
