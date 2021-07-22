/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.java9.classloader;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Date;

//import java.util.Date;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PlatformClassLoaderTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Ignore
    @Test
    public void sqlDate() throws ClassNotFoundException {
        Class<?> sqlDateClazz = java.sql.Date.class;
        logger.debug("sqlDate module name:{}", sqlDateClazz.getModule().getName());
        logger.debug("sqlDate classLoader:{}", java.sql.Date.class.getClassLoader());
        Class.forName("java.sql.Date", false, ClassLoader.getPlatformClassLoader());

        try {
            ClassLoader bootStrap = Object.class.getClassLoader();
            Class.forName("java.sql.Date", false, bootStrap);
            Assert.fail();
        } catch (ClassNotFoundException e) {
            // skip
        }
    }

    @Ignore
    @Test
    public void testBigDecimal() {
        Class<BigDecimal> bigDecimalClass = BigDecimal.class;
        Module module = bigDecimalClass.getModule();
        logger.debug("module:{}", module);
        logger.debug("cl:{}", bigDecimalClass.getClassLoader());
    }


    @Ignore
    @Test
    public void sqlDate_module() {
        Class<Date> dateClass = Date.class;
        logger.debug("sqlDate module name:{}", dateClass.getModule().getName());
        logger.debug("sqlDate module cl:{}", dateClass.getModule().getClassLoader());
        logger.debug("sqlDate module layer:{}", dateClass.getModule().getLayer());

        Class<Object> objectClass = Object.class;
        logger.debug("javaBase module name:{}", objectClass.getModule().getName());
        logger.debug("javaBase module cl:{}", objectClass.getModule().getClassLoader());
        logger.debug("javaBase module layer:{}", objectClass.getModule().getLayer());
    }

    @Ignore
    @Test
    public void moduleLayer() {
        ModuleLayer layer = Object.class.getModule().getLayer();

        logger.debug("bootstrap-------------");
        for (Module module : layer.modules()) {
            if (module.getClassLoader() == null) {
                logger.debug("module name:{} ", module.getName());
            }
        }
        logger.debug("bootstrap-------------");

        logger.debug("platform-------------");
        for (Module module : layer.modules()) {
            if (module.getClassLoader() == ClassLoader.getPlatformClassLoader()) {
                logger.debug("module name:{} ", module.getName());
            }
        }
        logger.debug("platform-------------");


        logger.debug("system-------------");
        for (Module module : layer.modules()) {
            if (module.getClassLoader() == ClassLoader.getSystemClassLoader()) {
                logger.debug("module name:{} ", module.getName());
            }
        }
        logger.debug("system-------------");
    }
}

