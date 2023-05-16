package com.navercorp.pinpoint.metric.web.mybatis.typehandler;

import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.web.util.TagUtils;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class TagListTypeHandler implements TypeHandler<List<Tag>> {

    @Override
    public void setParameter(PreparedStatement ps, int i, List<Tag> parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.toString());
    }

    @Override
    public List<Tag> getResult(ResultSet rs, String columnName) throws SQLException {
        return TagUtils.parseTags(rs.getString(columnName));
    }

    @Override
    public List<Tag> getResult(ResultSet rs, int columnIndex) throws SQLException {
        return TagUtils.parseTags(rs.getString(columnIndex));
    }

    @Override
    public List<Tag> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        return TagUtils.parseTags(cs.getString(columnIndex));
    }
}
