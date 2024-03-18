/*
 * Copyright 2024 NAVER Corp.
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
package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.id.ServiceId;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author youngjin.kim2
 */
public class ApplicationSelectorTest {

    @Test
    public void ApplicationSelectorSerdeTest() {
        UUID serviceIdValue = new UUID(0, 1);
        ApplicationSelector selector = new ApplicationSelector(ServiceId.of(serviceIdValue), "sampleApplication", ServiceType.SERVLET.getCode());
        Buffer buffer = new OffsetFixedBuffer(selector.toBytes());
        ApplicationSelector deserializedSelector = ApplicationSelector.fromBuffer(buffer);

        assertThat(deserializedSelector).isEqualTo(selector);
    }

}
