package com.trifidearth.zulu.coordinate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class CoordinatePairTest {

    @Test
    void growRandomStaysWithinBounds() {
        Coordinate origin = new Coordinate(0, 0, 0);
        CoordinateBounds bounds = new CoordinateBounds(origin, 10);
        CoordinatePair pair = new CoordinatePair(new Coordinate(0, 0, 0));
        for (int i = 0; i < 100; i++) {
            pair.growRandom(1.0, bounds);
            assertFalse(bounds.outOf(pair.getGrowing()));
        }
    }
}

