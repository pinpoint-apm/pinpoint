package com.nhn.pinpoint.profiler.modifier.tomcat;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.modifier.DedicatedModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tomcat connector 정보를 수집하기 위한 modifier
 *
 * @author netspider
 */
public class TomcatConnectorModifier extends DedicatedModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public TomcatConnectorModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "org/apache/catalina/connector/Connector";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", javassistClassName);
        }
        try {
			// initialize()할 때 protocol과 port번호를 저장해둔다.
			Interceptor interceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain,
                    "com.nhn.pinpoint.profiler.modifier.tomcat.interceptor.ConnectorInitializeInterceptor", new Object[] { agent }, new Class[] { Agent.class });
			InstrumentClass connector = byteCodeInstrumentor.getClass(javassistClassName);

			// Tomcat 6
			if (connector.hasDeclaredMethod("initialize", null)) {
				connector.addInterceptor("initialize", null, interceptor);
			}
			// Tomcat 7
			else if (connector.hasDeclaredMethod("initInternal", null)) {
				connector.addInterceptor("initInternal", null, interceptor);
			}

            if (this.logger.isInfoEnabled()) {
                this.logger.info("{} class is converted.", javassistClassName);
            }

            return connector.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage(), e);
            }
            return null;
        }
    }
}