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

import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;


/**
 * @author yinbp[yin-bp@163.com]
 */
public class ElasticsearchMetadataProvider implements TraceMetadataProvider {

    @Override
    public void setup(TraceMetadataSetupContext context) {

        if(context == null)
            return;
        context.addServiceType(ElasticsearchConstants.ELASTICSEARCH);
        context.addServiceType(ElasticsearchConstants.ELASTICSEARCH_EXECUTOR);

        context.addAnnotationKey(ElasticsearchConstants.ARGS_VERSION_ANNOTATION_KEY);//Elasticsearch version info
        context.addAnnotationKey(ElasticsearchConstants.ARGS_URL_ANNOTATION_KEY);//HTTP request URL parameters
        context.addAnnotationKey(ElasticsearchConstants.ARGS_DSL_ANNOTATION_KEY);//HTTP DSL body conent
        context.addAnnotationKey(ElasticsearchConstants.ARGS_ACTION_ANNOTATION_KEY);//HTTP Elasticsearch restful ACTION method
        context.addAnnotationKey(ElasticsearchConstants.ARGS_RESPONSEHANDLE_ANNOTATION_KEY);//HTTP Response handler class name，
        context.addAnnotationKey(ElasticsearchConstants.ARGS_ANNOTATION_KEY);//Elasticsearch client api arguments

    }

}
