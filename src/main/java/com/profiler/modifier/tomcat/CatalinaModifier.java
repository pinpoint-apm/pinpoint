package com.profiler.modifier.tomcat;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import com.profiler.logging.Logger;
import com.profiler.logging.LoggerFactory;

import com.profiler.Agent;
import com.profiler.DefaultAgent;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.modifier.AbstractModifier;
import com.profiler.modifier.tomcat.interceptors.CatalinaAwaitInterceptor;

/**
 * Tomcat startup정보를 HIPPO서버로 전송하는 코드를 호출하기위한 modifier
 *
 * @author netspider
 */
public class CatalinaModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(CatalinaModifier.class.getName());

    public CatalinaModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "org/apache/catalina/startup/Catalina";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. " + javassistClassName);
        }
        return changeMethod(javassistClassName, classFileBuffer);
    }

    public byte[] changeMethod(String javassistClassName, byte[] classfileBuffer) {
        try {
            /**
             * Tomcat startup완료되면 Catalina.await()을 호출하고 stop되기를 기다린다. 이 때
             * await하기 전에 서버가 시작되면서 수집된 WAS정보를 HIPPO 서버로 전송한다.
             */
            CatalinaAwaitInterceptor catalinaAwaitInterceptor = new CatalinaAwaitInterceptor(agent);
            InstrumentClass aClass = this.byteCodeInstrumentor.getClass(javassistClassName);
            aClass.addInterceptor("await", null, catalinaAwaitInterceptor);

            printClassConvertComplete(javassistClassName);

            return aClass.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage(), e);
            }
        }
        return null;
    }
}