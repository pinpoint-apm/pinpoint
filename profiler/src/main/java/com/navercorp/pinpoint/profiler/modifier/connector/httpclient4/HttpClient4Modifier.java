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

package com.navercorp.pinpoint.profiler.modifier.connector.httpclient4;

import java.security.ProtectionDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.DefaultInterceptorGroupDefinition;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InterceptorGroupDefinition;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroupTransaction;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;
import com.navercorp.pinpoint.profiler.modifier.connector.httpclient4.interceptor.HttpClient4Scope;

/**
 * Apache httpclient4 modifier (version 4.2 or before)
 * <p/>
 * <p/>
 * <pre>
 * http://grepcode.com/file/repo1.maven.org/maven2/org.apache.httpcomponents/httpclient4/4.0.3/org/apache/http/impl/client/AbstractHttpClient.java#AbstractHttpClient.execute%28org.apache.http.HttpHost%2Corg.apache.http.HttpRequest%2Corg.apache.http.client.ResponseHandler%2Corg.apache.http.protocol.HttpContext%29
 *
 * Hooking
 * org.apache.http.impl.client.AbstractHttpClient.
 * public <T> T execute(
 *            final HttpHost target,
 *            final HttpRequest request,
 *            final ResponseHandler<? extends T> responseHandler,
 *            final HttpContext context)
 *            throws IOException, ClientProtocolException {
 * </pre>
 *
 * @author netspider
 * @author emeroad
 * @author minwoo.jung
 */
public class HttpClient4Modifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public HttpClient4Modifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "org/apache/http/impl/client/AbstractHttpClient";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", javassistClassName);
        }
        
        try {
            //The execute method was moved to CloseableHttpClient class from httpclient 4.0 or later.
            //Need to check if CloseableHttpClient class exist.
            byteCodeInstrumentor.getClass(classLoader, "org.apache.http.impl.client.CloseableHttpClient", classFileBuffer);
            logger.info("{} don't need to be modified. Because target method is existed in org.apache.http.impl.client.CloseableHttpClient", javassistClassName);
            return null;
        } catch (InstrumentException e) {
        }

        try {
            InstrumentClass aClass = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);
            InterceptorGroupDefinition scopeDefinition = new DefaultInterceptorGroupDefinition(HttpClient4Scope.SCOPE);
            InterceptorGroupTransaction scope = byteCodeInstrumentor.getInterceptorGroupTransaction(scopeDefinition);

            addHttpRequestApi(classLoader, protectedDomain, aClass, scope);

            addHttpUriRequestApi(classLoader, protectedDomain, aClass, scope);

            return aClass.toBytecode();
        } catch (Throwable e) {
            logger.warn("httpClient4 modifier error. Caused:{}", e.getMessage(), e);
            return null;
        }
    }

    private void addHttpRequestApi(ClassLoader classLoader, ProtectionDomain protectedDomain, InstrumentClass aClass, InterceptorGroupTransaction scope) throws InstrumentException {
        Interceptor httpRequestApi1 = newHttpRequestInterceptor(classLoader, protectedDomain ,false, scope);
        aClass.addInterceptor("execute", new String[]{"org.apache.http.HttpHost", "org.apache.http.HttpRequest"}, httpRequestApi1);

        Interceptor httpRequestApi2 = newHttpRequestInterceptor(classLoader, protectedDomain, false, scope);
        aClass.addInterceptor("execute", new String[]{"org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.protocol.HttpContext"}, httpRequestApi2);

        Interceptor httpRequestApi3 = newHttpRequestInterceptor(classLoader, protectedDomain, true, scope);
        aClass.addInterceptor("execute", new String[]{"org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler"}, httpRequestApi3);

        Interceptor httpRequestApi4 = newHttpRequestInterceptor(classLoader, protectedDomain, true, scope);
        aClass.addInterceptor("execute", new String[]{"org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler", "org.apache.http.protocol.HttpContext"}, httpRequestApi4);
    }

    private Interceptor newHttpRequestInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, boolean isHasCallbackParam, InterceptorGroupTransaction scope) throws InstrumentException {
        return byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.navercorp.pinpoint.profiler.modifier.connector.httpclient4.interceptor.HttpRequestExecuteInterceptor", new Object[] {isHasCallbackParam, scope}, new Class[] {boolean.class, InterceptorGroupTransaction.class});
    }

    private void addHttpUriRequestApi(ClassLoader classLoader, ProtectionDomain protectedDomain, InstrumentClass aClass, InterceptorGroupTransaction scope) throws InstrumentException {
        Interceptor httpUriRequestInterceptor1 = newHttpUriRequestInterceptor(classLoader, protectedDomain, false, scope);
        aClass.addInterceptor("execute", new String[]{"org.apache.http.client.methods.HttpUriRequest"}, httpUriRequestInterceptor1);

        Interceptor httpUriRequestInterceptor2 = newHttpUriRequestInterceptor(classLoader, protectedDomain, false, scope);
        aClass.addInterceptor("execute", new String[]{"org.apache.http.client.methods.HttpUriRequest", "org.apache.http.protocol.HttpContext"}, httpUriRequestInterceptor2);

        Interceptor httpUriRequestInterceptor3 = newHttpUriRequestInterceptor(classLoader, protectedDomain, true, scope);
        aClass.addInterceptor("execute", new String[]{"org.apache.http.client.methods.HttpUriRequest", "org.apache.http.client.ResponseHandler"}, httpUriRequestInterceptor3);

        Interceptor httpUriRequestInterceptor4 = newHttpUriRequestInterceptor(classLoader, protectedDomain, true, scope);
        aClass.addInterceptor("execute", new String[]{"org.apache.http.client.methods.HttpUriRequest", "org.apache.http.client.ResponseHandler", "org.apache.http.protocol.HttpContext"}, httpUriRequestInterceptor4);
    }

    private Interceptor newHttpUriRequestInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, boolean isHasCallbackParam, InterceptorGroupTransaction scope) throws InstrumentException {
        return byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.navercorp.pinpoint.profiler.modifier.connector.httpclient4.interceptor.HttpUriRequestExecuteInterceptor", new Object[] {isHasCallbackParam, scope}, new Class[] {boolean.class, InterceptorGroupTransaction.class});
    }
}
