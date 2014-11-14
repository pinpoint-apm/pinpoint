package com.nhn.pinpoint.profiler.modifier.connector.httpclient4;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentException;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.connector.httpclient4.interceptor.HttpClient4Scope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Apache httpclient4 modifier (4.2이하 버전에서만 사용 가능)
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
            InstrumentClass aClass = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);

            addHttpRequestApi(classLoader, protectedDomain, aClass);

            addHttpUriRequestApi(classLoader, protectedDomain, aClass);

            return aClass.toBytecode();
        } catch (Throwable e) {
            logger.warn("httpClient4 modifier error. Caused:{}", e.getMessage(), e);
            return null;
        }
    }

    private void addHttpRequestApi(ClassLoader classLoader, ProtectionDomain protectedDomain, InstrumentClass aClass) throws InstrumentException {
        Interceptor httpRequestApi1= newHttpRequestInterceptor(classLoader, protectedDomain);
        aClass.addScopeInterceptorIfDeclared("execute", new String[]{"org.apache.http.HttpHost", "org.apache.http.HttpRequest"}, httpRequestApi1, HttpClient4Scope.SCOPE);

        Interceptor httpRequestApi2 = newHttpRequestInterceptor(classLoader, protectedDomain);
        aClass.addScopeInterceptorIfDeclared("execute", new String[]{"org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.protocol.HttpContext"}, httpRequestApi2, HttpClient4Scope.SCOPE);

        Interceptor httpRequestApi3 = newHttpRequestInterceptor(classLoader, protectedDomain);
        aClass.addScopeInterceptorIfDeclared("execute", new String[]{"org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler"}, httpRequestApi3, HttpClient4Scope.SCOPE);

        Interceptor httpRequestApi4 = newHttpRequestInterceptor(classLoader, protectedDomain);
        aClass.addScopeInterceptorIfDeclared("execute", new String[]{"org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler", "org.apache.http.protocol.HttpContext"}, httpRequestApi4, HttpClient4Scope.SCOPE);
    }

    private Interceptor newHttpRequestInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain) throws InstrumentException {
        return byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.connector.httpclient4.interceptor.HttpRequestExecuteInterceptor");
    }

    private void addHttpUriRequestApi(ClassLoader classLoader, ProtectionDomain protectedDomain, InstrumentClass aClass) throws InstrumentException {
        Interceptor httpUriRequestInterceptor1 = newHttpUriRequestInterceptor(classLoader, protectedDomain);
        aClass.addScopeInterceptorIfDeclared("execute", new String[]{"org.apache.http.client.methods.HttpUriRequest"}, httpUriRequestInterceptor1, HttpClient4Scope.SCOPE);

        Interceptor httpUriRequestInterceptor2 = newHttpUriRequestInterceptor(classLoader, protectedDomain);
        aClass.addScopeInterceptorIfDeclared("execute", new String[]{"org.apache.http.client.methods.HttpUriRequest", "org.apache.http.protocol.HttpContext"}, httpUriRequestInterceptor2, HttpClient4Scope.SCOPE);

        Interceptor httpUriRequestInterceptor3 = newHttpUriRequestInterceptor(classLoader, protectedDomain);
        aClass.addScopeInterceptorIfDeclared("execute", new String[]{"org.apache.http.client.methods.HttpUriRequest", "org.apache.http.client.ResponseHandler"}, httpUriRequestInterceptor3, HttpClient4Scope.SCOPE);

        Interceptor httpUriRequestInterceptor4 = newHttpUriRequestInterceptor(classLoader, protectedDomain);
        aClass.addScopeInterceptorIfDeclared("execute", new String[]{"org.apache.http.client.methods.HttpUriRequest", "org.apache.http.client.ResponseHandler", "org.apache.http.protocol.HttpContext"}, httpUriRequestInterceptor4, HttpClient4Scope.SCOPE);
    }

    private Interceptor newHttpUriRequestInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain) throws InstrumentException {
        return byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.connector.httpclient4.interceptor.HttpUriRequestExecuteInterceptor");
    }
}