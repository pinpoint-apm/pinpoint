package com.navercorp.pinpoint.otlp.web;

import com.navercorp.pinpoint.otlp.common.model.AggreFunc;
import com.navercorp.pinpoint.otlp.common.model.AggreTemporality;
import com.navercorp.pinpoint.otlp.common.model.DataType;
import com.navercorp.pinpoint.otlp.common.model.MetricType;
import com.navercorp.pinpoint.otlp.web.vo.FieldAttribute;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FieldAttributeHandler implements TypeHandler<FieldAttribute> {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Override
    public void setParameter(PreparedStatement ps, int i, FieldAttribute parameter, JdbcType jdbcType) throws SQLException {
        throw new UnsupportedOperationException("FieldAttribute is only used for result.");
    }

    @Override
    public FieldAttribute getResult(ResultSet rs, String columnName) throws SQLException {
        return parseResult(rs);
    }

    @Override
    public FieldAttribute getResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseResult(rs);
    }

    @Override
    public FieldAttribute getResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseResult(cs);
    }

    private FieldAttribute parseResult(ResultSet rs) {
        try {
            String fieldName = rs.getString("fieldName");
            MetricType metricType = MetricType.forNumber(rs.getInt("metricType"));
            DataType dataType = DataType.forNumber(rs.getInt("dataType"));
            AggreFunc aggreFunc = AggreFunc.forNumber(rs.getInt("aggreFunc"));
            AggreTemporality aggreTemporality = AggreTemporality.forNumber(rs.getInt("aggregationTemporality"));
            String description = rs.getString("description");
            String unit = rs.getString("unit");
            String version = rs.getString("version");
            return new FieldAttribute(fieldName, metricType, dataType, aggreFunc, aggreTemporality, description, unit, version);
        } catch (SQLException e) {
            logger.warn("FieldAttribute parsing error.");
            return null;
        }
    }
    private FieldAttribute parseResult(CallableStatement cs) {
        try {
            String fieldName = cs.getString("fieldName");
            MetricType metricType = MetricType.forNumber(cs.getInt("metricType"));
            DataType dataType = DataType.forNumber(cs.getInt("dataType"));
            AggreFunc aggreFunc = AggreFunc.forNumber(cs.getInt("aggreFunc"));
            AggreTemporality aggreTemporality = AggreTemporality.forNumber(cs.getInt("aggregationTemporality"));
            String description = cs.getString("description");
            String unit = cs.getString("unit");
            String version = cs.getString("version");
            return new FieldAttribute(fieldName, metricType, dataType, aggreFunc, aggreTemporality, description, unit, version);
        } catch (SQLException e) {
            logger.warn("FieldAttribute parsing error.");
            return null;
        }
    }
}
