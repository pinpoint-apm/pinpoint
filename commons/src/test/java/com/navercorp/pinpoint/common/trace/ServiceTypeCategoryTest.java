package com.navercorp.pinpoint.common.trace;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ServiceTypeCategoryTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    void findCategory() {
        ServiceTypeCategory undefined = ServiceTypeCategory.findCategory(-1);
        Assertions.assertEquals(ServiceTypeCategory.UNDEFINED_CATEGORY, undefined);

        ServiceTypeCategory server = ServiceTypeCategory.findCategory(1000);
        Assertions.assertEquals(ServiceTypeCategory.SERVER, server);

        ServiceTypeCategory serverEnd = ServiceTypeCategory.findCategory(1999);
        Assertions.assertEquals(ServiceTypeCategory.SERVER, serverEnd);

        ServiceTypeCategory database = ServiceTypeCategory.findCategory(2000);
        Assertions.assertEquals(ServiceTypeCategory.DATABASE, database);
    }

    @Test
    void findCategory_error() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> ServiceTypeCategory.findCategory(-1000));

        logger.info("{}", Byte.toUnsignedInt((byte)1));
        logger.info("{}", Byte.toUnsignedInt((byte)-1));
        logger.info("{}", Byte.toUnsignedInt(Byte.MAX_VALUE));
        logger.info("{}", Byte.toUnsignedInt(Byte.MIN_VALUE));
    }
}