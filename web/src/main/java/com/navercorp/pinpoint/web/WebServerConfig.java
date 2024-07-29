package com.navercorp.pinpoint.web;

import com.navercorp.pinpoint.web.interceptor.PerformanceLoggingInterceptor;
import com.navercorp.pinpoint.web.servlet.HttpIntentRoutingFilter;
import com.navercorp.pinpoint.web.servlet.VersionPrefixRewriter;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.support.ErrorPageFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

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
    public Filter rewriteFilter(@Value("${pinpoint.web.version-prefix.main-path:/index.html}") String mainPath,
                                @Value("${pinpoint.web.version-prefix.special-paths:}") List<String> specialPaths,
                                @Value("${pinpoint.web.version-prefix.resource-paths:/assets,/fronts,/img}") List<String> resourcePaths) {
        final VersionPrefixRewriter rewriter = new VersionPrefixRewriter(mainPath, specialPaths, resourcePaths);
        return new HttpIntentRoutingFilter(rewriter);
    }

    @Bean
    public PerformanceLoggingInterceptor performanceLoggingInterceptor(
            @Value("${pinpoint.web.performance-logging-interceptor.threshold:3000}") int threshold) {
        return new PerformanceLoggingInterceptor(threshold);
    }
}
