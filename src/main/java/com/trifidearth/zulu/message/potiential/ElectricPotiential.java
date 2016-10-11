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
        this.potientialVoltage += input.potientialVoltage;
        input.potientialVoltage = 0;
    }

    public double getPotientialVoltage() {
        return potientialVoltage;
    }

    public void setPotientialVoltage(double potientialVoltage) {
        this.potientialVoltage = potientialVoltage;
    }

    @Override
    public String toString() {
        return "ElectricPotiential{" + "potientialVoltage=" + potientialVoltage + '}';
    }
    
}
