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

import com.navercorp.pinpoint.collector.manage.AbstractCollectorManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * @author Taejin Koo
 */
public class PinpointMBeanServerTest {

    @Test
    public void registerTest1() {
        PinpointMBeanServer mBeanServer = new PinpointMBeanServer();
        Assertions.assertNull(mBeanServer.getPinpointMBean("PinpointMBeanServerTest$ATest"));

        ATest aTest = new ATest();
        mBeanServer.registerMBean(aTest);
        Assertions.assertEquals(aTest, mBeanServer.getPinpointMBean("PinpointMBeanServerTest$ATest"));

        mBeanServer.unregisterMBean(aTest);
        Assertions.assertNull(mBeanServer.getPinpointMBean("PinpointMBeanServerTest$ATest"));
    }

    @Test
    public void registerTest2() {
        PinpointMBeanServer mBeanServer = new PinpointMBeanServer();
        Assertions.assertNull(mBeanServer.getPinpointMBean("PinpointMBeanServerTest$ATest"));

        ATest aTest = new ATest();
        mBeanServer.registerMBean(aTest);
        Assertions.assertEquals(aTest, mBeanServer.getPinpointMBean("PinpointMBeanServerTest$ATest"));

        mBeanServer.unregisterMBean("PinpointMBeanServerTest$ATest");
        Assertions.assertNull(mBeanServer.getPinpointMBean("PinpointMBeanServerTest$ATest"));
    }

    @Test
    public void test() throws Exception {
        PinpointMBeanServer mBeanServer = new PinpointMBeanServer();

        ATest aTest = new ATest();
        mBeanServer.registerMBean(aTest);

        MBeanServer server = ManagementFactory.getPlatformMBeanServer();

        String mBeanObjectName = "com.navercorp.pinpoint.collector.mbean:type=PinpointMBeanServerTest$ATest";
        ObjectName objectName = new ObjectName(mBeanObjectName);

        String packageName = this.getClass().getPackage().getName();
        ObjectInstance instance = server.getObjectInstance(objectName);

        Assertions.assertEquals(packageName + ".PinpointMBeanServerTest$ATest", instance.getClassName());

        mBeanServer.unregisterMBean(aTest);
    }

    public interface ATestMBean {

    }

    private static class ATest extends AbstractCollectorManager implements ATestMBean {

    }

}
