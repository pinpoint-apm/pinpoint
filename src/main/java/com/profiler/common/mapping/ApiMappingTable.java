package com.profiler.common.mapping;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ApiMappingTable {
    public static final String[] NULL = new String[]{};
    // 2147483
    public static final int MySqlDriverCode = 1000;
    private static final ClassMapping MysqlDriver = new ClassMapping(MySqlDriverCode, "com.mysql.jdbc.NonRegisteringDriver",
            new MethodMapping("connect", new String[]{"java.lang.String", "java.util.Properties"}, new String[]{"url", "info"}));


    public static final int MySqlConnectionCode = 1001;
    private static final ClassMapping MysqlConnection = new ClassMapping(MySqlConnectionCode, "com.mysql.jdbc.ConnectionImpl",
            new MethodMapping("setAutoCommit", new String[]{"boolean"}, new String[]{"autoCommitFlag"}),
            new MethodMapping("commit", NULL, NULL),
            new MethodMapping("rollback", NULL, NULL),
            new MethodMapping("prepareStatement", new String[]{"java.lang.String"}, new String[]{"sql"})
    );

    public static final int MySqlPreparedStatementCode = 1002;
    private static final ClassMapping MySqlPreparedStatement = new ClassMapping(MySqlPreparedStatementCode, "com.mysql.jdbc.PreparedStatement",
            new MethodMapping("execute", NULL, NULL),
            new MethodMapping("executeQuery", NULL, NULL),
            new MethodMapping("executeUpdate", NULL, NULL)
    );

    public static final int MySqlStatementImplCode = 1003;
    private static final ClassMapping MySqlStatementImpl = new ClassMapping(MySqlStatementImplCode, "com.mysql.jdbc.StatementImpl",
            new MethodMapping("executeUpdate", new String[]{"java.lang.String"}, new String[]{"sql"}),
            new MethodMapping("executeUpdate", new String[]{"java.lang.String", "boolean"}, new String[]{"sql", "returnGeneratedKeys"}),
            new MethodMapping("execute", new String[]{"java.lang.String"}, new String[]{"sql"}),
            new MethodMapping("execute", new String[]{"java.lang.String", "boolean"}, new String[]{"sql", "returnGeneratedKeys"})
    );


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
        this.put(MysqlDriver);
        this.put(MysqlConnection);
        this.put(MySqlPreparedStatement);
        this.put(MySqlStatementImpl);
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
