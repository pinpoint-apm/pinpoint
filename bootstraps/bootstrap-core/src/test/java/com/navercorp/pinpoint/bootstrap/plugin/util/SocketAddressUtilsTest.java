/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.util;

import com.navercorp.pinpoint.common.util.JvmUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.AssumptionViolatedException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collections;

/**
 * @author HyunGil Jeong
 */
public class SocketAddressUtilsTest {

    private static final String VALID_HOST = "naver.com";
    private static final String INVALID_HOST = "pinpoint-none-existent-domain-hopefully";

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketAddressUtilsTest.class);

    private static NameServiceReplacer NAME_SERVICE_REPLACER;

    @BeforeClass
    public static void setUpBeforeClass() {
        NAME_SERVICE_REPLACER = createNameServiceReplacer();
    }

    @Test
    public void fromValidHostName() {
        String hostName = VALID_HOST;
        InetSocketAddress socketAddress = new InetSocketAddress(hostName, 80);
        assertResolved(socketAddress);

        final String expectedHostName = hostName;
        final String expectedAddress = socketAddress.getAddress().getHostAddress();

        verify(socketAddress, expectedHostName, expectedAddress);
    }

    @Test
    public void fromValidHostNameUnresolved() {
        String hostName = VALID_HOST;
        InetSocketAddress socketAddress = InetSocketAddress.createUnresolved(hostName, 80);
        assertUnresolved(socketAddress);

        final String expectedHostName = hostName;
        final String expectedAddress = hostName;

        verify(socketAddress, expectedHostName, expectedAddress);
    }

    @Test
    public void fromInvalidHostName() {
        String hostName = INVALID_HOST;
        InetSocketAddress socketAddress = new InetSocketAddress(hostName, 80);
        assertUnresolved(socketAddress);

        final String expectedHostName = hostName;
        final String expectedAddress = hostName;

        verify(socketAddress, expectedHostName, expectedAddress);
    }

    @Test
    public void fromAddress() {
        String hostName = VALID_HOST;
        InetAddress inetAddress = createAddressFromHostName(hostName);
        Assume.assumeNotNull(inetAddress);

        String address = inetAddress.getHostAddress();
        InetSocketAddress socketAddress = new InetSocketAddress(address, 80);
        assertResolved(socketAddress);

        final String expectedHostName = address;
        final String expectedAddress = address;

        verify(socketAddress, expectedHostName, expectedAddress);
    }

    @Test
    public void fromLookedUpAddress() {
        String hostName = VALID_HOST;
        InetAddress inetAddress = createAddressFromHostName(hostName);
        Assume.assumeNotNull(inetAddress);

        String address = inetAddress.getHostAddress();
        InetSocketAddress socketAddress = new InetSocketAddress(address, 80);
        assertResolved(socketAddress);

        String expectedHostName = socketAddress.getHostName(); // lookup host name
        String expectedAddress = address;

        verify(socketAddress, expectedHostName, expectedAddress);
    }

    @Test
    public void fromLocalAddress() {
        InetAddress inetAddress = createLocalAddress();
        Assume.assumeNotNull(inetAddress);

        String address = inetAddress.getHostAddress();
        InetSocketAddress socketAddress = new InetSocketAddress(address, 80);
        assertResolved(socketAddress);

        final String expectedHostName = address;
        final String expectedAddress = address;

        verify(socketAddress, expectedHostName, expectedAddress);
    }

    // Helpers

    private static void assertResolved(InetSocketAddress socketAddress) {
        Assert.assertFalse(socketAddress.isUnresolved());
    }

    private static void assertUnresolved(InetSocketAddress socketAddress) {
        Assert.assertTrue(socketAddress.isUnresolved());
    }

    private static void verify(InetSocketAddress socketAddress, String expectedHostName, String expectedAddress) {
        NAME_SERVICE_REPLACER.replace();
        try {
            String actualHostName = SocketAddressUtils.getHostNameFirst(socketAddress);
            String actualAddress = SocketAddressUtils.getAddressFirst(socketAddress);
            LOGGER.info("expectedHostName : {}, actualHostName : {}", expectedHostName, actualHostName);
            LOGGER.info("expectedAddress : {}, actualAddress : {}", expectedAddress, actualAddress);
            Assert.assertEquals(expectedHostName, actualHostName);
            Assert.assertEquals(expectedAddress, actualAddress);
        } finally {
            NAME_SERVICE_REPLACER.rollback();
        }
    }

    private static InetAddress createLocalAddress() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            LOGGER.warn("Error creating local InetAddress", e);
            return null;
        }
    }

    private static InetAddress createAddressFromHostName(String hostName) {
        try {
            return InetAddress.getByName(hostName);
        } catch (UnknownHostException e) {
            LOGGER.warn("Error creating InetAddress from host : {}", hostName, e);
            return null;
        }
    }

    private interface NameServiceReplacer {
        void replace();
        void rollback();
    }

    private static NameServiceReplacer createNameServiceReplacer() {

        Class<?> nameServiceClass = null;
        try {
            // pre jdk 9
            nameServiceClass = Class.forName("sun.net.spi.nameservice.NameService");
        } catch (ClassNotFoundException e) {
            try {
                // post jdk 9
                nameServiceClass = Class.forName("java.net.InetAddress$NameService");
            } catch (ClassNotFoundException e1) {
                // ignore and skip test below
            }
        }

        if (nameServiceClass == null) {
            LOGGER.error("[{}] {} - NameService class not found, skipping test.",
                    JvmUtils.getType(), JvmUtils.getVersion());
            throw new AssumptionViolatedException("NameService class required for test not found.");
        }

        ClassLoader cl = InetAddress.class.getClassLoader();
        final Object proxy = Proxy.newProxyInstance(cl, new Class<?>[]{nameServiceClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String methodName = method.getName();
                if (methodName.equals("lookupAllHostAddr")) {
                    throw new UnsupportedOperationException("DNS lookup should not be made");
                } else if (methodName.equals("getHostByAddr")) {
                    throw new UnsupportedOperationException("Reverse DNS lookup should not be made");
                } else if (methodName.equals("toString")) {
                    return SocketAddressUtilsTest.class.getSimpleName() + " Name Service";
                } else {
                    LOGGER.error("[{}] {} - Unexpected method invocation : {}",
                            JvmUtils.getType(), JvmUtils.getVersion(), methodName);
                    throw new IllegalStateException("Unknown method : " + methodName + " invoked");
                }
            }
        });

        // jdk 7, 8
        final String nameServicesFieldName = "nameServices";
        final Object nameServicesFieldValue = getNameServiceFieldValue(nameServicesFieldName);
        if (nameServicesFieldValue != null) {
            return new NameServiceReplacer() {
                @Override
                public void replace() {
                    setNameServiceFieldValue(nameServicesFieldName, Collections.singletonList(proxy));
                }

                @Override
                public void rollback() {
                    setNameServiceFieldValue(nameServicesFieldName, nameServicesFieldValue);
                }
            };
        }

        // jdk 6, jdk 9+
        final String nameServiceFieldName = "nameService";
        final Object nameServiceFieldValue = getNameServiceFieldValue(nameServiceFieldName);
        if (nameServiceFieldValue != null) {
            return new NameServiceReplacer() {
                @Override
                public void replace() {
                    setNameServiceFieldValue(nameServiceFieldName, proxy);
                }

                @Override
                public void rollback() {
                    setNameServiceFieldValue(nameServiceFieldName, nameServiceFieldValue);
                }
            };
        }

        LOGGER.error("[{}] {} - Field for name service not found.", JvmUtils.getType(), JvmUtils.getVersion());
        throw new AssumptionViolatedException("Cannot find field for name service.");
    }

    private static Object getNameServiceFieldValue(String fieldName) {
        try {
            Field nameServiceField = InetAddress.class.getDeclaredField(fieldName);
            nameServiceField.setAccessible(true);
            return nameServiceField.get(null);
        } catch (NoSuchFieldException e) {
            // This can happen depending on jvm
            return null;
        } catch (Exception e) {
            LOGGER.error("[{}] {} - Unexpected error while getting field [{}], skipping test",
                    JvmUtils.getType(), JvmUtils.getVersion(), fieldName, e);
            throw new AssumptionViolatedException("Unexpected reflection exception", e);
        }
    }

    private static void setNameServiceFieldValue(String fieldName, Object nameService) {
        try {
            Field nameServiceField = InetAddress.class.getDeclaredField(fieldName);
            nameServiceField.setAccessible(true);
            nameServiceField.set(null, nameService);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Expected field : [" + fieldName + "] not found");
        } catch (Exception e) {
            LOGGER.error("[{}] {} - Unexpected error while setting field [{}], skipping test",
                    JvmUtils.getType(), JvmUtils.getVersion(), fieldName, e);
            throw new AssumptionViolatedException("Unexpected reflection exception", e);
        }
    }
}
