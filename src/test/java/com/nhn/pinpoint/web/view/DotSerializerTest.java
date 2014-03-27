package com.nhn.pinpoint.web.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhn.pinpoint.web.vo.TransactionId;
import com.nhn.pinpoint.web.vo.scatter.Dot;
import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.util.logging.resources.logging;

/**
 * @author emeroad
 */
public class DotSerializerTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testSerialize() throws Exception {
        TransactionId transactionId = new TransactionId("aigw.dev.1^1395798795017^1527177");
        Dot dot = new Dot(transactionId, 100, 99, 1, "agent");
        String jsonValue = mapper.writeValueAsString(dot);
        Assert.assertEquals("[100,99,\"aigw.dev.1^1395798795017^1527177\",1]", jsonValue);
    }
}
