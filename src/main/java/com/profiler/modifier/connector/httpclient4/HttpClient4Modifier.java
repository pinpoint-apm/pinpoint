package com.profiler.modifier.connector.httpclient4;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import com.profiler.logging.Logger;

import com.profiler.Agent;
import com.profiler.DefaultAgent;
import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.logging.LoggerFactory;
import com.profiler.modifier.AbstractModifier;

/**
 * Apache httpclient4 modifier
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
 */
public class HttpClient4Modifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(HttpClient4Modifier.class.getName());

    public HttpClient4Modifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "org/apache/http/impl/client/AbstractHttpClient";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. " + javassistClassName);
        }

        byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
        try {
            InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);

            Interceptor interceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.connector.httpclient4.interceptor.ExecuteMethodInterceptor");
            aClass.addInterceptor("execute", new String[]{"org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler", "org.apache.http.protocol.HttpContext"}, interceptor);

            Interceptor interceptor2 = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.connector.httpclient4.interceptor.Execute2MethodInterceptor");
            aClass.addInterceptor("execute", new String[]{"org.apache.http.client.methods.HttpUriRequest"}, interceptor2);

            return aClass.toBytecode();
        } catch (Throwable e) {
            logger.warn("httpclient4 modifier error. Caused:" + e.getMessage(), e);
            return null;
        }
    }
}