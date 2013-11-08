package com.nhn.pinpoint.profiler.modifier.db.cubrid;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.context.DatabaseInfo;
import com.nhn.pinpoint.profiler.modifier.db.ConnectionStringParser;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author emeroad
 */
public class CubridConnectionStringParserTest {

    private final ConnectionStringParser parser = new CubridConnectionStringParser();
    @Test
    public void testParse() {
        String cubrid = "jdbc:cubrid:10.99.196.126:34001:nrdwapw:::?charset=utf-8:";

        DatabaseInfo dbInfo = parser.parse(cubrid);

        Assert.assertEquals(dbInfo.getType(), ServiceType.CUBRID);
        Assert.assertEquals(dbInfo.getHost().get(0), "10.99.196.126:34001");
        Assert.assertEquals(dbInfo.getDatabaseId(), "nrdwapw");
        Assert.assertEquals(dbInfo.getUrl(), "jdbc:cubrid:10.99.196.126:34001:nrdwapw:::");
    }

    @Test
    public void testNullParse() {

        DatabaseInfo dbInfo = parser.parse(null);

        Assert.assertEquals(dbInfo.getType(), ServiceType.CUBRID);
        Assert.assertEquals(dbInfo.getHost().get(0), "error");
        Assert.assertEquals(dbInfo.getDatabaseId(), "error");
        Assert.assertEquals(dbInfo.getUrl(), null);

//        Assert.assertEquals(dbInfo.getUrl(), cubrid);
    }
}
