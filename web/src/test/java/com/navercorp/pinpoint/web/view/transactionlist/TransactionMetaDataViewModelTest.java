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
package com.navercorp.pinpoint.web.view.transactionlist;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanOwner;
import com.navercorp.pinpoint.common.server.bo.TraceSourceType;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TransactionMetaDataViewModelTest {

    @Test
    public void applicationNameAndServiceTypeName() {
        SpanOwner owner = new SpanOwner();
        owner.setApplicationName("test-app");
        SpanBo span = new SpanBo(TraceSourceType.OPENTELEMETRY, owner);
        span.setApplicationServiceType(ServiceType.OPENTELEMETRY_SERVER.getCode());
        span.setRpc("/order/submit");

        TransactionMetaDataViewModel viewModel = new TransactionMetaDataViewModel(List.of(span),
                code -> code == ServiceType.OPENTELEMETRY_SERVER.getCode() ? ServiceType.OPENTELEMETRY_SERVER.getName() : null);

        List<TransactionMetaDataViewModel.MetaData> metadata = viewModel.getMetadata();
        assertThat(metadata).hasSize(1);
        TransactionMetaDataViewModel.MetaData metaData = metadata.get(0);
        assertThat(metaData.getApplicationName()).isEqualTo("test-app");
        assertThat(metaData.getServiceTypeName()).isEqualTo(ServiceType.OPENTELEMETRY_SERVER.getName());
        // "application" stays rpc for compatibility with existing consumers
        assertThat(metaData.getApplication()).isEqualTo("/order/submit");
    }

    @Test
    public void unknownServiceTypeCodeResolvesToNull() {
        SpanOwner owner = new SpanOwner();
        owner.setApplicationName("test-app");
        SpanBo span = new SpanBo(TraceSourceType.OPENTELEMETRY, owner);
        span.setApplicationServiceType(-1);

        TransactionMetaDataViewModel viewModel = new TransactionMetaDataViewModel(List.of(span), code -> null);

        assertThat(viewModel.getMetadata().get(0).getServiceTypeName()).isNull();
    }

    @Test
    public void emptyViewModel() {
        TransactionMetaDataViewModel viewModel = new TransactionMetaDataViewModel();
        assertThat(viewModel.getMetadata()).isEmpty();
    }
}
