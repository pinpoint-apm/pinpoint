package com.profiler.common;

import java.util.HashMap;
import java.util.Map;

/**
 * @author netspider
 */
public enum AnnotationKey {


    API_DID(10, "API-DID"),
    API_ID(11, "API-ID"),
    API(12, "API"),
    API_METADATA(13, "API-METADATA"),
    // 자동 id

    SQL_ID(20, "SQL-ID"),
    SQL(21, "SQL", true),
    SQL_METADATA(22, "SQL-METADATA"),
    SQL_PARAM(23, "SQL-PARAMS"),
    SQL_BINDVALUE(24, "SQL-BindValue", true),

    STRING_ID(30, "STRING_ID"),

    HTTP_URL(40, "http.url"),
    HTTP_PARAM(41, "http.params"),

    ARCUS_COMMAND(50, "arcus.command"),

    ARGS0(-1, "args[0]"),
    ARGS1(-2, "args[1]"),
    ARGS2(-3, "args[2]"),
    ARGS3(-4, "args[3]"),
    ARGS4(-5, "args[4]"),
    ARGS5(-6, "args[5]"),
    ARGS6(-7, "args[6]"),
    ARGS7(-8, "args[7]"),
    ARGS8(-9, "args[8]"),
    ARGS9(-10, "args[9]"),
    ARGSN(-11, "args[N]"),

    EXCEPTION(-50, "Exception", true),
    UNKNOWN(-9999, "UNKNOWN");

    private int code;
    private String value;
    private boolean viewInRecordSet;

    public final static int MAX_ARGS_SIZE = 10;

    private AnnotationKey(int code, String value) {
        this(code, value, false);
    }

    private AnnotationKey(int code, String value, boolean viewInRecordSet) {
        this.code = code;
        this.value = value;
        this.viewInRecordSet = viewInRecordSet;
    }

    public String getValue() {
        return value;
    }

    public int getCode() {
        return code;
    }

    public boolean isViewInRecordSet() {
        return viewInRecordSet;
    }

    private static final Map<Integer, AnnotationKey> CODE_LOOKUP_TABLE = new HashMap<Integer, AnnotationKey>();
    static {
        initializeLookupTable();
    }

    public static void initializeLookupTable() {
        AnnotationKey[] values = AnnotationKey.values();
        for (AnnotationKey name : values) {
            AnnotationKey check = CODE_LOOKUP_TABLE.put(name.getCode(), name);
            if (check != null) {
                throw new IllegalStateException("duplicated code found. code:" + name.getCode());
            }
        }
    }

    public static AnnotationKey findAnnotationKey(int code) {
        AnnotationKey annotationKey = CODE_LOOKUP_TABLE.get(code);
        if (annotationKey == null) {
            return UNKNOWN;
        }
        return annotationKey;
    }

    public static AnnotationKey getArgs(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("negative index:" + index);
        }
        switch (index) {
            case 0:
                return ARGS0;
            case 1:
                return ARGS1;
            case 2:
                return ARGS2;
            case 3:
                return ARGS3;
            case 4:
                return ARGS4;
            case 5:
                return ARGS5;
            case 6:
                return ARGS6;
            case 7:
                return ARGS7;
            case 8:
                return ARGS8;
            case 9:
                return ARGS9;
            default:
                return ARGSN;
        }
    }
}
