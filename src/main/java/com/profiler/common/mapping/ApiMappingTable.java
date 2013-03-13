package com.profiler.common.mapping;

import com.profiler.common.mapping.code.ServerRegister;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ApiMappingTable {


    public static final ApiMappingTable TABLE;

    static {
        TABLE = new ApiMappingTable();
        TABLE.init();
    }

    // 2147483647
    // xxxxxxxxx  - yy  xxxx는 class 명, yy는 함수명매칭하자. 하자.
    private Map<String, ClassMapping> classNameIndex;
    private Map<Integer, ClassMapping> classIdIndex;

    public ApiMappingTable() {
        this.classNameIndex = new HashMap<String, ClassMapping>(64);
        this.classIdIndex = new HashMap<Integer, ClassMapping>(64);
    }

    public void put(ClassMapping classMapping) {
        this.classNameIndex.put(classMapping.getClassName(), classMapping);
        this.classIdIndex.put(classMapping.getClassId(), classMapping);
    }

    public void init() {
        ServerRegister server = new ServerRegister();
        server.register(this, 5000, 5999);

    }

    public int findMethodApiId0(String className, String methodName, String[] parameter) {
        ClassMapping classMapping = classNameIndex.get(className);
        if (classMapping == null) {
            return -1;
        }
        int classId = classMapping.getClassId();
        MethodMapping methodMapping = classMapping.getMethodMapping(new MethodMapping(methodName, parameter));
        if (methodMapping == null) {
            return -2;
        }
        int methodId = methodMapping.getMethodId();
        return ApiUtils.getApiId(classId, methodId);
    }

    public MethodMapping findMethodMapping0(int apiId) {
        final int classId = ApiUtils.parseClassId(apiId);
        final int methodId = ApiUtils.parseMethodId(apiId);
        ClassMapping classMapping = classIdIndex.get(classId);
        if (classMapping == null) {
            return classIdNotFound(classId);
        }
        MethodMapping methodMapping = classMapping.getMethodMapping(methodId);
        if (methodMapping == null) {
            return methodIdNotFound(classMapping, methodId);
        }
        return methodMapping;
    }

    private MethodMapping methodIdNotFound(ClassMapping classMapping, int methodId) {
        MethodMapping methodMapping = new MethodMapping("methodId not found:" + methodId, null, null);
        new ClassMapping(classMapping.getClassId(), classMapping.getClassName(), methodMapping);
        return methodMapping;
    }

    private MethodMapping classIdNotFound(int classId) {
        MethodMapping methodMapping = new MethodMapping("not found", null, null);
        new ClassMapping(classId, "classId not found:" + classId, methodMapping);
        return methodMapping;
    }


    public static int findApiId(String className, String methodName, String[] parameter) {
        return TABLE.findMethodApiId0(className, methodName, parameter);
    }

    public static MethodMapping findMethodMapping(int apiId) {
        return TABLE.findMethodMapping0(apiId);
    }


}
