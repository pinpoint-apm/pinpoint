package com.profiler.modifier.db.mysql;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.JavaAssistClass;
import javassist.*;

import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.modifier.AbstractModifier;
import javassist.bytecode.AccessFlag;

public class MySQLStatementModifier extends AbstractModifier {

    private final Logger logger = Logger.getLogger(MySQLStatementModifier.class.getName());

    public MySQLStatementModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
        super(byteCodeInstrumentor);
    }

    public String getTargetClass() {
        return "com/mysql/jdbc/StatementImpl";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Modifing. " + javassistClassName);
        }

        byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
//        classPool.insertClassPath(new ByteArrayClassPath(javassistClassName, classFileBuffer));

        InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);
        Interceptor interceptor = newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.db.mysql.interceptors.ExecuteQueryMethodInterceptor");
        boolean executeQuery = aClass.addInterceptor("executeQuery", new String[]{"java.lang.String"}, interceptor);
        if (logger.isLoggable(Level.INFO)) {
            logger.info("executeQuery =" + executeQuery);
        }

        Interceptor interceptor1 = newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.db.mysql.interceptors.ExecuteUpdateMethodInterceptor");
        boolean executeUpdate = aClass.addInterceptor("executeUpdate", new String[]{"java.lang.String", "boolean", "boolean"}, interceptor1);
        if (logger.isLoggable(Level.INFO)) {
            logger.info("executeUpdate =" + executeUpdate);
        }
        addTraceData((JavaAssistClass) aClass);


        if (executeQuery && executeQuery) {
            return aClass.toBytecode();
        }
        return null;
    }

    private void addTraceData(JavaAssistClass aClass) {
        try {
            ClassPool classPool1 = byteCodeInstrumentor.getClassPool();
            JavaAssistClass jc = (JavaAssistClass) aClass;
            CtClass ctClass = jc.getCtClass();
            CtClass string = classPool1.get("java.lang.String");
            CtField traceUrl = new CtField(string, "__url", ctClass);
            traceUrl.setModifiers(AccessFlag.PUBLIC);
            ctClass.addField(traceUrl);
            CtMethod setUrl = CtNewMethod.setter("__setUrl", traceUrl);
            ctClass.addMethod(setUrl);
            CtMethod getUrl = CtNewMethod.getter("__getUrl", traceUrl);
            ctClass.addMethod(getUrl);
        } catch (NotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (CannotCompileException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}