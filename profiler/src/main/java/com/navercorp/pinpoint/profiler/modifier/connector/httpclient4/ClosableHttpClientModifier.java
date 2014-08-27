package com.nhn.pinpoint.profiler.modifier.connector.httpclient4;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.connector.httpclient4.interceptor.HttpClient4Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Apache httpclient 4.3 CloseableHttpClient modifier
 * 
 * <p/>
 * <p/>
 * <pre>
 * http://grepcode.com/file/repo1.maven.org/maven2/org.apache.httpcomponents/httpclient/4.3/org/apache/http/impl/client/CloseableHttpClient.java?av=h#CloseableHttpClient
 * </pre>
 *
 * @author netspider
 */
public class ClosableHttpClientModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ClosableHttpClientModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "org/apache/http/impl/client/CloseableHttpClient";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", javassistClassName);
        }

        byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
        try {
            InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);

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