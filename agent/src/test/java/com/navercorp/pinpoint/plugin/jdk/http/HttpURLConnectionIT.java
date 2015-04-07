/**
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.jdk.http;

import static com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier.ExpectedAnnotation.*;

import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier.BlockType;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;

/**
 * @author Jongho Moon
 *
 */
@RunWith(PinpointPluginTestSuite.class)
@JvmVersion({6, 7, 8})
public class HttpURLConnectionIT {

    @Test
    public void test() throws Exception {
        URL url = new URL("http://www.naver.com");
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.getHeaderFields();
        
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCachedApis(System.out);
        verifier.printBlocks(System.out);
        
        Class<?> targetClass = Class.forName("sun.net.www.protocol.http.HttpURLConnection");
        Method getInputStream = targetClass.getMethod("getInputStream");
        
        verifier.verifyTraceBlockCount(1);
        verifier.verifyTraceBlock(BlockType.EVENT, "JDK_HTTPURLCONNECTOR", getInputStream, null, null, null, "www.naver.com", annotation("http.url", "http://www.naver.com"));
    }
    
    @Test
    public void testConnectTwice() throws Exception {
        URL url = new URL("http://www.naver.com");
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        
        connection.connect();
        connection.getInputStream();
        
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printBlocks(System.out);
        
        Class<?> targetClass = Class.forName("sun.net.www.protocol.http.HttpURLConnection");
        Method connect = targetClass.getMethod("connect");
        
        verifier.verifyTraceBlockCount(1);
        verifier.verifyTraceBlock(BlockType.EVENT, "JDK_HTTPURLCONNECTOR", connect, null, null, null, "www.naver.com", annotation("http.url", "http://www.naver.com"));
    }
    
}
