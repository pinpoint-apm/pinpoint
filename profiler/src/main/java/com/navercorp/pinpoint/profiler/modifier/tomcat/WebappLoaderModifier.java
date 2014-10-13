package com.nhn.pinpoint.profiler.modifier.tomcat;

import java.security.ProtectionDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;

/**
 * @author hyungil.jeong
 */
public class WebappLoaderModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public WebappLoaderModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    @Override
    public String getTargetClass() {
        return "org/apache/catalina/loader/WebappLoader";
    }

    @Override
    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        logger.info("Modifying. {}", javassistClassName);
        try {
            InstrumentClass webappLoader = byteCodeInstrumentor.getClass(javassistClassName);

            Interceptor webappLoaderStartInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain,
                    "com.nhn.pinpoint.profiler.modifier.tomcat.interceptor.WebappLoaderStartInterceptor", null, null);
            
            boolean isHooked = false;
            // Tomcat 6 - org.apache.catalina.loader.WebappLoader.start()
            if (webappLoader.hasDeclaredMethod("start", null)) {
                webappLoader.addInterceptor("start", null, webappLoaderStartInterceptor);
				isHooked = true;
            }
            // Tomcat 7, 8 - org.apache.catalina.loader.WebappLoader.startInternal()
            else if (webappLoader.hasDeclaredMethod("startInternal", null)) {
                webappLoader.addInterceptor("startInternal", null, webappLoaderStartInterceptor);
				isHooked = true;
            }

            if (isHooked) {
                logger.info("{} class is converted.", javassistClassName);
            } else {
                logger.warn("{} class not converted - start() or startInternal() method not found.", javassistClassName);
            }
            return webappLoader.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("modify failed. Cause:" + e.getMessage(), e);
            }
        }
        return null;
    }
}
