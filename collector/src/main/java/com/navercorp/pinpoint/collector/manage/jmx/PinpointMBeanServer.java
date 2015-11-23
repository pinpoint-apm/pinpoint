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

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.collector.manage.CollectorManager;


/**
 * @author Taejin Koo
 */
public final class PinpointMBeanServer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private final MBeanServer mBeanServer;
    private final Map<String, CollectorManager> pinpointMBeanHolder;

    PinpointMBeanServer() {
        this.mBeanServer = ManagementFactory.getPlatformMBeanServer();
        this.pinpointMBeanHolder = new HashMap<>();
    }

    public void registerMBean(CollectorManager pinpointMBean) {
        if (pinpointMBean == null) {
            return;
        }
        
        registerMBean(pinpointMBean.getName(), pinpointMBean);
    }
    
    public void registerMBean(String name, CollectorManager pinpointMBean) {
        logger.info("registerMBean {}", name);

        if (isRegistered(pinpointMBean)) {
            return;
        }

        try {
            ObjectName mBeanObjectName = createMBeanObjectName(name);
            mBeanServer.registerMBean(pinpointMBean, mBeanObjectName);
            pinpointMBeanHolder.put(name, pinpointMBean);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void unregisterMBean(CollectorManager pinpointMBean) {
        if (pinpointMBean == null) {
            return;
        }

        unregisterMBean(pinpointMBean.getName());
    }

    public void unregisterMBean(String name) {
        logger.info("unregisterMBean {}", name);

        if (!isRegistered(name)) {
            return;
        }

        try {
            ObjectName mBeanObjectName = createMBeanObjectName(name);
            mBeanServer.unregisterMBean(mBeanObjectName);
            pinpointMBeanHolder.remove(name);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public boolean isRegistered(CollectorManager pinpointMBean) {
        if (pinpointMBean == null) {
            return false;
        }

        return isRegistered(pinpointMBean.getName());
    }

    public boolean isRegistered(String name) {
        ObjectName objectMBeanName;
        try {
            objectMBeanName = createMBeanObjectName(name);
            return mBeanServer.isRegistered(objectMBeanName);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    public CollectorManager getPinpointMBean(String name) {
        return pinpointMBeanHolder.get(name);
    }

    private static ObjectName createMBeanObjectName(String name) throws MalformedObjectNameException {
        String mBeanObjectName = "com.navercorp.pinpoint.collector.mbean:type=" + name;
        ObjectName objectName = new ObjectName(mBeanObjectName);
        return objectName;
    }

}
