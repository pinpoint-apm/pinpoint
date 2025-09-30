/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector.event.config;

import com.navercorp.pinpoint.collector.event.SpanStorePublisher;
import com.navercorp.pinpoint.collector.event.SpanStorePublisherImpl;
import com.navercorp.pinpoint.common.server.event.ContextSupplier;
import com.navercorp.pinpoint.common.server.event.SimpleContextSupplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class CollectorEventConfiguration {

    private final Logger logger = LogManager.getLogger(CollectorEventConfiguration.class);


    @Bean
    public SpanStorePublisher spanPublisher(ApplicationEventPublisher publisher,
                                            Optional<ContextSupplier> optionalSupplier) {
        ContextSupplier supplier = optionalSupplier.orElseGet(SimpleContextSupplier::new);
        logger.info("ContextSupplier:{}", supplier);
        return new SpanStorePublisherImpl(publisher, supplier);
    }
}
