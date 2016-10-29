/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trifidearth.zulu.message.transmitter;


import com.trifidearth.zulu.message.Message;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;

/**
 *
 * @author iSteve
 */
public class Transmitters extends Message {

    private static final Logger log = Logger.getLogger(Transmitters.class);
    
    private final ConcurrentLinkedQueue<Transmitter> transmitterList = new ConcurrentLinkedQueue<>();
    private static final List<Transmitter> AVALIBLE_TRANSMITTERS = new ArrayList<>(Arrays.asList(
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
        for (int i = (int)(Math.random() * AVALIBLE_TRANSMITTERS.size()); i >= 0; i--){
            t.getTransmitters().add(AVALIBLE_TRANSMITTERS.get((int)(Math.random()*AVALIBLE_TRANSMITTERS.size())));
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
        for(Transmitter each : transmitterList){
            each.checkDesolved();
        }
    }
    
    public List<String> getTransmittersAsStrings() {
        return transmitterList.stream().map(t -> t.toString()).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Transmitters{" + transmitterList.size() + '}';
    }

    public int countNonZeroPotientials() {
        int ret = 0;
        for(Transmitter each : transmitterList){
            if(each.potiential.getPotientialVoltage()!= 0D){
                ret++;
            }
        }
        return ret;
    }
    
    public int countZeroPotientials() {
        int ret = 0;
        for(Transmitter each : transmitterList){
            if(each.potiential.getPotientialVoltage()== 0D){
                log.info("lifespan = "+ (each.lifespan-System.currentTimeMillis()));
                ret++;
            }
        }
        return ret;
    }    
}
