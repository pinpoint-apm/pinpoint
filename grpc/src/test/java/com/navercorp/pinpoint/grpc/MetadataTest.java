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

package com.navercorp.pinpoint.grpc;

import com.navercorp.pinpoint.common.util.BytesUtils;
import io.grpc.InternalMetadata;
import io.grpc.Metadata;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataTest {
    private static final Logger logger = LoggerFactory.getLogger(ChannelFactoryTest.class);

    @Test
    public void metadataTest() {
        Metadata.Key<String> dd = Metadata.Key.of("key", Metadata.ASCII_STRING_MARSHALLER);
        Metadata metadata = InternalMetadata.newMetadata(BytesUtils.toBytes("key"), BytesUtils.toBytes("value"));

        Iterable<String> remove1 = metadata.removeAll(dd);
        logger.debug("{}", remove1);

        Iterable<String> remove2 = metadata.removeAll(dd);
        logger.debug("{}", remove2);
    }
}
