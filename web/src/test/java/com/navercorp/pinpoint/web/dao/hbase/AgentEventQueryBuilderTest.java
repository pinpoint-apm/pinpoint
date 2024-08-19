package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.server.util.AgentEventType;
import io.jsonwebtoken.impl.lang.Bytes;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.filter.Filter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

class AgentEventQueryBuilderTest {

    AgentEventFilterBuilder filterBuilder = new AgentEventFilterBuilder();

    @Test
    void excludeFilter_single() throws IOException {

        Filter filter = filterBuilder.excludeFilter(Set.of(AgentEventType.AGENT_CONNECTED));

        KeyValue connect = newKeyValue(AgentEventType.AGENT_CONNECTED);

        Filter.ReturnCode connectReturn = filter.filterCell(connect);
        Assertions.assertEquals(Filter.ReturnCode.SKIP, connectReturn);


        KeyValue ping = newKeyValue(AgentEventType.AGENT_PING);

        Filter.ReturnCode pingReturn = filter.filterCell(ping);
        Assertions.assertEquals(Filter.ReturnCode.INCLUDE, pingReturn);
    }

    @Test
    void excludeFilter_multi() throws IOException {

        Filter filter = filterBuilder.excludeFilter(Set.of(AgentEventType.AGENT_CONNECTED, AgentEventType.AGENT_SHUTDOWN));

        KeyValue connect = newKeyValue(AgentEventType.AGENT_CONNECTED);

        Filter.ReturnCode returnCode = filter.filterCell(connect);
        Assertions.assertEquals(Filter.ReturnCode.SKIP, returnCode);


        KeyValue ping = newKeyValue(AgentEventType.AGENT_PING);

        Filter.ReturnCode pingReturn = filter.filterCell(ping);
        Assertions.assertEquals(Filter.ReturnCode.INCLUDE, pingReturn);
    }

    private KeyValue newKeyValue(AgentEventType agentConnected) {
        byte[] qualifier = Bytes.toBytes(agentConnected.getCode());
        return new KeyValue(new byte[1], new byte[1], qualifier);
    }


    @Test
    void includeFilter_single() throws IOException {

        Filter filter = filterBuilder.includeFilter(Set.of(AgentEventType.AGENT_CONNECTED));

        KeyValue connect = newKeyValue(AgentEventType.AGENT_CONNECTED);

        Filter.ReturnCode connectReturn = filter.filterCell(connect);
        Assertions.assertEquals(Filter.ReturnCode.INCLUDE, connectReturn);


        KeyValue ping = newKeyValue(AgentEventType.AGENT_PING);

        Filter.ReturnCode pingReturn = filter.filterCell(ping);
        Assertions.assertEquals(Filter.ReturnCode.SKIP, pingReturn);
    }


    @Test
    void includeFilter_multi() throws IOException {

        Filter filter = filterBuilder.includeFilter(Set.of(AgentEventType.AGENT_CONNECTED, AgentEventType.AGENT_SHUTDOWN));

        KeyValue connect = newKeyValue(AgentEventType.AGENT_CONNECTED);

        Filter.ReturnCode connectReturn = filter.filterCell(connect);
        Assertions.assertEquals(Filter.ReturnCode.INCLUDE, connectReturn);


        KeyValue ping = newKeyValue(AgentEventType.AGENT_PING);

        Filter.ReturnCode pingReturn = filter.filterCell(ping);
        Assertions.assertEquals(Filter.ReturnCode.SKIP, pingReturn);
    }


}