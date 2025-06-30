package com.navercorp.pinpoint.common.profiler.sql;

public final class Tokens {
    public static final char SEPARATOR = ',';
    public static final char SYMBOL_REPLACE = '$';
    public static final char NUMBER_REPLACE = '#';

    public static final int NEXT_TOKEN_NOT_EXIST = -1;
    public static final int NORMALIZED_SQL_BUFFER = 32;

    private Tokens() {
        // prevent instantiation
    }
}
