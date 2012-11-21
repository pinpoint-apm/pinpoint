package com.profiler.modifier.connector;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.interceptor.bci.InstrumentException;
import com.profiler.modifier.AbstractModifier;

/**
 * Apache httpclient modifier
 * <p/>
 * 
 * <pre>
 * http://grepcode.com/file/repo1.maven.org/maven2/org.apache.httpcomponents/httpclient/4.0.3/org/apache/http/impl/client/AbstractHttpClient.java#AbstractHttpClient.execute%28org.apache.http.HttpHost%2Corg.apache.http.HttpRequest%2Corg.apache.http.client.ResponseHandler%2Corg.apache.http.protocol.HttpContext%29
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
public class HTTPClientModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(HTTPClientModifier.class.getName());

	public HTTPClientModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
		super(byteCodeInstrumentor);
	}

	public String getTargetClass() {
		return "org/apache/http/impl/client/AbstractHttpClient";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Modifing. " + javassistClassName);
		}

		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
		try {
			Interceptor interceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.connector.interceptors.ExecuteMethodInterceptor");
			Interceptor interceptor2 = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.connector.interceptors.Execute2MethodInterceptor");

			InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);

			aClass.addInterceptor("execute", new String[] { "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler", "org.apache.http.protocol.HttpContext" }, interceptor);
			aClass.addInterceptor("execute", new String[] { "org.apache.http.client.methods.HttpUriRequest" }, interceptor2);

			return aClass.toBytecode();
		} catch (InstrumentException e) {
			// TODO log
			return null;
		}
	}
}