package com.navercorp.pinpoint.metric.web.mybatis.typehandler;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DoubleTypeHandler implements TypeHandler<Double> {
    @Override
    public void setParameter(PreparedStatement ps, int i, Double parameter, JdbcType jdbcType) throws SQLException {
        ps.setDouble(i, parameter);
    }

    @Override
    public Double getResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getDouble(columnName);
    }

    @Override
    public Double getResult(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getDouble(columnIndex);
    }

    @Override
    public Double getResult(CallableStatement cs, int columnIndex) throws SQLException {
        return cs.getDouble(columnIndex);
    }
}
