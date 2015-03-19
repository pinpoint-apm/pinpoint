package com.navercorp.pinpoint.web.dao.hbase;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class HbaseApiMetaDataDaoTest {

    @Test
    public void getApiMetaDataCachable() {
        // cacheable key - spring expression language
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("agentId", "foo");
        context.setVariable("time", (long) 1);
        context.setVariable("apiId", (int) 2);

        String key = (String) parser.parseExpression(HbaseApiMetaDataDao.SPEL_KEY).getValue(context);
        assertEquals("foo.1.2", key);
    }
}
