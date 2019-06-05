/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trifidearth.zulu.message.potiential;

import com.trifidearth.zulu.message.Message;

/**
 *
 * @author iSteve
 */
public class ElectricPotiential extends Message{

    private double potientialVoltage;

    private ElectricPotiential() {
    }

    public ElectricPotiential(double potientialVoltage) {
        this.potientialVoltage = potientialVoltage;
    }
    
    public void absorb(ElectricPotiential input) {
        absorb(input.potientialVoltage);
        input.potientialVoltage = 0;
    }

    public synchronized void absorb(double inputVoltage) {
        updatePotientialVoltage(potientialVoltage + inputVoltage);
    }

    public double getPotientialVoltage() {
        return potientialVoltage;
    }

    public synchronized void forcePotientialVoltage(double potientialVoltage) {
        this.potientialVoltage = potientialVoltage;
    }

    public synchronized void updatePotientialVoltage(double potientialVoltage) {
        //System.out.println(this.potientialVoltage + "<<<<" + potientialVoltage);
        this.potientialVoltage = (this.potientialVoltage + (4d*potientialVoltage)) / 5d;
        //System.out.println(this.potientialVoltage);
    }

    public boolean isEmpty() {
        return potientialVoltage == 0D;
    }

    @Override
    public String toString() {
        return "ElectricPotiential{" + "potientialVoltage=" + potientialVoltage + '}';
    }
    
}
