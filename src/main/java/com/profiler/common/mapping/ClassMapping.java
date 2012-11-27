package com.profiler.common.mapping;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ClassMapping {
    private int classId;
    private String className;
    private Map<Integer, MethodMapping> methodIdIndex = new HashMap<Integer, MethodMapping>();
    private Map<MethodMapping, MethodMapping> methodMapping = new HashMap<MethodMapping, MethodMapping>();

    public ClassMapping(int classId, String className, MethodMapping... methodIdIndex) {
        this.classId = classId;
        this.className = className;

        this.methodIdIndex = new HashMap<Integer, MethodMapping>();
        this.methodMapping = new HashMap<MethodMapping, MethodMapping>();
        for (int i = 0; i < methodIdIndex.length; i++) {
            this.methodIdIndex.put(i, methodIdIndex[i]);
            this.methodMapping.put(methodIdIndex[i], methodIdIndex[i]);
            methodIdIndex[i].setClassMapping(this);
            methodIdIndex[i].setMethodId(i);
        }
    }

    public int getClassId() {
        return classId;
    }

    public String getClassName() {
        return className;
    }

    public MethodMapping getMethodMapping(int methodId) {
        return methodIdIndex.get(methodId);
    }

    public MethodMapping getMethodMapping(MethodMapping mapping) {
        return methodMapping.get(mapping);
    }
}
