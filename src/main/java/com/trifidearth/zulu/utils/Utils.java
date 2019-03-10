package com.trifidearth.zulu.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class Utils {

    public static double getSecondOfMillis(long milliseconds) {
        return milliseconds / 1000D;
    }

    public static String readFile(File file) throws IOException {
        return readFile(file, Charset.defaultCharset());
    }

    public static String readFile(File file, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(file.getPath()));
        return new String(encoded, encoding);
    }

}
