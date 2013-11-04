package com.nhn.pinpoint.collector.spring;

import com.nhn.pinpoint.collector.receiver.DispatchHandler;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

/**
 * @author emeroad
 */
@Deprecated
public class ApplicationContextUtils {

    public static GenericApplicationContext createContext(String contextPath) {
        return createContext0(contextPath);
    }

    public static GenericApplicationContext createContext() {
        return createContext0("applicationContext.xml");
    }

    public static DispatchHandler getTcpDispatchHandler(AbstractApplicationContext context) {
        return context.getBean("tcpDispatchHandler", DispatchHandler.class);
    }

    private static GenericApplicationContext createContext0(String contextClassPath) {
        GenericXmlApplicationContext context = new GenericXmlApplicationContext();
        ClassPathResource resource = new ClassPathResource(contextClassPath);
        context.load(resource);

        context.refresh();
        // 순서 보장을 할수가 없음.
        //context.registerShutdownHook();
        return context;
    }
}
