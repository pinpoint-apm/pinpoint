/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.web;

import com.navercorp.pinpoint.web.problem.ProblemWrapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.zalando.problem.Problem;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.spring.web.advice.AdviceTrait;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

/**
 * @author intr3p1d
 */
@Configuration
public class ProblemSpringWebConfig {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer addCustomBigDecimalDeserialization(
            @Qualifier("pinpointWebProblemModule") ProblemModule problemModule,
            @Qualifier("pinpointConstraintViolationProblemModule") ConstraintViolationProblemModule constraintViolationProblemModule
    ) {
        return (Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) ->
                jacksonObjectMapperBuilder.modules(
                        problemModule, constraintViolationProblemModule
                ).mixIn(Problem.class, ProblemWrapper.class);
    }

    @Bean
    public ProblemModule pinpointWebProblemModule() {
        return new ProblemModule().withStackTraces(true);
    }

    @Bean
    public ConstraintViolationProblemModule pinpointConstraintViolationProblemModule() {
        return new ConstraintViolationProblemModule();
    }

    @Bean
    @Primary
    public AdviceTrait pinpointExceptionHandling() {
        return new ExceptionHandling();
    }
}
