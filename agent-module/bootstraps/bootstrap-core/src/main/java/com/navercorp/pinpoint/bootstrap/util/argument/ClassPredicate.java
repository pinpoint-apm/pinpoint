package com.navercorp.pinpoint.bootstrap.util.argument;

import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ClassPredicate implements Predicate {
    private final PluginLogger logger;

    private final Class<?> clazz;
    private final int index;


    public ClassPredicate(PluginLogger logger, Class<?> clazz, int index) {
        this.logger = logger;
        this.clazz = clazz;
        this.index = index;
    }

    @Override
    public boolean test(Object[] args) {
        final Object param = args[index];
        if (!clazz.isInstance(param)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Invalid args[{}] object, Not implemented of {}. args[{}]={}", index, clazz, index, param);
            }
            return false;
        }
        return true;
    }

    @Override
    public int index() {
        return index;
    }
}
