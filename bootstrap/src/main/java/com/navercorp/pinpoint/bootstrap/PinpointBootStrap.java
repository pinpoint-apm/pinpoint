/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.navercorp.pinpoint.ProductInfo;

/**
 * @author emeroad
 * @author netspider
 */
public class PinpointBootStrap {

    private static final Logger logger = Logger.getLogger(PinpointBootStrap.class.getName());

    public static final String BOOT_CLASS = "com.navercorp.pinpoint.profiler.DefaultAgent";

    private static final boolean STATE_NONE = false;
    private static final boolean STATE_STARTED = true;
    private static final AtomicBoolean LOAD_STATE = new AtomicBoolean(STATE_NONE);

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        if (agentArgs != null) {
            logger.info(ProductInfo.NAME + " agentArgs:" + agentArgs);
        }

        final boolean duplicated = checkDuplicateLoadState();
        if (duplicated) {
            logPinpointAgentLoadFail();
            return;
        }
        
        loadBootstrapCoreLib(instrumentation);

        PinpointStarter bootStrap = new PinpointStarter(agentArgs, instrumentation);
        bootStrap.start();

    }

    private static void loadBootstrapCoreLib(Instrumentation instrumentation) {
        // 1st find boot-strap.jar
        final ClassPathResolver classPathResolver = new ClassPathResolver();
        boolean agentJarNotFound = classPathResolver.findAgentJar();
        if (!agentJarNotFound) {
            logger.severe("pinpoint-bootstrap-x.x.x(-SNAPSHOT).jar Fnot found.");
            logPinpointAgentLoadFail();
            return;
        }

        // 2nd find boot-strap-core.jar
        final String bootStrapCoreJar = classPathResolver.getBootStrapCoreJar();
        if (bootStrapCoreJar == null) {
            logger.severe("pinpoint-bootstrap-core-x.x.x(-SNAPSHOT).jar not found");
            logPinpointAgentLoadFail();
            return;
        }

        JarFile bootStrapCoreJarFile = getBootStrapJarFile(bootStrapCoreJar);
        if (bootStrapCoreJarFile == null) {
            logger.severe("pinpoint-bootstrap-core-x.x.x(-SNAPSHOT).jar not found");
            logPinpointAgentLoadFail();
            return;
        }
        logger.info("load pinpoint-bootstrap-core-x.x.x(-SNAPSHOT).jar :" + bootStrapCoreJar);
        instrumentation.appendToBootstrapClassLoaderSearch(bootStrapCoreJarFile);
    }

    // for test
    static boolean getLoadState() {
        return LOAD_STATE.get();
    }

    private static boolean checkDuplicateLoadState() {
        final boolean startSuccess = LOAD_STATE.compareAndSet(STATE_NONE, STATE_STARTED);
        if (startSuccess) {
            return false;
        } else {
            if (logger.isLoggable(Level.SEVERE)) {
                logger.severe("pinpoint-bootstrap already started. skipping agent loading.");
            }
            return true;
        }
    }

    private static void logPinpointAgentLoadFail() {
        final String errorLog =
                "*****************************************************************************\n" +
                        "* Pinpoint Agent load failure\n" +
                        "*****************************************************************************";
        System.err.println(errorLog);
    }


    private static JarFile getBootStrapJarFile(String bootStrapCoreJar) {
        try {
            return new JarFile(bootStrapCoreJar);
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, bootStrapCoreJar + " file not found.", ioe);
            return null;
        }
    }

}
