package com.navercorp.pinpoint.profiler.plugin.xml;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.profiler.plugin.xml.transformer.ClassRecipe;

import java.util.Arrays;

public class OverrideMethodInjector implements ClassRecipe {
    private final String methodName;
    private final String[] paramTypes;
    
    public OverrideMethodInjector(String methodName, String[] paramTypes) {
        this.methodName = methodName;
        this.paramTypes = paramTypes;
    }

    @Override
    public void edit(ClassLoader classLoader, InstrumentClass target) throws InstrumentException {
        target.addDelegatorMethod(methodName, paramTypes);
    }

    @Override
    public String toString() {
        return "OverrideMethodInjector[methodName=" + methodName + ", paramTypes" + Arrays.toString(paramTypes) + "]";
    }
}
