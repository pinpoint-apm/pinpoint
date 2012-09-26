package com.profiler.server;
//
//import org.apache.log4j.PropertyConfigurator;
//import org.apache.log4j.xml.DOMConfigurator;

import java.net.URL;

public class Log4jConfigurer {
    public static final String XML_FILE_EXTENSION = ".xml";

    public static void configure(String location) {
        URL resource = getURL(location);
        if(location.toLowerCase().endsWith(XML_FILE_EXTENSION)) {
//            DOMConfigurator.configure(resource);
        } else {
//            PropertyConfigurator.configure(resource);
        }
    }

    private static URL getURL(String location) {
        return Log4jConfigurer.class.getClassLoader().getResource(location);
    }
}
