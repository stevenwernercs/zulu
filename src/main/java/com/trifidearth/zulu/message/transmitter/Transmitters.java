/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trifidearth.zulu.message.transmitter;

import com.trifidearth.zulu.coordinate.Coordinate;
import com.trifidearth.zulu.message.Message;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author iSteve
 */
public class Transmitters extends Message {
    private final List<Transmitter> transmitters = new LinkedList<>();

    public Transmitters(Transmitter... transmitters) {
        for(Transmitter each : transmitters) {
            this.transmitters.add(each);
        }
    }

    public List<Transmitter> getTransmitters() {
        return transmitters;
    }
    
    public List<String> getTransmittersAsStrings() {
        return transmitters.stream().map(t -> t.toString()).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Transmitters{" + transmitters.size() + '}';
    }
    
}
