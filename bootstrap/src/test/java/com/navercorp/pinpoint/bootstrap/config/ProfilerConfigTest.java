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
	public void testIsProfilableClassWithNoConfiguration() throws IOException {
		ProfilerConfig profilerConfig = new ProfilerConfig();
		profilerConfig.setProfilableClass("com.nhn.pinpoint.testweb.controller.*,com.nhn.pinpoint.testweb.MyClass");

		Assert.assertFalse(profilerConfig.isProfilableClass("com/nhn/pinpoint/testweb/controllers/MyController"));
		Assert.assertFalse(profilerConfig.isProfilableClass("net/spider/king/wang/Jjang"));
		Assert.assertFalse(profilerConfig.isProfilableClass("com/nhn/pinpoint/testweb2/controller/MyController"));
		Assert.assertFalse(profilerConfig.isProfilableClass("com/nhn/pinpoint/testweb2/MyClass"));
	}
	
	/**
	 * <pre>
	 * configuration is 
	 * profile.package.include=com.nhn.pinpoint.testweb.controller.*,com.nhn.pinpoint.testweb.MyClass
	 * </pre>
	 * 
	 * @throws IOException
	 */
	@Test
    public void testIsProfilableClass() throws IOException {

        ProfilerConfig profilerConfig = new ProfilerConfig();
        profilerConfig.setProfilableClass("com.nhn.pinpoint.testweb.controller.*,com.nhn.pinpoint.testweb.MyClass");

        Assert.assertTrue(profilerConfig.isProfilableClass("com/nhn/pinpoint/testweb/MyClass"));
        Assert.assertTrue(profilerConfig.isProfilableClass("com/nhn/pinpoint/testweb/controller/MyController"));
        Assert.assertTrue(profilerConfig.isProfilableClass("com/nhn/pinpoint/testweb/controller/customcontroller/MyCustomController"));

        Assert.assertFalse(profilerConfig.isProfilableClass("com/nhn/pinpoint/testweb/MyUnknownClass"));
        Assert.assertFalse(profilerConfig.isProfilableClass("com/nhn/pinpoint/testweb/controller2/MyController"));
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
