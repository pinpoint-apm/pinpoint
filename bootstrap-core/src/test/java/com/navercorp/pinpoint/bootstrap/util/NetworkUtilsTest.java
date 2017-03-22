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

package com.navercorp.pinpoint.bootstrap.util;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * @author emeroad
 */
public class NetworkUtilsTest {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Test
    public void hostNameCheck() throws UnknownHostException {
        InetAddress localHost = InetAddress.getLocalHost();
        logger.debug(localHost.toString());
        logger.debug("InetAddress.getLocalHost().getHostAddress()={}", localHost.getHostAddress());
        logger.debug("InetAddress.getLocalHost().getHostName()={}", localHost.getHostName());
        logger.debug("InetAddress.getLocalHost().getCanonicalHostName()={}", localHost.getCanonicalHostName());
    }

    public void testGetMachineName2() {
        String machineName = NetworkUtils.getMachineName();
        Assert.assertNotSame(machineName, NetworkUtils.ERROR_HOST_NAME);
    }

    @Test
    public void testGetHostName() {
        String hostName = NetworkUtils.getHostName();
        Assert.assertNotSame(hostName, NetworkUtils.ERROR_HOST_NAME);
    }

    @Test
    public void testHostFromUrl() {
        String hostFromURL1 = NetworkUtils.getHostFromURL("http://www.naver.com");
        Assert.assertEquals("www.naver.com", hostFromURL1);

        String hostFromURL1_1 = NetworkUtils.getHostFromURL("http://www.naver.com/test");
        Assert.assertEquals("www.naver.com", hostFromURL1_1);


        // TODO how should we resolve host when the url includes the default port?
        String hostFromURL2 = NetworkUtils.getHostFromURL("http://www.naver.com:80");
        Assert.assertEquals("www.naver.com:80", hostFromURL2);

        String hostFromURL2_1 = NetworkUtils.getHostFromURL("http://www.naver.com:80/test");
        Assert.assertEquals("www.naver.com:80", hostFromURL2_1);
    }

    @Test
    public void testHostFromUrl_ErrorTest() {
        String nullUrl = NetworkUtils.getHostFromURL(null);
        Assert.assertSame(nullUrl, null);

        String emptyUrl = NetworkUtils.getHostFromURL("");
        Assert.assertSame(emptyUrl, null);
    }
    
    @Test
    public void hostIpTest() throws Exception {
        List<String> hostIpList = NetworkUtils.getHostIpList();
        for (String hostIp : hostIpList) {
            Assert.assertFalse(NetworkUtils.isLoopbackAddress(hostIp));
        }
        int hostIpListSize = hostIpList.size();

        List<String> hostV4IpList = NetworkUtils.getHostV4IpList();
        for (String hostV4Ip : hostV4IpList) {
            Assert.assertFalse(NetworkUtils.isLoopbackAddress(hostV4Ip));
            Assert.assertTrue(NetworkUtils.validationIpV4FormatAddress(hostV4Ip));
        }
        int hostV4IpListSize = hostV4IpList.size();

        Assert.assertTrue(hostIpListSize >= hostV4IpListSize);
    }

}
