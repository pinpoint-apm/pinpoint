package com.navercorp.pinpoint.banner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;


class BannerUtilsTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    void banner() {
        String banner = BannerUtils.banner();
        logger.debug("--------");
        logger.debug(banner);
        logger.debug("--------");
    }

}