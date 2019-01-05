package com.navercorp.pinpoint.plugin.elasticsearchbboss;
/**
 * Copyright 2008 biaoping.yin
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.navercorp.pinpoint.common.trace.*;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.TERMINAL;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/1/5 22:48
 * @author biaoping.yin
 * @version 1.0
 */
public class ElasticsearchConstants {
	public static final AnnotationKey ARGS_ANNOTATION_KEY = AnnotationKeyFactory.of(971, "es.args", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
	public static int maxDslSize = 50000;
	public static final ServiceType ELASTICSEARCH = ServiceTypeFactory.of(8804, "ElasticsearchBBoss");

	public static final AnnotationKey ARGS_URL_ANNOTATION_KEY = AnnotationKeyFactory.of(972, "es.url", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
	public static final AnnotationKey ARGS_DSL_ANNOTATION_KEY = AnnotationKeyFactory.of(973, "es.dsl", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
	public static final AnnotationKey ARGS_ACTION_ANNOTATION_KEY = AnnotationKeyFactory.of(974, "es.action", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
	public static final AnnotationKey ARGS_RESPONSEHANDLE_ANNOTATION_KEY = AnnotationKeyFactory.of(975, "es.responseHandle", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
	public static final AnnotationKey ARGS_VERSION_ANNOTATION_KEY = AnnotationKeyFactory.of(976, "es.version", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
	public static final ServiceType ELASTICSEARCH_EXECUTOR = ServiceTypeFactory.of(8805, "ElasticsearchBBossExecutor", TERMINAL, RECORD_STATISTICS);
	public static final String ELASTICSEARCH_SCOPE = "ElasticsearchBBoss_SCOPE";
	public static final String ELASTICSEARCH_SLICE_SCOPE = "ElasticsearchBBoss_SLICE_SCOPE";
	public static final String ELASTICSEARCH_EXECUTOR_SCOPE = "ElasticsearchBBossExecutor_SCOPE";
	public static final String[] clazzInterceptors = new String[]{
			"org.frameworkset.elasticsearch.client.ConfigRestClientUtil",
			"org.frameworkset.elasticsearch.client.RestClientUtil"
	};
}
