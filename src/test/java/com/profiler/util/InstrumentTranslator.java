package com.profiler.util;

import com.profiler.modifier.Modifier;
import javassist.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InstrumentTranslator implements Translator {
    private final Logger logger = Logger.getLogger(InstrumentTranslator.class.getName());

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
        if(logger.isLoggable(Level.FINE)) {
            logger.fine("loading className:" + classname);
        }

        Modifier modifier = modifierMap.get(classname);
        if(modifier == null) {
            return;
        }
        modifier.modify(this.loader, classname, null, null);
    }
}
