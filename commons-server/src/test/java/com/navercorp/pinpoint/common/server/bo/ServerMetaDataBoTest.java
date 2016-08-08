/*
 * Copyright 2014 NAVER Corp.
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

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * @author hyungil.jeong
 */
public class ServerMetaDataBoTest {

    @Test
    public void testByteArrayConversion() {
        // Given
        final ServerMetaDataBo testBo = createTestBo("testServer", Arrays.asList("arg1", "arg2"), 
                Arrays.asList(ServiceInfoBoTest.createTestBo("testService", Arrays.asList("lib1", "lib2"))));
        // When
        final byte[] serializedBo = testBo.writeValue();
        final ServerMetaDataBo deserializedBo = new ServerMetaDataBo.Builder(serializedBo).build();
        // Then
        assertEquals(testBo, deserializedBo);
    }
    
    @Test
    public void testByteArrayConversionNullValues() {
        // Given
        final ServerMetaDataBo testBo = createTestBo(null, null, null);
        // When
        final byte[] serializedBo = testBo.writeValue();
        final ServerMetaDataBo deserializedBo = new ServerMetaDataBo.Builder(serializedBo).build();
        // Then
        assertEquals(testBo, deserializedBo);
    }
    
    static ServerMetaDataBo createTestBo(String serverInfo, List<String> vmArgs, List<ServiceInfoBo> serviceInfos) {
        final ServerMetaDataBo.Builder builder = new ServerMetaDataBo.Builder();
        builder.serverInfo(serverInfo);
        builder.vmArgs(vmArgs);
        builder.serviceInfos(serviceInfos);
        return builder.build();
    }

}
