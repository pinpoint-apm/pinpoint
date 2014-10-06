package com.nhn.pinpoint.profiler.util;

import com.nhn.pinpoint.profiler.DefaultAgent;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import javassist.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author emeroad
 */
public class InstrumentTranslator implements Translator {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final DefaultAgent agent;

    private ConcurrentMap<String, AbstractModifier> modifierMap = new ConcurrentHashMap<String, DedicatedModifier>();

    private ClassLoader loader;

    public InstrumentTranslator(ClassLoader loader, DefaultAgent agent) {
        this.loader = loader;
        this.agent = agent;
    }

    public AbstractModifier addModifier(AbstractModifier modifier) {
        return modifierMap.put(modifier.getTargetClass().replace('/', '.'), modifier);
    }

    @Override
    public void start(ClassPool pool) throws NotFoundException, CannotCompileException {
//        this.pool = pool;
    }

    @Override
    public void onLoad(ClassPool pool, String classname) throws NotFoundException, CannotCompileException {
        logger.debug("loading className:{}", classname);

        try {
            // agent가 등록한 Modifier를 찾아서 트랜스 폼 시도를 한다.
            String replace = classname.replace('.', '/');
            ClassFileTransformer classFileTransformer = agent.getClassFileTransformer();
            byte[] transform = classFileTransformer.transform(this.loader, replace, null, null, null);
            if (transform != null) {
                pool.makeClass(new ByteArrayInputStream(transform));
                return;
            }
        } catch (IOException ex) {
            throw new NotFoundException(classname + " not found. Caused:" + ex.getMessage(), ex);
        } catch (IllegalClassFormatException ex) {
            throw new RuntimeException(classname + " not found. Caused:" + ex.getMessage(), ex);
        }
        // 자체적으로 등록한 ModifierMap 을 찾는다.
        findModifierMap(pool, classname);


    }
    private void findModifierMap(ClassPool pool, String classname) throws NotFoundException, CannotCompileException {
        AbstractModifier modifier = modifierMap.get(classname);
        if (modifier == null) {
            return;
        }
        logger.info("Modify loader:{}, name:{},  modifier{}", loader, classname, modifier);

        final Thread thread = Thread.currentThread();
        final ClassLoader beforeClassLoader = thread.getContextClassLoader();
        thread.setContextClassLoader(loader);
        try {
            byte[] modify = modifier.modify(this.loader, classname, null, null);
            pool.makeClass(new ByteArrayInputStream(modify));
        } catch (IOException ex) {
            throw new NotFoundException(classname + " not found. Caused:" + ex.getMessage(), ex);
        } finally {
            thread.setContextClassLoader(beforeClassLoader);
        }
    }
}
