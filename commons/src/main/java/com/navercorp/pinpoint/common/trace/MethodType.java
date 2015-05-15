package com.navercorp.pinpoint.common.trace;

public class MethodType {

    // method
    public static final int DEFAULT = 0;
    // exception message
    public static final int EXCEPTION = 1;
    // information
    public static final int ANNOTATION = 2;
    // method parameter
    public static final int PARAMETER = 3;
    // tomcat, jetty, bloc ...
    public static final int WEB_REQUEST = 100;
    // sync/async
    public static final int INVOCATION = 200;

    // database, javascript
    
    // corrupted when : 1. slow network, 2. too much node ...
    public static final int CORRUPTED = 900;
}
