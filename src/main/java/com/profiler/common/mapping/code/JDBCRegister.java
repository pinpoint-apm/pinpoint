package com.profiler.common.mapping.code;

import com.profiler.common.mapping.ApiMappingTable;
import com.profiler.common.mapping.ClassMapping;
import com.profiler.common.mapping.Register;
import com.profiler.common.mapping.MethodMapping;

/**
 *
 */
public class JDBCRegister implements Register {
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

    @Override
    public void register(ApiMappingTable apiMappingTable, int startRange, int endRange) {
        apiMappingTable.put(MysqlDriver);
        apiMappingTable.put(MysqlConnection);
        apiMappingTable.put(MySqlPreparedStatement);
        apiMappingTable.put(MySqlStatementImpl);


    }
}
