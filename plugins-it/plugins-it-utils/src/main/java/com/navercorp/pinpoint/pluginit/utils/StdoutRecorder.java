package com.navercorp.pinpoint.pluginit.utils;

import com.navercorp.pinpoint.common.util.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Objects;

public class StdoutRecorder {

    public String record(Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable");

        final PrintStream originalOut = System.out;

        final OutputStream stream = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(stream);

        System.setOut(printStream);
        try {
            runnable.run();
            return stream.toString();
        } finally {
            System.setOut(originalOut);
            IOUtils.closeQuietly(printStream);
            IOUtils.closeQuietly(stream);
        }
    }

}
