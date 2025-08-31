package com.trifidearth.zulu.utils;

public final class Utils {

    private Utils() {}

    public static double getSecondOfMillis(long millis) {
        return millis / 1000.0;
    }
}

