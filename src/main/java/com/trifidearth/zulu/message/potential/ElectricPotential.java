/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trifidearth.zulu.message.potential;

import com.trifidearth.zulu.message.Message;

/**
 *
 * @author iSteve
 */
public class ElectricPotential extends Message{

    private double potentialVoltage;

    private ElectricPotential() {
    }

    public ElectricPotential(double potentialVoltage) {
        this.potentialVoltage = potentialVoltage;
    }

    public void absorb(ElectricPotential input) {
        this.potentialVoltage += input.potentialVoltage;
        input.potentialVoltage = 0;
    }

    public double getPotentialVoltage() {
        return potentialVoltage;
    }

    public void setPotentialVoltage(double potentialVoltage) {
        this.potentialVoltage = potentialVoltage;
    }

    @Override
    public String toString() {
        return "ElectricPotential{" + "potentialVoltage=" + potentialVoltage + '}';
    }

}
