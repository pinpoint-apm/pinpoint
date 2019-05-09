/*
 *  Copyright 2018 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.navercorp.pinpoint.plugin.elasticsearchbboss;

import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;
import org.junit.Test;

import static org.mockito.Mockito.mock;


/**
 * @author yinbp[yin-bp@163.com]
 */
public class ElasticsearchMetadataProviderTest{

    @Test
    public void setup() {
        ElasticsearchMetadataProvider elasticsearchMetadataProvider = new ElasticsearchMetadataProvider();
        TraceMetadataSetupContext traceMetadataSetupContext = mock( TraceMetadataSetupContext.class);
        elasticsearchMetadataProvider.setup(traceMetadataSetupContext);
        
    }

}
