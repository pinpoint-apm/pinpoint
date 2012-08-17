package com.profiler;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

public class Log4jConfigurer {
    public static final String XML_FILE_EXTENSION = ".xml";

    public static void configure(String location) {
        if(location.toLowerCase().endsWith(XML_FILE_EXTENSION)) {
            DOMConfigurator.configure(location);
        } else {
            PropertyConfigurator.configure(location);
        }
    }
}
