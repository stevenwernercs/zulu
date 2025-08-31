package com.trifidearth.zulu.message.transmitter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TransmitterTest {

    static class TestTx extends Transmitter {
        TestTx(double potentialVoltage, float lifespan, float decayspan) {
            super("Test", potentialVoltage, lifespan, decayspan);
        }
    }

    @Test
    void checkDissolvedSetsPotentialToZeroAfterLifespan() {
        TestTx tx = new TestTx(5.0, -1.0f, 60.0f); // already past lifespan
        assertFalse(tx.getElectricPotential().getPotentialVoltage() == 0.0);
        boolean dissolved = tx.checkDissolved();
        assertFalse(dissolved); // not fully decayed yet
        assertEquals(0.0, tx.getElectricPotential().getPotentialVoltage(), 1e-9);
    }

    @Test
    void checkDissolvedReturnsTrueAfterDecay() {
        TestTx tx = new TestTx(5.0, -1.0f, -1.0f); // past lifespan and decay
        boolean removed = tx.checkDissolved();
        assertTrue(removed);
    }
}

