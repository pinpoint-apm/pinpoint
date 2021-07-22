package com.navercorp.pinpoint.web;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import javax.servlet.Filter;
import java.util.Collections;

@Configuration
public class WebServerConfig {
    @Bean
    public FilterRegistrationBean etagFilterBean() {
        FilterRegistrationBean filterBean = new FilterRegistrationBean();
        Filter filter = new ShallowEtagHeaderFilter();
        filterBean.setFilter(filter);
        filterBean.setUrlPatterns(Collections.singletonList("*"));
        return filterBean;
    }
}
