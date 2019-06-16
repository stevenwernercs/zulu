/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trifidearth.zulu.neuron;

import com.trifidearth.zulu.coordinate.CoordinatePair;
import com.trifidearth.zulu.coordinate.Coordinate;
import com.trifidearth.zulu.brain.Brain;
import com.trifidearth.zulu.coordinate.CoordinateBounds;
import com.trifidearth.zulu.message.potiential.ActionPotiential;
import com.trifidearth.zulu.message.potiential.ElectricPotiential;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import render.sound.SoundUtils;

import javax.sound.sampled.LineUnavailableException;

/**
 *
 * @author iSteve
 */
public class Neuron extends Node implements Listening, Grows, Runnable{

    private static final Logger log = Logger.getLogger(Neuron.class);
    Brain brain;
    public final int name;
    int storagedEnergy = 0;
    public final NeuronType type;
    ConcurrentLinkedQueue<Dendrite> dendrites;
    Soma soma;
    Axon axon;
    ConcurrentLinkedQueue<Synapse> synapses;

    public Neuron(Brain brain, int name, NeuronType type, Coordinate fixedPoint) {
        super(fixedPoint);
        this.brain = brain;
        this.name = name;
        this.type =  type;
        this.storagedEnergy = 0;
        init(fixedPoint, brain.getBounds());
    }

    public Neuron(Brain brain, JSONObject neuronObj) {
        super(new Coordinate(neuronObj.getJSONObject("soma")));
        this.brain = brain;
        this.name = neuronObj.getInt("name");
        this.type = NeuronType.valueOf(neuronObj.getString("type"));
        this.soma = new Soma(new CoordinatePair(super.getfixedPoint()), new ElectricPotiential(Soma.RESTING_POTIENTIAL));
        this.dendrites = new ConcurrentLinkedQueue<>();
        for(Object obj : neuronObj.getJSONArray("dendrites")) {
            JSONObject dendriteObj = (JSONObject)obj;
            Dendrite dendrite = new Dendrite(new Coordinate(dendriteObj));
            this.dendrites.add(dendrite);
        }
        this.axon = new Axon(this.soma.getFixed(), neuronObj.getJSONObject("axon"));
        this.synapses = new ConcurrentLinkedQueue<>();
        for(Object obj : neuronObj.getJSONArray("synapses")) {
            JSONObject synapseObj = (JSONObject)obj;
            Synapse synapse = new Synapse(new Coordinate(synapseObj), this);
            this.synapses.add(synapse);
        }
        bringToLife();
    }

    private void init(Coordinate coordinate, CoordinateBounds bounds){
        CoordinatePair point = new CoordinatePair(coordinate);
        soma = new Soma(point, new ElectricPotiential(Soma.RESTING_POTIENTIAL));
        dendrites = new ConcurrentLinkedQueue<>();
        if(!type.equals(NeuronType.SENSORY)) {
            dendrites.add(new Dendrite(soma.getFixed()));
        }
        axon = new Axon(point);
        axon.grow(brain.getBounds());
        synapses = new ConcurrentLinkedQueue<>();
        if(!type.equals(NeuronType.MOTOR)) {
            synapses.add(new Synapse(axon.getGrowing(), this));
        }
        grow(bounds);
        bringToLife();
    }

    @Override
    public void update() {
        log.trace(this.type.name() + " Neuron " + String.valueOf(Character.toChars(name)) + " @ " + this.soma.potiential);
        double sum = 0d;
        for(Dendrite each : dendrites) {
            sum += each.propagate(brain.retrieveNearByTransmitters(each.getGrowing())).getPotientialVoltage();
        }
        ElectricPotiential dendritalSum = new ElectricPotiential(sum);
        log.trace(this.type.name() + " pulled in " + dendritalSum);
        ActionPotiential axonIn = soma.propagate(dendritalSum);
        if(axonIn != null){
            try {
                SoundUtils.play_tone(this.name * 100, 500, type.equals(NeuronType.SENSORY) ? 0.1 : 1.0);
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
            this.storagedEnergy++;
            if(NeuronType.MOTOR.equals(type)) {
                log.info("##########FIRE###########: " + String.valueOf(Character.toChars(name)));
            } else {
                log.info("#PROPAGATE#: " + type.name() + ": " + String.valueOf(Character.toChars(name)));
                ActionPotiential axonOut = axon.propagate(axonIn);
                for (Synapse each : synapses) {
                    brain.depositTransmitters(each.getGrowing(), each.propagate(axonOut));
                }
            }
        }
    }

    @Override
    public void grow(CoordinateBounds bounds) {
        if (canGrow()) {
            storagedEnergy -= 1;
            if(!type.equals(NeuronType.SENSORY)) {
                if(Math.random() < (1D/(dendrites.size()+1D))) {
                    dendrites.add(new Dendrite(soma.getFixed()));
                }
                for(Dendrite each : dendrites) {
                    each.grow(bounds);
                }
            }
            if(!type.equals(NeuronType.MOTOR)) {
                if (Math.random() < (1D / (synapses.size() + 1D))) {
                    synapses.add(new Synapse(axon.getGrowing(), this));
                }
                for (Synapse each : synapses) {
                    each.grow(bounds);
                }
            }
        }
    }

    private boolean canGrow() {
        return storagedEnergy > (this.dendrites.size() + this.synapses.size());
    }

    public Map<Coordinate,List<String>> getNodeLocationMap() {
        Map<Coordinate,List<String>> map = new HashMap();
        Map map2 = new HashMap();
        appendAtLocation(map, soma.getfixedPoint(), name+"_N");
        appendAtLocation(map, axon.getGrowing(), name+"_A");
        for(Dendrite d : dendrites) {
            appendAtLocation(map, d.getGrowing(), name+"_D");    
        }
        for(Synapse s : synapses) {
            appendAtLocation(map, s.getGrowing(), name+"_S");    
        }
        return map;
    }

    public void stimulate(double mv) {
        this.soma.potiential.absorb(mv);
    }
    
    private static Map<Coordinate,List<String>> appendAtLocation(Map<Coordinate,List<String>> map,
            Coordinate coordinate, String name){
        if(map.containsKey(coordinate)) {
            map.get(coordinate).add(name);
        } else {
            List<String> newList = new LinkedList<>();
            newList.add(name);
            map.put(coordinate, newList);
        }
        return map;
    }

    @Override
    public String toString() {
        return "Neuron{" + "name=" + name + ", type=" + type + ", potiential=" + soma.potiential + ", energy= " + storagedEnergy + ", dendrites@" + dendrites.size() + ", axon@" + axon.distance + ", synapses@" + synapses.size() + '}';
    }

    @Override
    public void run() {
        while(this.isAlive()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                log.warn("InterruptedException", ex);
            }
            log.trace("Updating " + name);
            update();
        }
    }

    @Override
    public JSONObject toJson() {
        JSONObject neuron = new JSONObject();

        neuron.put("type", this.type.name());
        neuron.put("name", this.name);

        neuron.put("soma", this.soma.toJson());

        JSONArray dendrites = new JSONArray();
        for(Dendrite dendrite : this.dendrites) {
            dendrites.put(dendrite.toJson());
        }
        neuron.put("dendrites", dendrites);

        neuron.put("axon", this.axon.toJson());

        JSONArray synapses = new JSONArray();
        for(Synapse synapse : this.synapses) {
            synapses.put(synapse.toJson());
        }
        neuron.put("synapses", synapses);

        return neuron;
    }

    public int synapseCount() {
        return this.synapses.size();
    }

    public int dendriteCount() {
        return this.dendrites.size();
    }
}
