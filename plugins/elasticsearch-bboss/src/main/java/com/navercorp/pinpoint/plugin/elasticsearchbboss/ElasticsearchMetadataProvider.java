/*
 * Copyright 2015 NAVER Corp.
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
//        context.addServiceType(ElasticsearchConstants.ELASTICSEARCH_EVENT);
        context.addServiceType(ElasticsearchConstants.ELASTICSEARCH_EXECUTOR);//ElasticSearch版本信息，和方法名称在同一行

		/**
		 * ,
		 *                 AnnotationKeyMatchers.exact(ElasticsearchConstants.ARGS_VERSION_ANNOTATION_KEY)
		 */
//        context.addAnnotationKey(AnnotationKeyMatchers.exact(AnnotationKey.ARGS0));
//        context.addServiceType(ElasticsearchConstants.ELASTICSEARCH_EXECUTOR);
        //context.addServiceType(ELASTICSEARCH_EXECUTOR, AnnotationKeyMatchers.exact(AnnotationKey.ARGS0));//参数和方法名称在同一行
        context.addAnnotationKey(ElasticsearchConstants.ARGS_VERSION_ANNOTATION_KEY);//HTTP请求URL参数，在新的一行展示
        context.addAnnotationKey(ElasticsearchConstants.ARGS_URL_ANNOTATION_KEY);//HTTP请求URL参数，在新的一行展示
        context.addAnnotationKey(ElasticsearchConstants.ARGS_DSL_ANNOTATION_KEY);//HTTP请求DSL参数，在新的一行展示
        context.addAnnotationKey(ElasticsearchConstants.ARGS_ACTION_ANNOTATION_KEY);//HTTP请求ACTION参数，在新的一行展示
        context.addAnnotationKey(ElasticsearchConstants.ARGS_RESPONSEHANDLE_ANNOTATION_KEY);//HTTP请求Response处理器参数，
        context.addAnnotationKey(ElasticsearchConstants.ARGS_ANNOTATION_KEY);//HTTP请求URL参数，在新的一行展示

    }

}
