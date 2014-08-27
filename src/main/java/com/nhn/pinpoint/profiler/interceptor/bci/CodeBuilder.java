package com.nhn.pinpoint.profiler.interceptor.bci;

import java.util.Formatter;

/**
 * @author emeroad
 */
public class CodeBuilder {

    private final StringBuilder codeBlock;
    private final Formatter formatter;

    public CodeBuilder() {
        this(1024);
    }

    public CodeBuilder(int bufferSize) {
        this.codeBlock = new StringBuilder(bufferSize);
        this.formatter = new Formatter(codeBlock);
    }

    public void begin() {
        codeBlock.append('{');
    }

    public void end() {
        codeBlock.append('}');
    }

    public void append(String code) {
        codeBlock.append(code);
    }

    public void format(String format, Object... args) {
        formatter.format(format, args);
    }

    public String toString() {
        return codeBlock.toString();
    }

}
