package com.nhn.pinpoint.bootstrap.util;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

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

    @Test
    @Ignore
    // FIXME 테스트 깨짐
    public void testGetMachineName__getHostName() {
        String machineName = NetworkUtils.getMachineName();
        logger.debug("getMachineName:{}", machineName);
        String hostName = NetworkUtils.getHostName();
        logger.debug("getHostName:{}", hostName);
        Assert.assertEquals(machineName, hostName);
    }

    @Test
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


        // TODO default port가 기본값으로 들어갔을 경우 어떤 처리가 맞는건가?
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
}
