package com.nhn.pinpoint.profiler.util;

import com.nhn.pinpoint.profiler.ClassFileTransformerDispatcher;
import com.nhn.pinpoint.profiler.DefaultAgent;
import com.nhn.pinpoint.profiler.modifier.Modifier;
import javassist.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author emeroad
 */
public class InstrumentTranslator implements Translator {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final DefaultAgent agent;

//    private ConcurrentMap<String, Modifier> modifierMap = new ConcurrentHashMap<String, Modifier>();

    private ClassLoader loader;

    public InstrumentTranslator(ClassLoader loader, DefaultAgent agent) {
        this.loader = loader;
        this.agent = agent;
    }

    public Modifier addModifier(Modifier modifier) {
        return null;
//        return modifierMap.put(modifier.getTargetClass().replace('/', '.'), modifier);
    }

    @Override
    public void start(ClassPool pool) throws NotFoundException, CannotCompileException {
//        this.pool = pool;
    }

    @Override
    public void onLoad(ClassPool pool, String classname) throws NotFoundException, CannotCompileException {
        logger.debug("loading className:{}", classname);
        ClassFileTransformerDispatcher classFileTransformer = agent.getClassFileTransformer();

        classname = classname.replace('.', '/');
        try {
            byte[] transform = classFileTransformer.transform(this.loader, classname, null, null, null);
            if (transform == null) {
                return;
            }
            pool.makeClass(new ByteArrayInputStream(transform));
        } catch (IOException ex) {
            throw new NotFoundException(classname + " not found. Caused:" + ex.getMessage(), ex);
        } catch (IllegalClassFormatException ex) {
            throw new RuntimeException(classname + " not found. Caused:" + ex.getMessage(), ex);

        }

    }
}
