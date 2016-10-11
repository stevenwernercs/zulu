/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trifidearth.zulu.message.transmitter;

import com.trifidearth.zulu.message.Message;

/**
 *
 * @author iSteve
 */
public abstract class Transmitter extends Message {
    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
