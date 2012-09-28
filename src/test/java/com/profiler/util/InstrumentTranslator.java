package com.profiler.util;

import com.profiler.modifier.Modifier;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.NotFoundException;
import javassist.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InstrumentTranslator implements Translator {
    private final Logger logger = LoggerFactory.getLogger(InstrumentTranslator.class.getName());

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
        modifier.modify(this.loader, classname, null, null);
    }
}
