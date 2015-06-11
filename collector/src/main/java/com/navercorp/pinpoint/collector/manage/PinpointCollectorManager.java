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

package com.navercorp.pinpoint.collector.manage;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.navercorp.pinpoint.common.hbase.AccessControlOperations;

/**
 * @author Taejin Koo
 */
public class PinpointCollectorManager {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PinpointMBeanServer pinpointMBeanServer;
    private final List<PinpointCollectorMBean> pinpointMBeanList = new ArrayList<PinpointCollectorMBean>();
    
    @Autowired
    private AccessControlOperations hbaseTemplate;

    public PinpointCollectorManager() {
        this.pinpointMBeanServer = new PinpointMBeanServer();
    }

    @PostConstruct
    public void setUp() {
        logger.info("PinpointCollectorManager initialization started.");
        
        if (hbaseTemplate != null) {
            pinpointMBeanList.add(new DBAccessControl(hbaseTemplate));
        }

        for (PinpointCollectorMBean pinpontMBean : pinpointMBeanList) {
            try {
                pinpointMBeanServer.registerMBean(pinpontMBean);
            } catch (Exception e) {
                logger.warn("Failed to register {} MBean.", pinpontMBean);
            }
        }

        logger.info("PinpointCollectorManager initialization completed.");
    }

    @PreDestroy
    public void tearDown() {
        logger.info("PinpointCollectorManager finalization started.");

        for (PinpointCollectorMBean pinpontMBean : pinpointMBeanList) {
            try {
                pinpointMBeanServer.unregisterMBean(pinpontMBean);
            } catch (Exception e) {
                logger.warn("Failed to unregister {} MBean.", pinpontMBean);
            }
        }

        logger.info("PinpointCollectorManager finalization completed.");
    }
    
    public PinpointCollectorMBean getMBean(String name) {
        return pinpointMBeanServer.getPinpointMBean(name);
    }

}
