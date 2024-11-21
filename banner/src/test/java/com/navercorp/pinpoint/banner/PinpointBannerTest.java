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

        Banner banner = new PinpointBanner(Mode.LOG, dumpKeys,
                properties::getProperty, logger::info);
        banner.printBanner();
    }

    private List<String> keyList(Properties properties) {
        @SuppressWarnings("unchecked")
        Enumeration<String> enumeration = (Enumeration<String>) properties.propertyNames();
        return Collections.list(enumeration);
    }
}