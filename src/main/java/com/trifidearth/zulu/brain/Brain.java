/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trifidearth.zulu.brain;

import com.sun.istack.internal.logging.Logger;
import com.trifidearth.zulu.coordinate.Coordinate;
import com.trifidearth.zulu.coordinate.CoordinateBounds;
import com.trifidearth.zulu.message.transmitter.Transmitters;
import com.trifidearth.zulu.neuron.Neuron;
import com.trifidearth.zulu.neuron.NeuronType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author iSteve
 */
public class Brain {
    
    private static final Logger log = Logger.getLogger(Brain.class);
    Collection<Neuron> neurons;
    CoordinateBounds bounds;
    TreeMap<Coordinate, Transmitters> ceribralFluid = new TreeMap<>();
    
    public Brain(CoordinateBounds bounds, int inputs, int outputs) {
        this.bounds = bounds;
        this.neurons = new ArrayList<>();
        for(int i = 0; i < inputs; i++) {
            this.neurons.add(new Neuron(this, "S"+i, NeuronType.SENSORY, new Coordinate(bounds)));
        }
        for(int i = 0; i < outputs; i++) {
            this.neurons.add(new Neuron(this, "M"+i, NeuronType.MOTOR, new Coordinate(bounds)));
        }
    }

    public CoordinateBounds getBounds() {
        return bounds;
    }
    
    public void update() {
        for(Neuron neuron : neurons){ 
            neuron.update();
        }
    }
    
    public void grow() {
        for(Neuron neuron : neurons){ 
            neuron.grow(bounds);
        }
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
            log.info(transmitters + " relayed!");
        }
        return transmitters;
    }

    public void depositTranmitters(Coordinate coordinate, Transmitters transmitters) {
        if(ceribralFluid.containsKey(coordinate)){
            ceribralFluid.get(coordinate).getTransmitters().addAll(transmitters.getTransmitters());
        } else {
            ceribralFluid.put(coordinate, transmitters);
        }
        
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
    
    private StringBuilder repeatChar(char c, int n){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < n; i++){
            sb.append(c);
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
            sb.append(repeatChar('\n', newLinesNeeded));
            //restart new line reset column indicator
            xprev = bounds.getRadius() * -1;
        }
        sb.append(repeatChar('\t', next.getX()-xprev));
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
    
    public static void main(String args []) throws InterruptedException{
        Coordinate orgin = new Coordinate(0, 0, 0);
        CoordinateBounds bounds = new CoordinateBounds(orgin, 5);
        Brain brain = new Brain(bounds, 3, 1);
        log.info("Initial Brain State:\n"+brain.toString());
        int iteration = 0;
        while(true) {
            Thread.sleep(100);
            brain.update();
            if(iteration % 5 == 0) {
                brain.grow();
            }
            log.info("Brain State " + iteration + ":\n"+brain.toString()+"\n"+brain.getPlot());
            iteration++;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Brain: ")
                .append(neurons.size())
                .append(" neurons, ")
                .append( getTransmitterCount())
                .append(" transmitters, ")
                .append(bounds.toString());
        for(Neuron neuron : neurons) {
            sb.append("\n\t").append(neuron.toString());
        }
        return sb.toString();
    }

    private int getTransmitterCount() {
        int ret = 0;
        for(Transmitters t : ceribralFluid.values()){
            ret += t.getTransmitters().size();
        }
        return ret;
    }
}
