package com.navercorp.pinpoint.banner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;


class BannerSupplierTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    void banner() {
        String banner = new BannerSupplier().get();
        logger.debug("--------");
        logger.debug(banner);
        logger.debug("--------");
    }

}