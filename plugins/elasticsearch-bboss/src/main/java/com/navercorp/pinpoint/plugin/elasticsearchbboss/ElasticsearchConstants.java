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

import com.navercorp.pinpoint.common.trace.*;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.*;

/**
 * @author yinbp[yin-bp@163.com]
 */
public class ElasticsearchConstants {
	public static final AnnotationKey ARGS_ANNOTATION_KEY = AnnotationKeyFactory.of(171, "es.args", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
	public static final int maxDslSize = 50000;
	public static final ServiceType ELASTICSEARCH = ServiceTypeFactory.of(9201, "ElasticsearchBBoss");

	public static final AnnotationKey ARGS_URL_ANNOTATION_KEY = AnnotationKeyFactory.of(172, "es.url", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
	public static final AnnotationKey ARGS_DSL_ANNOTATION_KEY = AnnotationKeyFactory.of(173, "es.dsl", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
	public static final AnnotationKey ARGS_ACTION_ANNOTATION_KEY = AnnotationKeyFactory.of(174, "es.action", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
	public static final AnnotationKey ARGS_RESPONSEHANDLE_ANNOTATION_KEY = AnnotationKeyFactory.of(175, "es.responseHandle", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
	public static final AnnotationKey ARGS_VERSION_ANNOTATION_KEY = AnnotationKeyFactory.of(176, "es.version", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
	public static final ServiceType ELASTICSEARCH_EXECUTOR = ServiceTypeFactory.of(9202, "ElasticsearchBBossExecutor", ALIAS);
	public static final String ELASTICSEARCH_SCOPE = "ElasticsearchBBoss_SCOPE";
	public static final String ELASTICSEARCH_Parallel_SCOPE = "ElasticsearchBBoss_Parallel_SCOPE";
	public static final String ELASTICSEARCH_EXECUTOR_SCOPE = "ElasticsearchBBossExecutor_SCOPE";
	public static final String[] clazzInterceptors = new String[]{
			"org.frameworkset.elasticsearch.client.ConfigRestClientUtil",
			"org.frameworkset.elasticsearch.client.RestClientUtil"
	};
}
