package com.profiler.common.util;

import com.profiler.common.bo.SqlMetaDataBo;
import com.profiler.common.dto.thrift.SqlMetaData;
import junit.framework.Assert;
import org.junit.Test;

/**
 *
 */
public class RowKeyUtilsTest {
    @Test
    public void testGetSqlId() throws Exception {
        long startTime = System.currentTimeMillis();
        byte[] agents = RowKeyUtils.getSqlId("agent", (short)1234, 1, startTime);

        SqlMetaDataBo sqlId = RowKeyUtils.parseSqlId(agents);
        Assert.assertEquals(sqlId.getAgentId(), "agent");
        Assert.assertEquals(sqlId.getIdentifier(), 1234);
        Assert.assertEquals(sqlId.getHashCode(), 1);
        Assert.assertEquals(sqlId.getStartTime(), startTime);
    }
}
