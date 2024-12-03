package com.navercorp.pinpoint.banner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

class PinpointBannerTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    void printBanner() {
        Properties properties = new Properties();
        properties.put("aaa", "111");
        properties.put("bbb", "222");
        List<String> dumpKeys = keyList(properties);

        PinpointBanner.Builder builder = PinpointBanner.newBuilder();
        builder.setBannerMode(Mode.LOG);
        builder.setDumpKeys(dumpKeys);
        builder.setProperties(properties::getProperty);
        builder.setLoggerWriter(logger::info);
        Banner banner = builder.build();
        banner.printBanner();
    }

    private List<String> keyList(Properties properties) {
        @SuppressWarnings("unchecked")
        Enumeration<String> enumeration = (Enumeration<String>) properties.propertyNames();
        return Collections.list(enumeration);
    }
}