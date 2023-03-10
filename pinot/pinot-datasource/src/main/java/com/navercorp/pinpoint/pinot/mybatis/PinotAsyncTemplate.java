package com.navercorp.pinpoint.pinot.mybatis;

import com.navercorp.pinpoint.pinot.datasource.ParameterRecorder;
import com.navercorp.pinpoint.pinot.datasource.PinotDataSource;
import com.navercorp.pinpoint.pinot.datasource.StatementWrapper;
import com.navercorp.pinpoint.pinot.datasource.WrappedPinotConnection;
import com.navercorp.pinpoint.pinot.util.JdbcUtils;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pinot.client.Connection;
import org.apache.pinot.client.PinotResultSet;
import org.apache.pinot.client.PreparedStatement;
import org.apache.pinot.client.ResultSetGroup;
import org.apache.pinot.client.utils.DriverUtils;
import org.mybatis.spring.MyBatisExceptionTranslator;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;

public class PinotAsyncTemplate {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final SqlSessionFactory sqlSessionFactory;
    private final Configuration configuration;
    private final PinotDataSource dataSource;
    private final TransactionFactory transactionFactory = new ManagedTransactionFactory();

    private final PersistenceExceptionTranslator exceptionTranslator;

    private static final String LIMIT_STATEMENT = "LIMIT";


    public PinotAsyncTemplate(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = Objects.requireNonNull(sqlSessionFactory, "sqlSessionFactory");
        this.configuration = sqlSessionFactory.getConfiguration();
        this.dataSource = (PinotDataSource) configuration.getEnvironment().getDataSource();
        this.exceptionTranslator = new MyBatisExceptionTranslator(dataSource, true);
    }

    public <E> Future<List<E>> selectList(String statement) {
        return this.selectList(statement, null, RowBounds.DEFAULT);
    }

    public <E> Future<List<E>> selectList(String statement, Object parameter) {
        return this.selectList(statement, parameter, RowBounds.DEFAULT);
    }

    private <E> Future<List<E>> selectList(String statement, Object parameter, RowBounds rowBounds) {
        WrappedPinotConnection connection = null;
        try {
            connection = (WrappedPinotConnection) dataSource.getConnection();
            Connection session = connection.getSession();

            MappedStatement mappedStatement = configuration.getMappedStatement(statement);
            BoundSql boundSql = mappedStatement.getBoundSql(parameter);

            PreparedStatement pinotStatement = preparedStatement(session, mappedStatement, boundSql);

            ParameterHandler parameterHandler = configuration.newParameterHandler(mappedStatement, parameter, boundSql);
            bindParameter(pinotStatement, parameterHandler);

            Executor executor = configuration.newExecutor(transactionFactory.newTransaction(connection));
            StatementHandler handler = new StatementHandler(configuration, executor, mappedStatement, parameterHandler, rowBounds, boundSql);
            return executeAsync(connection, pinotStatement, handler);
        } catch (Throwable th) {
            RuntimeException exception = translateException(th);
            return CompletableFuture.failedFuture(exception);
        } finally {
            JdbcUtils.closeConnection(connection);
        }
    }


    private PreparedStatement preparedStatement(Connection session, MappedStatement mappedStatement, BoundSql boundSql) {
        String sql = boundSql.getSql();
        int fetchSize = getFetchSize(mappedStatement.getFetchSize());
        sql = checkLimitStatement(sql, fetchSize);

        return session.prepareStatement(sql);
    }

    /**
     * @see org.apache.pinot.client.PinotPreparedStatement
     */
    private String checkLimitStatement(String sql, Integer fetchSize) {
        if (!DriverUtils.queryContainsLimitStatement(sql)) {
            return sql.concat(" " + LIMIT_STATEMENT + " " + fetchSize);
        }
        return sql;
    }

    private int getFetchSize(Integer fetchSize) {
        if (fetchSize == null) {
            return Integer.MAX_VALUE;
        }
        return fetchSize;
    }

    private <E> Future<List<E>> executeAsync(java.sql.Connection connection, PreparedStatement preparedStatement,
                                             StatementHandler handler) {
        Future<ResultSetGroup> resultSetGroupFuture = preparedStatement.executeAsync();
        Future<List<E>> transformFuture = new TransformFuture<>(resultSetGroupFuture, new Function<ResultSetGroup, List<E>> () {
            @Override
            public List<E> apply(ResultSetGroup resultSetGroup) {
                try (ResultSet resultSet = toResultSet(resultSetGroup);) {
                    Statement statement = new StatementWrapper(connection, resultSet);
                    return handler.handleResultSet(statement);
                } catch (SQLException e) {
                    throw translateException(e);
                }
            }
        });
        return transformFuture;
    }

    private RuntimeException translateException(Throwable th) {
        if (th instanceof PersistenceException) {
            DataAccessException dataAccessException = exceptionTranslator.translateExceptionIfPossible((PersistenceException) th);
            if (dataAccessException != null) {
                return dataAccessException;
            }
        }
        return new PersistenceException(th);
    }

    private ResultSet toResultSet(ResultSetGroup resultSetGroup) {
        if (resultSetGroup == null) {
//            return null or empty??
            return PinotResultSet.empty();
        }
        if (resultSetGroup.getResultSetCount() == 0) {
            return PinotResultSet.empty();
        }
        return new PinotResultSet(resultSetGroup.getResultSet(0));
    }


    private static class StatementHandler {
        private final Configuration configuration;
        private final Executor executor;
        private final MappedStatement mappedStatement;
        private final ParameterHandler parameterHandler;
        private final RowBounds rowBounds;
        private final BoundSql boundSql;

        public StatementHandler(Configuration configuration,
                                Executor executor,
                                MappedStatement mappedStatement,
                                ParameterHandler parameterHandler,
                                RowBounds rowBounds,
                                BoundSql boundSql) {
            this.configuration = Objects.requireNonNull(configuration, "configuration");
            this.executor = Objects.requireNonNull(executor, "executor");
            this.mappedStatement = Objects.requireNonNull(mappedStatement, "mappedStatement");
            this.parameterHandler = Objects.requireNonNull(parameterHandler, "parameterHandler");
            this.rowBounds = Objects.requireNonNull(rowBounds, "rowBounds");
            this.boundSql = Objects.requireNonNull(boundSql, "boundSql");
        }

        public <E> List<E> handleResultSet(java.sql.Statement statement) throws SQLException {
            ResultSetHandler resultSetHandler = configuration.newResultSetHandler(executor, mappedStatement, rowBounds, parameterHandler, Executor.NO_RESULT_HANDLER, boundSql);
            return resultSetHandler.handleResultSets(statement);
        }
    }

    private void bindParameter(PreparedStatement preparedStatement, ParameterHandler parameterHandler) throws SQLException {
        try (java.sql.PreparedStatement recorder = new ParameterRecorder(preparedStatement)) {
            parameterHandler.setParameters(recorder);
        }
    }

}
