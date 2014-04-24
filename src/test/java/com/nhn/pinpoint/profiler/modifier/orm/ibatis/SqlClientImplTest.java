package com.nhn.pinpoint.profiler.modifier.orm.ibatis;

import com.nhn.pinpoint.ProductInfo;
import com.nhn.pinpoint.bootstrap.ContextClassLoaderExecuteTemplate;
import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.DefaultAgent;
import com.nhn.pinpoint.profiler.DummyInstrumentation;
import com.nhn.pinpoint.profiler.logging.Slf4jLoggerBinder;
import com.nhn.pinpoint.profiler.util.MockAgent;
import com.nhn.pinpoint.profiler.util.TestClassLoader;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.Callable;

/**
 * @author emeroad
 */
public class SqlClientImplTest {

    private static TestClassLoader LOADER;
    private static ContextClassLoaderExecuteTemplate TEMPLATE;

    @Before
    public void setUp() throws Exception {
        PLoggerFactory.initialize(new Slf4jLoggerBinder());

        ProfilerConfig profilerConfig = new ProfilerConfig();

        String path = ProfilerConfig.class.getClassLoader().getResource("pinpoint.config").getPath();
        profilerConfig.readConfigFile(path);

        profilerConfig.setApplicationServerType(ServiceType.STAND_ALONE);
        DefaultAgent agent = new MockAgent("", new DummyInstrumentation(), profilerConfig);
        LOADER = new TestClassLoader(agent);

        TEMPLATE = new ContextClassLoaderExecuteTemplate<Callable>(LOADER);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testModify() throws Throwable {

        TEMPLATE.execute(new Callable() {
            @Override
            public Object call() throws Exception {
                try {
                    LOADER.runTest("com.nhn.pinpoint.profiler.modifier.orm.ibatis.SqlMapClientImplModifierTest", "testModify");
                } catch (Throwable th) {
                    throw new RuntimeException(th.getMessage(), th);
                }
                return null;
            }
        });

    }
}
