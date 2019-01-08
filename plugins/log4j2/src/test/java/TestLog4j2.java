import com.navercorp.pinpoint.bootstrap.config.*;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcContext;
import com.navercorp.pinpoint.common.util.PropertyUtils;
import com.navercorp.pinpoint.plugin.log4j2.Log4j2Config;
import com.navercorp.pinpoint.plugin.log4j2.Log4j2Plugin;
import com.navercorp.pinpoint.plugin.log4j2.interceptor.LoggingEventOfLog4j2Interceptor;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @Author: 21627@etransfar.com
 * @Date: 2019/1/7 21:11
 * @Version: 1.0
 */
public class TestLog4j2 {

    private static final String TRANSACTION_ID = "PtxId";

    @Test
    public void testLoggingEventOfLog4j2Interceptor() {

        TraceContext traceContext = new TraceContext() {
            @Override
            public Trace currentTraceObject() {
                return null;
            }

            @Override
            public Trace currentRawTraceObject() {
                return null;
            }

            @Override
            public Trace continueTraceObject(TraceId traceId) {
                return null;
            }

            @Override
            public Trace continueTraceObject(Trace trace) {
                return null;
            }

            @Override
            public Trace newTraceObject() {
                return null;
            }

            @Override
            public Trace newAsyncTraceObject() {
                return null;
            }

            @Override
            public Trace continueAsyncTraceObject(TraceId traceId) {
                return null;
            }

            @Override
            public Trace removeTraceObject() {
                return null;
            }

            @Override
            public Trace removeTraceObject(boolean closeDisableTrace) {
                return null;
            }

            @Override
            public String getAgentId() {
                return null;
            }

            @Override
            public String getApplicationName() {
                return null;
            }

            @Override
            public long getAgentStartTime() {
                return 0;
            }

            @Override
            public short getServerTypeCode() {
                return 0;
            }

            @Override
            public String getServerType() {
                return null;
            }

            @Override
            public int cacheApi(MethodDescriptor methodDescriptor) {
                return 0;
            }

            @Override
            public int cacheString(String value) {
                return 0;
            }

            @Override
            public ParsingResult parseSql(String sql) {
                return null;
            }

            @Override
            public boolean cacheSql(ParsingResult parsingResult) {
                return false;
            }

            @Override
            public TraceId createTraceId(String transactionId, long parentSpanId, long spanId, short flags) {
                return null;
            }

            @Override
            public Trace disableSampling() {
                return null;
            }

            @Override
            public ProfilerConfig getProfilerConfig() {
                return null;
            }

            @Override
            public ServerMetaDataHolder getServerMetaDataHolder() {
                return null;
            }

            @Override
            public JdbcContext getJdbcContext() {
                return null;
            }
        };
        LoggingEventOfLog4j2Interceptor interceptor = new LoggingEventOfLog4j2Interceptor(traceContext);
        interceptor.before(null);
        Assert.assertTrue(MDC.get(TRANSACTION_ID) == null);
    }

    @Test
    public void testLog4j2Config() {
        try {
            Log4j2Config log4j2Config = new Log4j2Config(null);
            log4j2Config.isLog4j2LoggingTransactionInfo();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof NullPointerException);
        }
    }

    @Test
    public void testLog4j2ConfigToString() {
        try {
            Log4j2Config log4j2Config = new Log4j2Config(null);
            log4j2Config.toString();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof NullPointerException);
        }
    }


    @Test
    public void testLog4j2Plugin() {
        try {
            Log4j2Plugin plugin = new Log4j2Plugin();
            plugin.setTransformTemplate(null);
            plugin.setup(null);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof NullPointerException);
        }
    }

}
