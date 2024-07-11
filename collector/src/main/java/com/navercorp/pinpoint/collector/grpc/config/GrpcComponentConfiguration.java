/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.collector.grpc.config;

import com.navercorp.pinpoint.collector.receiver.grpc.service.DefaultServerRequestFactory;
import com.navercorp.pinpoint.collector.receiver.grpc.service.ServerRequestFactory;
import com.navercorp.pinpoint.common.server.bo.filter.SpanEventFilter;
import com.navercorp.pinpoint.common.server.bo.grpc.CollectorGrpcSpanFactory;
import com.navercorp.pinpoint.common.server.bo.grpc.GrpcSpanBinder;
import io.grpc.ServerTransportFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author emeroad
 */
@Configuration
public class GrpcComponentConfiguration {

    public GrpcComponentConfiguration() {
    }


    @Bean
    public ServerRequestFactory serverRequestFactory() {
        return new DefaultServerRequestFactory();
    }

    @Bean
    public List<ServerTransportFilter> serverTransportFilterList() {
        return List.of();
    }


    @Bean
    public CollectorGrpcSpanFactory collectorGrpcSpanFactory(SpanEventFilter spanEventFilter) {
        GrpcSpanBinder grpcSpanBinder = new GrpcSpanBinder();
        return new CollectorGrpcSpanFactory(grpcSpanBinder, spanEventFilter);
    }

}