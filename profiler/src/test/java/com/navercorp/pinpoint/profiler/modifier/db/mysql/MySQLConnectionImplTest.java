package com.nhn.pinpoint.profiler.modifier.db.mysql;

import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.DefaultAgent;
import com.nhn.pinpoint.profiler.logging.Slf4jLoggerBinder;
import com.nhn.pinpoint.test.MockAgent;
import com.nhn.pinpoint.test.TestClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class MySQLConnectionImplTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private TestClassLoader loader;

//    @Before
    public void setUp() throws Exception {
        PLoggerFactory.initialize(new Slf4jLoggerBinder());
        DefaultAgent agent = MockAgent.of("pinpoint.config");
        loader = new TestClassLoader(agent);
        loader.initialize();
    }

//    @Test
    public void test() throws Throwable {
//      간접 참조로 실행할 경우의 샘플용으로 commit함.
//        loader.runTest("com.nhn.pinpoint.profiler.modifier.db.mysql.MySQLConnectionImplModifierTest", "testModify");
    }


}
