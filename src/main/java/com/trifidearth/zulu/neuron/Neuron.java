/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trifidearth.zulu.neuron;

import com.sun.istack.internal.logging.Logger;
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
import java.util.logging.Level;

/**
 *
 * @author iSteve
 */
public class Neuron extends Node implements Listening, Grows, Runnable{

    private static final Logger log = Logger.getLogger(Neuron.class);
    Brain brain;
    int name;
    NeuronType type;
    ConcurrentLinkedQueue<Dendrite> dentrites;
    Soma soma;
    Axon axon;
    ConcurrentLinkedQueue<Synapse> synapses;

    public Neuron(Brain brain, int name, NeuronType type, Coordinate fixedPoint) {
        super(fixedPoint);
        this.brain = brain;
        this.name = name;
        this.type =  type;
        init(fixedPoint, brain.getBounds());
    }

    @Override
    public void update() {
        ElectricPotiential dentritalSum = new ElectricPotiential(0);
        for(Dendrite each : dentrites) {
            dentritalSum.absorb(each.propagate(brain.retreveNearByTranmitters(each.getGrowing())));
        }
        ActionPotiential axonIn = soma.propagate(dentritalSum);
        if(axonIn != null){
            if(NeuronType.MOTOR.equals(type)) {
                brain.out.println(String.valueOf(Character.toChars(name)));
            }
            ActionPotiential axonOut = axon.propagate(axonIn);
            for(Synapse each : synapses) {
                brain.depositTranmitters(each.getGrowing(), each.propagate(axonOut));
            }
        }
    }
    
    private void init(Coordinate coordinate, CoordinateBounds bounds){
        CoordinatePair point = new CoordinatePair(coordinate);
        dentrites = new ConcurrentLinkedQueue<>();
        soma = new Soma(point, new ElectricPotiential(0));
        axon = new Axon(point);
        axon.grow(brain.getBounds());
        synapses = new ConcurrentLinkedQueue<>();
        grow(bounds);
        bringToLife();
    }

    @Override
    public void grow(CoordinateBounds bounds) {
        if(Math.random() < (1D/(dentrites.size()+1D))) {
            dentrites.add(new Dendrite(soma.getFixed()));
        }
        for(Dendrite each : dentrites) {
            each.grow(bounds);
        }
        if(Math.random() < (1D/(synapses.size()+1D))) {
            synapses.add(new Synapse(axon.getGrowing(), this));
        }
        for(Synapse each : synapses) {
            each.grow(bounds);
        }
    }

    public Map<Coordinate,List<String>> getNodeLocationMap() {
        Map<Coordinate,List<String>> map = new HashMap();
        Map map2 = new HashMap();
        appendAtLocation(map, soma.getfixedPoint(), name+"_N");
        appendAtLocation(map, axon.getGrowing(), name+"_A");
        for(Dendrite d : dentrites) {
            appendAtLocation(map, d.getGrowing(), name+"_D");    
        }
        for(Synapse s : synapses) {
            appendAtLocation(map, s.getGrowing(), name+"_S");    
        }
        return map;
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
        return "Neuron{" + "name=" + name + ", type=" + type + ", dentrites@" + dentrites.size() + ", axon@" + axon.distance + ", synapses@" + synapses.size() + '}';
    }

    @Override
    public void run() {
        while(this.isAlive()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                log.warning("InterruptedException", ex);
            }
            log.finest("Updating " + name);
            update();
        }
    }
    
}
