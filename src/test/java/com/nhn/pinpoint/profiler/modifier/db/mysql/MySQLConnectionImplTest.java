package com.nhn.pinpoint.profiler.modifier.db.mysql;

import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.DefaultAgent;
import com.nhn.pinpoint.profiler.logging.Slf4jLoggerBinder;
import com.nhn.pinpoint.profiler.util.MockAgent;
import com.nhn.pinpoint.profiler.util.TestClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class MySQLConnectionImplTest {
    private final Logger logger = LoggerFactory.getLogger(MySQLConnectionImplTest.class.getName());

    private TestClassLoader loader;

//    @Before
    public void setUp() throws Exception {
        PLoggerFactory.initialize(new Slf4jLoggerBinder());

        ProfilerConfig profilerConfig = new ProfilerConfig();
        // profiler config를 setter를 열어두는것도 괜찮을듯 하다.
        String path = MockAgent.class.getClassLoader().getResource("pinpoint.config").getPath();
        profilerConfig.readConfigFile(path);

        profilerConfig.setApplicationServerType(ServiceType.TEST_STAND_ALONE);
        DefaultAgent agent = new MockAgent("", profilerConfig);
        loader = new TestClassLoader(agent);
        // agent가 로드한 모든 Modifier를 자동으로 찾도록 변경함.


        loader.initialize();
    }

//    @Test
    public void test() throws Throwable {
//      간접 참조로 실행할 경우의 샘플용으로 commit함.
//        loader.runTest("com.nhn.pinpoint.profiler.modifier.db.mysql.MySQLConnectionImplModifierTest", "testModify");
    }


}
