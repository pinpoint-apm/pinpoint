package com.navercorp.pinpoint.it.plugin.utils;

import org.testcontainers.containers.output.OutputFrame;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class LogOutputStream implements Consumer<OutputFrame> {

    private final BiConsumer<String, String> consumer;

    public LogOutputStream(BiConsumer<String, String> consumer) {
        this.consumer = Objects.requireNonNull(consumer, "consumer");
    }

    @Override
    public void accept(OutputFrame outputFrame) {
        String log = outputFrame.getUtf8StringWithoutLineEnding();
        consumer.accept("{}", log);
    }

}
