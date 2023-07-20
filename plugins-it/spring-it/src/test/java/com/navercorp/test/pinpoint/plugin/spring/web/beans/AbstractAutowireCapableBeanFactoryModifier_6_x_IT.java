
package com.navercorp.test.pinpoint.plugin.spring.web.beans;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@PinpointAgent(AgentPath.PATH)
@JvmVersion(17)
@Dependency({"org.springframework:spring-context:[6.0.0.RELEASE,)", "cglib:cglib-nodep:3.1"})
@PinpointConfig("pinpoint-spring-bean-test.config")
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-spring-plugin"})
public class AbstractAutowireCapableBeanFactoryModifier_6_x_IT {

    @Test
    public void test() throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring-beans-test.xml");

        Maru maru = context.getBean(Maru.class);
        maru.publicMethod();
        maru.protectedMethod();

        Morae morae = (Morae) context.getBean("morae");
        morae.doSomething();

        Morae duplicatedMorae = (Morae) context.getBean("duplicatedMorae");
        duplicatedMorae.doSomething();

        Mozzi mozzi = (Mozzi) context.getBean("mozzi");
        mozzi.doSomething();

        Excluded excluded = (Excluded) context.getBean("excluded");
        excluded.doSomething();

        Outer outer = (Outer) context.getBean("outer");
        outer.doSomething();

        Inner inner = outer.getInner();
        inner.doSomething();

        ProxyTarget proxyTarget = context.getBean(ProxyTarget.class);
        proxyTarget.doSomething();


        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        verifier.verifyTrace(Expectations.event("SPRING_BEAN", Outer.class.getMethod("setInner", Inner.class)));
        verifier.verifyTrace(Expectations.event("SPRING_BEAN", Maru.class.getMethod("publicMethod")));
        verifier.verifyTrace(Expectations.event("SPRING_BEAN", Morae.class.getMethod("doSomething")));
        verifier.verifyTrace(Expectations.event("SPRING_BEAN", Morae.class.getMethod("doSomething")));
        verifier.verifyTrace(Expectations.event("SPRING_BEAN", Mozzi.class.getMethod("doSomething")));
        verifier.verifyTrace(Expectations.event("SPRING_BEAN", Outer.class.getMethod("doSomething")));
        verifier.verifyTrace(Expectations.event("SPRING_BEAN", Outer.class.getMethod("getInner")));
        verifier.verifyTrace(Expectations.event("SPRING_BEAN", Inner.class.getMethod("doSomething")));
        verifier.verifyTrace(Expectations.event("SPRING_BEAN", ProxyTarget.class.getMethod("doSomething")));

        verifier.verifyTraceCount(0);
    }
}