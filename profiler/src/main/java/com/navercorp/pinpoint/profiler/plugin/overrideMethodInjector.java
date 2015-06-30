package com.navercorp.pinpoint.profiler.plugin;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.profiler.plugin.transformer.ClassRecipe;

public class overrideMethodInjector implements ClassRecipe {
    private final String methodName;
    private final String[] paramTypes;
    
    public overrideMethodInjector(String methodName, String[] paramTypes) {
        this.methodName = methodName;
        this.paramTypes = paramTypes;
    }

    @Override
    public void edit(ClassLoader classLoader, InstrumentClass target) throws InstrumentException {
        target.addDelegatorMethod(methodName, paramTypes);
    }

    @Override
    public String toString() {
        return "overrideMethodInjector[methodName=" + methodName + ", paramTypes" + paramTypes + "]";
    }
}
