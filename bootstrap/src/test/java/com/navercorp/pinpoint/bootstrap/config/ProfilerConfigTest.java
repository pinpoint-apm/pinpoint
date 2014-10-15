package com.nhn.pinpoint.bootstrap.config;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class ProfilerConfigTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


	@Test
	public void defaultProfilableClassFilter() throws IOException {
		ProfilerConfig profilerConfig = new ProfilerConfig();
		Filter<String> profilableClassFilter = profilerConfig.getProfilableClassFilter();
		Assert.assertFalse(profilableClassFilter.filter("net/spider/king/wang/Jjang"));
	}

    @Test
    public void readProperty() throws IOException {
        String path = ProfilerConfig.class.getResource("/com/nhn/pinpoint/bootstrap/config/test.property").getPath();
        logger.debug("path:{}", path);

        ProfilerConfig profilerConfig = new ProfilerConfig();
        profilerConfig.readConfigFile(path);

    }


    @Test
    public void ioBuffering_test() throws IOException {
        String path = ProfilerConfig.class.getResource("/com/nhn/pinpoint/bootstrap/config/test.property").getPath();
        logger.debug("path:{}", path);

        ProfilerConfig profilerConfig = new ProfilerConfig();
        profilerConfig.readConfigFile(path);

        Assert.assertEquals(profilerConfig.isIoBufferingEnable(), false);
        Assert.assertEquals(profilerConfig.getIoBufferingBufferSize(), 30);
    }

    @Test
    public void ioBuffering_default() throws IOException {
        String path = ProfilerConfig.class.getResource("/com/nhn/pinpoint/bootstrap/config/default.property").getPath();
        logger.debug("path:{}", path);

        ProfilerConfig profilerConfig = new ProfilerConfig();
        profilerConfig.readConfigFile(path);

        Assert.assertEquals(profilerConfig.isIoBufferingEnable(), true);
        Assert.assertEquals(profilerConfig.getIoBufferingBufferSize(), 10);
    }
    
    @Test
    public void tcpCommandAcceptrConfigTest1() throws IOException {
        String path = ProfilerConfig.class.getResource("/com/nhn/pinpoint/bootstrap/config/test.property").getPath();
        logger.debug("path:{}", path);

        ProfilerConfig profilerConfig = new ProfilerConfig();
        profilerConfig.readConfigFile(path);
        
        Assert.assertFalse(profilerConfig.isTcpDataSenderCommandAcceptEnable());
    }
    
    @Test
    public void tcpCommandAcceptrConfigTest2() throws IOException {
        String path = ProfilerConfig.class.getResource("/com/nhn/pinpoint/bootstrap/config/test2.property").getPath();
        logger.debug("path:{}", path);

        ProfilerConfig profilerConfig = new ProfilerConfig();
        profilerConfig.readConfigFile(path);
        
        Assert.assertTrue(profilerConfig.isTcpDataSenderCommandAcceptEnable());
    }
    

}
