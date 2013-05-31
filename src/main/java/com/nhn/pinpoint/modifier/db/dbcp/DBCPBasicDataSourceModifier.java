package com.nhn.pinpoint.modifier.db.dbcp;

import com.nhn.pinpoint.Agent;
import com.nhn.pinpoint.config.ProfilerConstant;
import com.nhn.pinpoint.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.logging.LoggerFactory;
import com.nhn.pinpoint.modifier.AbstractModifier;
import com.nhn.pinpoint.trace.DatabaseRequestTracer;
import javassist.CtClass;
import javassist.CtMethod;

import java.security.ProtectionDomain;
import com.nhn.pinpoint.logging.Logger;

public class DBCPBasicDataSourceModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(DBCPBasicDataSourceModifier.class.getName());

    public DBCPBasicDataSourceModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "org/apache/commons/dbcp/BasicDataSource";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. " + javassistClassName);
        }
        this.byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
        return changeMethod(javassistClassName, classFileBuffer);
    }

    private byte[] changeMethod(String javassistClassName, byte[] classfileBuffer) {
        try {
            CtClass cc = null;

            updateGetConnectionMethod(cc);

            printClassConvertComplete(javassistClassName);
            byte[] bytes = cc.toBytecode();
            cc.detach();
            return bytes;
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage(), e);
            }
        }
        return null;
    }

    private void updateGetConnectionMethod(CtClass cc) throws Exception {
        CtMethod method = cc.getDeclaredMethod("getConnection", null);
        method.insertAfter("{" + DatabaseRequestTracer.FQCN + ".putConnection(" + ProfilerConstant.REQ_DATA_TYPE_DB_GET_CONNECTION + ",$0.getUrl()); }");
    }
}
