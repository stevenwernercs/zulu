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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author iSteve
 */
public class Brain {
    
    private static final Logger log = LoggerFactory.getLogger(Brain.class);
    Collection<Neuron> neurons;
    CoordinateBounds bounds;
    private int transmitterCount;
    private int deadTransmitterCount;
    ConcurrentSkipListMap<Coordinate, Transmitters> cerebralFluid = new ConcurrentSkipListMap<>();
    public PrintStream out;
    
    public Brain(CoordinateBounds bounds, int inputs, int relay, int outputs) throws UnsupportedEncodingException {
        out = System.out;
        this.bounds = bounds;
        this.neurons = new ArrayList<>();
        System.out.println("Brain: constructing with inputs="+inputs+", relay="+relay+", outputs="+outputs+", bounds="+bounds+");
        System.out.flush();
        for (int i = 'a'; i < inputs + 'a'; i++) {
            System.out.println("Brain: creating sensory neuron '" + (char)i + "'..."); System.out.flush();
            Neuron sensor = new Neuron(this, i, NeuronType.SENSORY, new Coordinate(bounds), 1, 5);
            this.neurons.add(sensor);
        }
        for (int i = 0; i < relay; i++) {
            System.out.println("Brain: creating inter neuron #" + i + "..."); System.out.flush();
            Neuron inter = new Neuron(this, i, NeuronType.INTER_NEURON, new Coordinate(bounds), 5, 5);
            this.neurons.add(inter);
        }
        for (int i = 'A'; i < outputs + 'A'; i++) {
            System.out.println("Brain: creating motor neuron '" + (char)i + "'..."); System.out.flush();
            Neuron motor = new Neuron(this, i, NeuronType.MOTOR, new Coordinate(bounds), 5, 1);
            this.neurons.add(motor);
        }
        System.out.println("Brain: construction complete. total neurons="+this.neurons.size()); System.out.flush();
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
    
    public Transmitters pollNearByTransmitters(Coordinate coordinate) {
        Transmitters transmitters = cerebralFluid.get(coordinate);
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
    public Transmitters retrieveNearByTransmitters(Coordinate coordinate) {
        Transmitters transmitters = cerebralFluid.remove(coordinate);
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

    public void depositTransmitters(Coordinate coordinate, Transmitters transmitters) {
        Transmitters value;
        if((value = cerebralFluid.get(coordinate)) != null){
            transmitters.getTransmitters().addAll(value.getTransmitters());
        }
        cerebralFluid.put(coordinate, transmitters);
        
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
        for(Map.Entry<Coordinate, Transmitters> entry : cerebralFluid.entrySet()) {
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
        Coordinate origin = new Coordinate(0, 0, 0);
        CoordinateBounds bounds = new CoordinateBounds(origin, 10);
        Brain brain = new Brain(bounds, 1, 2, 1);
        log.info("Initial Brain State:"+System.lineSeparator()+brain.toString());
        int iteration = 0;
        brain.start();
            while(true) {
                Thread.sleep(1000);
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
        for(Transmitters t : cerebralFluid.values()){
            count += t.getTransmitters().size();
            dead += t.countZeroPotentials();
        }
        transmitterCount=count;
        deadTransmitterCount = dead;
    }
}
