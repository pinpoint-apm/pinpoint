package com.nhn.pinpoint.bootstrap;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author emeroad
 */
public class PinpointBootStrapTest {
    @Test
    public void testDuplicatedLoadCheck() throws Exception {
        PinpointBootStrap.premain("test", new DummyInstrumentation());
        String exist = System.getProperty(PinpointBootStrap.BOOT_STRAP_LOAD_STATE);
        Assert.assertTrue(exist != null);

        PinpointBootStrap.premain("test", new DummyInstrumentation());
        // 중복 된경우를 체크 할수 있는 방법이 로그 확인 뿐이 없나??

        String recheck = System.getProperty(PinpointBootStrap.BOOT_STRAP_LOAD_STATE);
        Assert.assertEquals(exist, recheck);
    }
}
