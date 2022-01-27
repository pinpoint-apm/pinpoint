/*
 *  Copyright 2019 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.elasticsearch;

import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

/**
 * @author Roy Kim
 */
public class ElasticsearchMetadataProvider implements TraceMetadataProvider {

    @Override
    public void setup(TraceMetadataSetupContext context) {

        if (context == null) {
            return;
        }
        context.addServiceType(ElasticsearchConstants.ELASTICSEARCH);
        context.addServiceType(ElasticsearchConstants.ELASTICSEARCH_EXECUTOR);
        context.addServiceType(ElasticsearchConstants.ELASTICSEARCH_EEST);

        context.addAnnotationKey(ElasticsearchConstants.ARGS_VERSION_ANNOTATION_KEY);//Elasticsearch version info
        context.addAnnotationKey(ElasticsearchConstants.ARGS_DSL_ANNOTATION_KEY);//HTTP DSL body conent
        context.addAnnotationKey(ElasticsearchConstants.INDEX_KEY);
        context.addAnnotationKey(ElasticsearchConstants.INDEX_KEY_METHOD);
    }

}
