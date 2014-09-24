package com.nhn.pinpoint.common.util;

import junit.framework.Assert;
import org.junit.Test;

public class HttpUtilsTest {
    @Test
    public void contentTypeCharset1() {
        String test = "text/plain; charset=UTF-8";

        String charset = HttpUtils.parseContentTypeCharset(test);
        Assert.assertEquals("UTF-8", charset);
    }

    @Test
    public void contentTypeCharset2() {
        String test = "text/plain; charset=UTF-8;";

        String charset = HttpUtils.parseContentTypeCharset(test);
        Assert.assertEquals("UTF-8", charset);
    }

    @Test
    public void contentTypeCharset3() {
        String test = "text/plain; charset=UTF-8; test=a";

        String charset = HttpUtils.parseContentTypeCharset(test);
        Assert.assertEquals("UTF-8", charset);
    }

    @Test
    public void contentTypeCharset4() {
        String test = "text/plain; charset= UTF-8 ; test=a";

        String charset = HttpUtils.parseContentTypeCharset(test);
        Assert.assertEquals("UTF-8", charset);
    }

}