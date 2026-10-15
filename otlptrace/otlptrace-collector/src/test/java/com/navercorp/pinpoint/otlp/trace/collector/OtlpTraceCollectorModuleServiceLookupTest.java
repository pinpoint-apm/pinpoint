/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.otlp.trace.collector;

import com.navercorp.pinpoint.collector.uid.ServiceLookupConfiguration;
import com.navercorp.pinpoint.collector.uid.service.ServiceLookupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exactly one ServiceLookupService is defined by {@link OtlpTraceCollectorModule} whether
 * {@code pinpoint.collector.service.lookup.enabled} comes from the module's own root properties
 * or is known before the module is parsed (application.yml, command line).
 * <p>
 * The lookup configurations are conditional on that key and are evaluated when parsed and again
 * when their beans are registered. When the module's property files were imported after them, a
 * key that only the root file sets (enabled=true) was absent at the first evaluation and present
 * at the second, so both the caching and the static configuration dropped out and the context
 * failed with "required a bean of type ServiceLookupService that could not be found".
 * Only bean definitions are built here: nothing is instantiated and no storage is reached.
 */
class OtlpTraceCollectorModuleServiceLookupTest {

    // module switch, set by the starter application.yml before any configuration is parsed
    private static final String MODULE_ENABLED = "pinpoint.modules.collector.otlptrace.enabled";

    private static String[] serviceLookupBeans(Map<String, Object> knownBeforeParsing) {
        final Map<String, Object> early = new HashMap<>(knownBeforeParsing);
        early.put(MODULE_ENABLED, "true");
        final StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("early", early));
        final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        new AnnotatedBeanDefinitionReader(beanFactory, environment).register(OtlpTraceCollectorModule.class);

        final ConfigurationClassPostProcessor processor = new ConfigurationClassPostProcessor();
        processor.setEnvironment(environment);
        processor.setResourceLoader(new DefaultResourceLoader());
        processor.postProcessBeanDefinitionRegistry(beanFactory);

        return beanFactory.getBeanNamesForType(ServiceLookupService.class, true, false);
    }

    @Test
    void keyFromTheRootPropertiesOnly_definesTheCachingLookup() {
        // pinpoint-collector-root.properties: pinpoint.collector.service.lookup.enabled=true
        assertThat(serviceLookupBeans(Map.of())).containsExactly("cachingServiceLookupService");
    }

    @Test
    void enabledBeforeParsing_definesTheCachingLookup() {
        assertThat(serviceLookupBeans(Map.of(ServiceLookupConfiguration.ENABLED_KEY, "true")))
                .containsExactly("cachingServiceLookupService");
    }

    @Test
    void disabledBeforeParsing_definesTheStaticLookup() {
        assertThat(serviceLookupBeans(Map.of(ServiceLookupConfiguration.ENABLED_KEY, "false")))
                .containsExactly("staticServiceLookupService");
    }
}
