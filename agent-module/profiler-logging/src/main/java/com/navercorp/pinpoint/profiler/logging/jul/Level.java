package com.navercorp.pinpoint.profiler.logging.jul;

public class Level {
    public static final Level SEVERE = new Level("SEVERE",1000);

    public static final Level WARNING = new Level("WARNING", 900);

    public static final Level INFO = new Level("INFO", 800);

    public static final Level CONFIG = new Level("CONFIG", 700);

    public static final Level FINE = new Level("FINE", 500);

    public static final Level FINER = new Level("FINER", 400);

    public static final Level FINEST = new Level("FINEST", 300);

    private final String name;
    private final int value;

    Level(String name, int value) {
        if (name == null) {
            throw new NullPointerException();
        }
        this.name = name;
        this.value = value;
    }

    public final int intValue() {
        return value;
    }
}
