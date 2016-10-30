/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trifidearth.zulu.brain;

import com.trifidearth.zulu.coordinate.Coordinate;
import com.trifidearth.zulu.coordinate.CoordinateBounds;
import com.trifidearth.zulu.message.transmitter.Transmitters;
import com.trifidearth.zulu.neuron.Neuron;
import com.trifidearth.zulu.neuron.NeuronType;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import org.apache.log4j.Logger;

/**
 *
 * @author iSteve
 */
public class Brain {
    
    private static final Logger log = Logger.getLogger(Brain.class);
    Collection<Neuron> neurons;
    CoordinateBounds bounds;
    private int transmitterCount;
    private int deadTransmitterCount;
    ConcurrentSkipListMap<Coordinate, Transmitters> ceribralFluid = new ConcurrentSkipListMap<>();
    public PrintStream out;
    
    public Brain(CoordinateBounds bounds, int inputs, int outputs) throws UnsupportedEncodingException {
        out = System.out;
        this.bounds = bounds;
        this.neurons = new ArrayList<>();
        for(int i = 32; i < inputs+32; i++) {
            this.neurons.add(new Neuron(this, i, NeuronType.SENSORY, new Coordinate(bounds)));
        }
        for(int i = 32; i < outputs+32; i++) {
            this.neurons.add(new Neuron(this, i, NeuronType.MOTOR, new Coordinate(bounds)));
        }
    }

    public CoordinateBounds getBounds() {
        return bounds;
    }
    
    public void start() {
        for(Neuron neuron : neurons){ 
            Thread t = new Thread(neuron);
            t.setDaemon(true);
            t.start();
        }
    }
    
    public void grow() {
        for(Neuron neuron : neurons){ 
            neuron.grow(bounds);
        }
    }
    
    public Transmitters pollNearByTranmitters(Coordinate coordinate) {
        Transmitters transmitters = ceribralFluid.get(coordinate);
        if (transmitters == null){
            transmitters = new Transmitters();
        } else {
            transmitters.update();  //remove old ones!
        }
        return transmitters;
    }
    
    /**
     * 
     * @param coordinate
     * @return null or list containing 0 or more transmitters
     */
    public Transmitters retreveNearByTranmitters(Coordinate coordinate) {
        Transmitters transmitters = ceribralFluid.remove(coordinate);
        if (transmitters == null){
            transmitters = new Transmitters();
        } else {
            transmitters.update();  //remove old ones!
        }
        if(!transmitters.getTransmitters().isEmpty()) {
            log.trace(transmitters + " relayed!");
            out.print("t"+transmitters.getTransmitters().size()+">");
        }
        return transmitters;
    }

    public void depositTranmitters(Coordinate coordinate, Transmitters transmitters) {
        Transmitters value;
        if((value = ceribralFluid.get(coordinate)) != null){
            transmitters.getTransmitters().addAll(value.getTransmitters());
        }
        ceribralFluid.put(coordinate, transmitters);
        
    }
    
    public TreeMap<Coordinate, List<String>> getNodeLocationMap(){
        TreeMap<Coordinate, List<String>> nodes = new TreeMap<>();
        for(Neuron each : neurons){
            Map<Coordinate, List<String>> neuronMap = each.getNodeLocationMap();
            for(Map.Entry<Coordinate, List<String>> entry : neuronMap.entrySet()){
                if(nodes.containsKey(entry.getKey())){
                    nodes.get(entry.getKey()).addAll(entry.getValue());
                } else {
                    nodes.put(entry.getKey(), entry.getValue());
                }
            }
        }
        for(Map.Entry<Coordinate, Transmitters> entry : ceribralFluid.entrySet()) {
            String transmitterMarker = "T"+entry.getValue().getTransmitters().size();
            if(nodes.containsKey(entry.getKey())) {
                nodes.get(entry.getKey()).add(transmitterMarker);
            } else {
                List<String> newList = new LinkedList<>();
                newList.add(transmitterMarker);
                nodes.put(entry.getKey(), newList);
            }
        }
        return nodes;
    }
    
    private StringBuilder repeatString(String s, int n){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < n; i++){
            sb.append(s);
        }
        return sb;
    }
    
    private StringBuilder getLeadingWhiteSpace(Coordinate prev, Coordinate next) {
        StringBuilder sb = new StringBuilder();
        int newLinesNeeded = prev.getY()-next.getY();
        int xprev;
        assert newLinesNeeded >= 0 : "prev next not in sorted order";
        if(newLinesNeeded==0) {
            xprev = prev.getX();
        } else {
            sb.append(repeatString(System.lineSeparator(), newLinesNeeded));
            //restart new line reset column indicator
            xprev = bounds.getRadius() * -1;
        }
        sb.append(repeatString("\t", next.getX()-xprev));
        return sb;
    }
    
    public String getPlot(){
        TreeMap<Coordinate, List<String>> nodes = getNodeLocationMap();
        StringBuilder sb = new StringBuilder();
        Coordinate prev = Coordinate.getTopLeftFront(bounds);
        for(Map.Entry<Coordinate,List<String>> entry : nodes.descendingMap().entrySet()) {
            sb.append(getLeadingWhiteSpace(prev,entry.getKey())).append(entry.getValue());
            prev = entry.getKey();
        }
        return sb.toString();
    }
    
    public static void main(String args []) throws InterruptedException, UnsupportedEncodingException{
        Coordinate orgin = new Coordinate(0, 0, 0);
        CoordinateBounds bounds = new CoordinateBounds(orgin, 250);
        Brain brain = new Brain(bounds, 10000, 10000);
        log.info("Initial Brain State:"+System.lineSeparator()+brain.toString());
        int iteration = 0;
        brain.start();
            while(true) {
                Thread.sleep(5000);
                brain.grow();
                brain.getTransmitterCount();
                log.info("Brain State " + iteration + System.lineSeparator() + brain.toString());
                log.info(brain.getPlot());
                iteration++;
            }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Brain: ")
                .append(neurons.size())
                .append(" neurons, ")
                .append(transmitterCount)
                .append(" transmitters, ")
                .append("(").append(deadTransmitterCount).append(" dead), ")
                .append(bounds.toString());
        for(Neuron neuron : neurons) {
            sb.append(System.lineSeparator()).append("\t").append(neuron.toString());
        }
        return sb.toString();
    }

    private void getTransmitterCount() {
        int count = 0;
        int dead = 0;
        for(Transmitters t : ceribralFluid.values()){
            count += t.getTransmitters().size();
            dead += t.countZeroPotientials();
        }
        transmitterCount=count;
        deadTransmitterCount = dead;
    }
}
