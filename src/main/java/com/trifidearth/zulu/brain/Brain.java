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

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

import com.trifidearth.zulu.utils.Utils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author iSteve
 */
public class Brain {
    
    private static final Logger log = Logger.getLogger(Brain.class);
    Collection<Neuron> neurons;
    Map<String, Neuron> sensors;
    CoordinateBounds bounds;
    private int transmitterCount;
    private int deadTransmitterCount;
    ConcurrentSkipListMap<Coordinate, Transmitters> cerebralFluid = new ConcurrentSkipListMap<>();
    public PrintStream out;
    
    public Brain(CoordinateBounds bounds, int inputs, int relay, int outputs) throws UnsupportedEncodingException {
        out = System.out;
        this.bounds = bounds;
        this.neurons = new ArrayList<>(inputs + relay + outputs);
        this.sensors = new HashMap<>(inputs);
        for (int i = 'a'; i < inputs + 'a'; i++) {
            Neuron sensor = new Neuron(this, i, NeuronType.SENSORY, new Coordinate(bounds));
            log.trace("Created new Neuron: " + sensor);
            this.neurons.add(sensor);
            this.sensors.put(String.valueOf(Character.toChars(i)), sensor);
        }
        for (int i = '0'; i < relay + '0'; i++) {
            Neuron inter = new Neuron(this, i, NeuronType.INTER_NEURON, new Coordinate(bounds));
            log.trace("Created new Neuron: " + inter);
            this.neurons.add(inter);
        }
        for (int i = 'A'; i < outputs + 'A'; i++) {
            Neuron motor = new Neuron(this, i, NeuronType.MOTOR, new Coordinate(bounds));
            log.trace("Created new Neuron: " + motor);
            this.neurons.add(motor);
        }
    }

    public Brain(File jsonFile) throws IOException {
        this.out = System.out;
        String jsonString = Utils.readFile(jsonFile);
        JSONObject json = new JSONObject(jsonString);
        this.bounds = new CoordinateBounds(json.getJSONObject("bounds"));

        JSONArray neurons = json.getJSONArray("neurons");
        this.neurons = new ArrayList<>(neurons.length());
        this.sensors = new HashMap<>(neurons.length());
        for(Object obj : neurons) {
           JSONObject neuronObj = (JSONObject)obj;
           Neuron neuron = new Neuron(this, neuronObj);
           if(neuron.type.equals(NeuronType.SENSORY)) {
               this.sensors.put(String.valueOf(Character.toChars(neuron.name)), neuron);
           }
           this.neurons.add(neuron);
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

    public Transmitters pollNearByTransmitters(Coordinate coordinate) {
        Transmitters transmitters = cerebralFluid.get(coordinate);
        if (transmitters == null){
            transmitters = new Transmitters();
        } else {
            transmitters.update();  //remove old ones!
        }
        if (transmitters.isEmpty()) {
            cerebralFluid.remove(coordinate);
        }
        return transmitters;
    }
    
    /**
     * 
     * @param coordinate
     * @return null or list containing 0 or more transmitters
     */
    public Transmitters retrieveNearByTransmitters(Coordinate coordinate) {
        Transmitters transmitters = cerebralFluid.remove(coordinate); //remove %
        if (transmitters == null){
            transmitters = new Transmitters();
        } else {
            transmitters.update();  //remove old ones!
        }
        if(!transmitters.getTransmitters().isEmpty()) {
            log.trace(transmitters + " relayed!");
            log.info("t"+transmitters.getTransmitters().size()+">");
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

    public void serialize(File output) throws FileNotFoundException {
        JSONObject brain = new JSONObject();

        brain.put("bounds", this.bounds.toJson());

        JSONArray neurons = new JSONArray();
        for(Neuron neuron : this.neurons) {
            neurons.put(neuron.toJson());
        }
        brain.put("neurons", neurons);

        log.trace(brain.toString(2));
        try (PrintWriter out = new PrintWriter(output)) {
            out.println(brain.toString(2));
        }
    }

    public void stimulate(String key) {
        if(sensors.containsKey(key)) {
            sensors.get(key).stimulate(30d);

        }
    }

    public static void main(String args []) throws InterruptedException, IOException {

        Brain brain;
        String output = "brain.json";
        if(args.length == 0) {
            Coordinate orgin = new Coordinate(0, 0, 0);
            CoordinateBounds bounds = new CoordinateBounds(orgin, 2);
            brain = new Brain(bounds, 5, 10, 26);
        } else {
            brain = new Brain(new File(args[0]));
            output = args[0] + "_new";
        }
        KeyboardEnvironment env = new KeyboardEnvironment(brain);
        Thread envT = new Thread(env);
        envT.start();
        log.info("Initial Brain State:"+System.lineSeparator()+brain.toString());
        int iteration = 0;
        brain.start();
        int stim = 0;
            while(true) {
                int i = 0;
                for(Neuron sensor : brain.sensors.values()) {
                    if(i++ == stim) {
                        sensor.stimulate(30d);
                        break;
                    }
                }
                stim = (stim + 1) % brain.sensors.size();
                Thread.sleep(5000);
                brain.grow();
                brain.visceralCleansing();
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

    public void visceralCleansing() {
        Set<Coordinate> remove = new HashSet<>();
        int count = 0;
        int dead = 0;
        for(Map.Entry<Coordinate, Transmitters> transmittersEntry : cerebralFluid.entrySet()) {
            transmittersEntry.getValue().update();
            if(transmittersEntry.getValue().isEmpty()) {
                remove.add(transmittersEntry.getKey());
            } else {
                count += transmittersEntry.getValue().size();
                dead += transmittersEntry.getValue().countZeroPotentials();
            }
        }
        transmitterCount = count;
        deadTransmitterCount = dead;
        for(Coordinate coordinate : remove) {
            cerebralFluid.remove(coordinate);
        }
    }

    public String info() {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        int r = 0;
        int o = 0;

        int s = 0;
        int d = 0;
        for(Neuron neuron : neurons) {
            switch (neuron.type) {
                case SENSORY: i++; break;
                case INTER_NEURON: r++; break;
                case MOTOR: o++; break;
            }
            s+=neuron.synapseCount();
            d+=neuron.dendriteCount();
        }

        sb.append("n-").append(neurons.size())
          .append("_s-").append(s)
          .append("_d-").append(d)
          .append("_i-").append(i)
          .append("_r-").append(r)
          .append("_o-").append(o)
          .append("_b-").append(bounds.info());

        return sb.toString();
    }
}
