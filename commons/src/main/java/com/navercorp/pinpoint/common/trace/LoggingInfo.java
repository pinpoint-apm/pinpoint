package com.navercorp.pinpoint.common.trace;

import com.navercorp.pinpoint.common.util.apache.IntHashMap;

public enum LoggingInfo {

    NOT_LOGGED((byte)0, "NOT_LOGGED", "message is not logged"),
    
    LOGGED((byte)1, "LOGGED", "message is logged"),

    //log level for log4j, slf4j
    ALL((byte)11, "ALL", "ALL log level"),

    TRACE((byte)12, "TRACE", "TRACE log level"),
  
    DEBUG((byte)13, "DEBUG", "DEBUG log level"),
  
    INFO((byte)14, "INFO", "INFO log level"),
  
    WARN((byte)15, "WARN", "WARN log level"),

    ERROR((byte)16, "ERROR", "ERROR log level"),

    FATAL((byte)17, "FATAL", "FATAL log level"),

    OFF((byte)18, "OFF", "OFF log level"),
  
    // log level for java   
    FINEST_JAVA((byte)31, "FINEST", "SEVERE log level"),

    FINER_JAVA((byte)32, "FINAL", "SEVERE log level"),

    FINE_JAVA((byte)33, "FINE", "SEVERE log level"),
  
    CONFIG_JAVA((byte)34, "CONFIG", "SEVERE log level"),

    INFO_JAVA((byte)35, "INFO", "SEVERE log level"),

    WARNING_JAVA((byte)36, "WARNING", "SEVERE log level"),

    SEVERE_JAVA((byte)37, "SEVERE", "SEVERE log level");
    
    private final byte code;
    private final String name;
    private final String desc;
        
    LoggingInfo(byte code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }
 
    public byte getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }
    
    private final static IntHashMap<LoggingInfo> LOGGING_INFO_MAP = toLoggingInfoByCodeMap();
    
    private static IntHashMap<LoggingInfo> toLoggingInfoByCodeMap() {
        final IntHashMap<LoggingInfo> loggingInfoMap = new IntHashMap<LoggingInfo>();
        for (LoggingInfo loggingInfo : LoggingInfo.values()) {
            loggingInfoMap.put(loggingInfo.getCode(), loggingInfo);
        }
        return loggingInfoMap;
    }

    public static LoggingInfo searchByCode(byte code) {
        return LOGGING_INFO_MAP.get(code);
        
    }
    

}
