package com.nhn.pinpoint.profiler.util;

import com.nhn.pinpoint.profiler.modifier.Modifier;
import javassist.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author emeroad
 */
public class InstrumentTranslator implements Translator {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ConcurrentMap<String, Modifier> modifierMap = new ConcurrentHashMap<String, Modifier>();

    private ClassLoader loader;

    public InstrumentTranslator(ClassLoader loader) {
        this.loader = loader;
    }

    public Modifier addModifier(Modifier modifier) {
        return modifierMap.put(modifier.getTargetClass().replace('/', '.'), modifier);
    }

    @Override
    public void start(ClassPool pool) throws NotFoundException, CannotCompileException {
//        this.pool = pool;
    }

    @Override
    public void onLoad(ClassPool pool, String classname) throws NotFoundException, CannotCompileException {
        logger.debug("loading className:{}", classname);

        Modifier modifier = modifierMap.get(classname);
        if(modifier == null) {
            return;
        }
        logger.info("Modify loader:{}, name:{},  modifier{}", loader, classname, modifier);

        final Thread thread = Thread.currentThread();
        ClassLoader beforeClassLoader = thread.getContextClassLoader();
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
