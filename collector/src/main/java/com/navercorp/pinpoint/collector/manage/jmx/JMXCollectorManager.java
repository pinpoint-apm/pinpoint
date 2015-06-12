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

package com.navercorp.pinpoint.collector.manage.jmx;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.navercorp.pinpoint.collector.manage.CollectorManager;

/**
 * @author Taejin Koo
 */
public class JMXCollectorManager {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PinpointMBeanServer pinpointMBeanServer;

    @Autowired
    private JMXCollectorManagerList jmxCollectorManagerList;
    
    public JMXCollectorManager() {
        this.pinpointMBeanServer = new PinpointMBeanServer();
    }

    @PostConstruct
    public void setUp() {
        logger.info("PinpointCollectorManager initialization started.");
        
        for (CollectorManager collectorManager : jmxCollectorManagerList.getSupportList()) {
            try {
                pinpointMBeanServer.registerMBean(collectorManager);
            } catch (Exception e) {
                logger.warn("Failed to register {} MBean.", collectorManager, e);
            }
        }

        logger.info("PinpointCollectorManager initialization completed.");
    }

    @PreDestroy
    public void tearDown() {
        logger.info("PinpointCollectorManager finalization started.");

        for (CollectorManager collectorManager : jmxCollectorManagerList.getSupportList()) {
            try {
                pinpointMBeanServer.unregisterMBean(collectorManager);
            } catch (Exception e) {
                logger.warn("Failed to unregister {} MBean.", collectorManager, e);
            }
        }

        logger.info("PinpointCollectorManager finalization completed.");
    }
    
    public CollectorManager getMBean(String name) {
        return pinpointMBeanServer.getPinpointMBean(name);
    }
    

}
