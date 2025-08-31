package com.trifidearth.zulu.coordinate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CoordinateTest {

    @Test
    void distanceUsesXYZ() {
        Coordinate a = new Coordinate(0, 0, 0);
        Coordinate b = new Coordinate(3, 4, 12); // length sqrt(9+16+144) = sqrt(169) = 13
        assertEquals(13.0, a.computeDistanceTo(b), 1e-9);
    }
}

