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

package com.navercorp.pinpoint.collector.handler.grpc;

import com.navercorp.pinpoint.collector.sampler.SpanSamplerFactory;
import com.navercorp.pinpoint.collector.sampler.TrueSampler;
import com.navercorp.pinpoint.collector.sampling.tail.TailSampler;
import com.navercorp.pinpoint.collector.service.TraceService;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.io.GrpcSpanFactory;
import com.navercorp.pinpoint.common.server.io.ServerHeader;
import com.navercorp.pinpoint.common.server.io.ServerRequest;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GrpcSpanHandlerTailTest {

    @Test
    void whenTailEnabled_routesToTailSampler_bypassingTraceServiceLoop() {
        TraceService traceService = Mockito.mock(TraceService.class);
        GrpcSpanFactory factory = Mockito.mock(GrpcSpanFactory.class);
        SpanSamplerFactory samplerFactory = Mockito.mock(SpanSamplerFactory.class);
        when(samplerFactory.createBasicSpanSampler()).thenReturn(TrueSampler.instance());
        TailSampler tailSampler = Mockito.mock(TailSampler.class);

        @SuppressWarnings("unchecked")
        ObjectProvider<TailSampler> provider = Mockito.mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(tailSampler);

        SpanBo spanBo = new SpanBo();
        when(factory.buildSpanBo(any(), any(), org.mockito.ArgumentMatchers.anyLong())).thenReturn(spanBo);

        @SuppressWarnings("unchecked")
        ServerRequest<PSpan> request = Mockito.mock(ServerRequest.class);
        when(request.getData()).thenReturn(PSpan.getDefaultInstance());
        when(request.getHeader()).thenReturn(Mockito.mock(ServerHeader.class));
        when(request.getRequestTime()).thenReturn(123L);

        GrpcSpanHandler handler = new GrpcSpanHandler(
                new TraceService[]{traceService}, factory, samplerFactory, provider);
        handler.handleSimple(request);

        verify(tailSampler).acceptSpan(org.mockito.ArgumentMatchers.eq(spanBo), any(byte[].class));
        verify(traceService, never()).insertSpan(any()); // existing loop bypassed
    }
}
