package com.navercorp.pinpoint.common.server.util.time;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

@Configuration
public class RangeValidatorConfiguration {

    @Bean
    @Primary
    public RangeValidator rangeValidator2d() {
        return new ForwardRangeValidator(Duration.ofDays(2));
    }

    @Bean
    public RangeValidator rangeValidator7d() {
        return new ForwardRangeValidator(Duration.ofDays(7));
    }

    @Bean
    public RangeValidator rangeValidator14d() {
        return new ForwardRangeValidator(Duration.ofDays(14));
    }

    @Bean
    public RangeValidator rangeValidator30d() {
        return new ForwardRangeValidator(Duration.ofDays(30));
    }

    @Bean
    public RangeValidator reverseRangeValidator2d() {
        return new ReverseRangeValidator(Duration.ofDays(2));
    }

    @Bean
    public RangeValidator reverseRangeValidator7d() {
        return new ReverseRangeValidator(Duration.ofDays(7));
    }

    @Bean
    public RangeValidator reverseRangeValidator14d() {
        return new ReverseRangeValidator(Duration.ofDays(14));
    }

    @Bean
    public RangeValidator reverseRangeValidator30d() {
        return new ReverseRangeValidator(Duration.ofDays(30));
    }
}
