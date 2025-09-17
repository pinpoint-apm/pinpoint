package com.navercorp.pinpoint.profiler.instrument.scanner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DirectoryScannerTest {

    @Test
    void toPath() {
        DirectoryScanner directoryScanner = new DirectoryScanner("");
        directoryScanner = new DirectoryScanner("/C:/Windows/");
        directoryScanner = new DirectoryScanner("/");

        Assertions.assertThrows(NullPointerException.class, () -> {
            new DirectoryScanner(null);
        });
    }
}