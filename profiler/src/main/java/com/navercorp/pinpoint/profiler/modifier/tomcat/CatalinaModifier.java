package com.nhn.pinpoint.profiler.modifier.tomcat;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.tomcat.interceptor.CatalinaAwaitInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tomcat startup정보를 Pinpoint서버로 전송하는 코드를 호출하기위한 modifier
 *
 * @author netspider
 */
public class CatalinaModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public CatalinaModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "org/apache/catalina/startup/Catalina";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", javassistClassName);
        }
        return changeMethod(javassistClassName, classFileBuffer);
    }

    public byte[] changeMethod(String javassistClassName, byte[] classfileBuffer) {
        try {
            /**
             * Tomcat startup완료되면 Catalina.await()을 호출하고 stop되기를 기다린다. 이 때
             * await하기 전에 서버가 시작되면서 수집된 WAS정보를 Pinpoint 서버로 전송한다.
             */
            InstrumentClass aClass = this.byteCodeInstrumentor.getClass(javassistClassName);
            
            CatalinaAwaitInterceptor catalinaAwaitInterceptor = new CatalinaAwaitInterceptor(agent);
            aClass.addInterceptor("await", null, catalinaAwaitInterceptor);

//            // Tomcat 7
//			if (aClass.hasDeclaredMethod("start", null) && aClass.hasDeclaredMethod("stop", null)) {
//				LifeCycleEventListener lifeCycleEventListener = new LifeCycleEventListener(agent);
//				aClass.addInterceptor("start", null, new CatalinaStartInterceptor(lifeCycleEventListener));
//				aClass.addInterceptor("stop", null, new CatalinaStopInterceptor(lifeCycleEventListener));
//			}

            if (this.logger.isInfoEnabled()) {
                this.logger.info("{} class is converted.", javassistClassName);
            }

            return aClass.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage(), e);
            }
        }
        return null;
    }
}