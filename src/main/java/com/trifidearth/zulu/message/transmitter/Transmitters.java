/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trifidearth.zulu.message.transmitter;

import com.trifidearth.zulu.message.Message;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author iSteve
 */
public class Transmitters extends Message {

    private final List<Transmitter> transmitterList = new LinkedList<>();
    private static List<Transmitter> avalibleTransmitters = new ArrayList<>(Arrays.asList(
        new Acetlylcholine(),
        new Adrenaline(),
        new Dopamine(),
        new Endorphins(),
        new Gaba(),
        new Glutamate(),
        new Noradrenaline(),
        new Serotonin()
    ));
   
    public static Transmitters getRandomTransmitters() {
        Transmitters t = new Transmitters();
        for (int i = (int)(Math.random() * 8); i > 0; i--){
            t.getTransmitters().add(avalibleTransmitters.get((int)(Math.random()*avalibleTransmitters.size())));
        }
        return t;
    }

    public Transmitters(Transmitter... transmitters) {
        for(Transmitter each : transmitters) {
            this.transmitterList.add(each);
        }
    }

    public List<Transmitter> getTransmitters() {
        return transmitterList;
    }
    
    public List<String> getTransmittersAsStrings() {
        return transmitterList.stream().map(t -> t.toString()).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Transmitters{" + transmitterList.size() + '}';
    }
    
}
