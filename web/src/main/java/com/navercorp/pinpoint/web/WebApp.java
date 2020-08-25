package com.navercorp.pinpoint.web;

import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import java.util.Arrays;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, TransactionAutoConfiguration.class, BatchAutoConfiguration.class})
@ImportResource({ "classpath:applicationContext-web.xml", "classpath:servlet-context-web.xml"})
public class WebApp {
    private static final ServerBootLogger logger = ServerBootLogger.getLogger(WebApp.class);


    public static void main(String[] args) {
        try {
            WebStarter starter = new WebStarter(WebApp.class, WebMvcConfig.class);
            starter.start(args);
        } catch (Exception exception) {
            logger.error("[WebApp] could not launch app.", exception);
        }
    }

    @Bean
    public FilterRegistrationBean etagFilterBean() {
        FilterRegistrationBean filterBean = new FilterRegistrationBean();
        ShallowEtagHeaderFilter filter = new ShallowEtagHeaderFilter();
        filterBean.setFilter(filter);
        filterBean.setUrlPatterns(Arrays.asList("*"));
        return filterBean;
    }

}
