package com.navercorp.pinpoint.web;

import com.navercorp.pinpoint.web.interceptor.PerformanceLoggingInterceptor;
import com.navercorp.pinpoint.web.servlet.HttpIntentRoutingFilter;
import com.navercorp.pinpoint.web.servlet.VersionPrefixRewriter;
import jakarta.servlet.Filter;
import jakarta.servlet.MultipartConfigElement;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.ErrorPageFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.List;

@Configuration
public class WebServerConfig {
    @Bean
    public FilterRegistrationBean<Filter> etagFilterBean() {
        Filter filter = new ShallowEtagHeaderFilter();

        FilterRegistrationBean<Filter> filterBean = new FilterRegistrationBean<>();
        filterBean.setFilter(filter);
        filterBean.setName(filter.getClass().getSimpleName());
        filterBean.setUrlPatterns(List.of("/assets/*", "*.html", "/main"));
        return filterBean;
    }

    @Bean
    public Filter errorPageFilter() {
        return new ErrorPageFilter();
    }

    @Bean
    public Filter rewriteFilter() {
        final VersionPrefixRewriter rewriter = new VersionPrefixRewriter();
        return new HttpIntentRoutingFilter(rewriter);
    }

    @Bean
    public PerformanceLoggingInterceptor performanceLoggingInterceptor(
            @Value("${pinpoint.web.performance-logging-interceptor.threshold:3000}") int threshold) {
        return new PerformanceLoggingInterceptor(threshold);
    }

    @Bean
    public ServletRegistrationBean<DispatcherServlet> servletRegistrationBean(
            DispatcherServlet dispatcherServlet,
            WebMvcProperties webMvcProperties,
            ObjectProvider<MultipartConfigElement> multipartConfig) {

        final ServletRegistrationBean<DispatcherServlet> bean = new ServletRegistrationBean<>(dispatcherServlet, "/api/*", "/monitor/*");
        bean.setName(DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME);
        bean.setLoadOnStartup(webMvcProperties.getServlet().getLoadOnStartup());
        bean.setMultipartConfig(multipartConfig.getObject());
        return bean;
    }
}
