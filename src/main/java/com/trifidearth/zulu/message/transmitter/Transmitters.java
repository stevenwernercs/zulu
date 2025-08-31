/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trifidearth.zulu.message.transmitter;


import com.trifidearth.zulu.message.Message;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author iSteve
 */
public class Transmitters extends Message {

    private static final Logger log = LoggerFactory.getLogger(Transmitters.class);
    
    private final ConcurrentLinkedQueue<Transmitter> transmitterList = new ConcurrentLinkedQueue<>();
    private static final List<Transmitter> AVAILABLE_TRANSMITTERS = new ArrayList<>(Arrays.asList(
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
        for (int i = (int)(Math.random() * AVAILABLE_TRANSMITTERS.size()); i >= 0; i--){
            t.getTransmitters().add(AVAILABLE_TRANSMITTERS.get((int)(Math.random()* AVAILABLE_TRANSMITTERS.size())));
        }
        return t;
    }

    public Transmitters(Transmitter... transmitters) {
        for(Transmitter each : transmitters) {
            this.transmitterList.add(each);
        }
    }

    public ConcurrentLinkedQueue<Transmitter> getTransmitters() {
        return transmitterList;
    }
    
    public void update(){
        Iterator<Transmitter> transmitterIterator = transmitterList.iterator();
        while(transmitterIterator.hasNext()) {
            Transmitter transmitter = transmitterIterator.next();
            if(transmitter.checkDissolved()) {
                transmitterIterator.remove();
            }
        }
    }
    
    public List<String> getTransmittersAsStrings() {
        return transmitterList.stream().map(t -> t.toString()).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Transmitters{" + transmitterList.size() + '}';
    }

    public int countNonZeroPotentials() {
        int ret = 0;
        for(Transmitter each : transmitterList){
            if(each.potential.getPotentialVoltage()!= 0D){
                ret++;
            }
        }
        return ret;
    }
    
    public int countZeroPotentials() {
        int ret = 0;
        for(Transmitter each : transmitterList){
            if(each.potential.getPotentialVoltage()== 0D){
                ret++;
            }
        }
        return ret;
    }    
}
