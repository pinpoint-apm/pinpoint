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


import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * @author yinbp[yin-bp@163.com]
 */
@RunWith(PinpointPluginTestSuite.class)
@Dependency({"com.bbossgroups.plugins:bboss-elasticsearch-rest-jdbc:5.3.6"})
public class ElasticsearchExecutorIT {
	@Test
	public void test(){
		try {
			//build a elasticsearch client instance.
			ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
			//use the elasticsearch client instance to validate the indice exist or not
			boolean existIndice = clientUtil.existIndice("twitter");
//			BaseApplicationContext.shutdown();
		}
		catch (Exception e){

		}

		PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
		verifier.printCache();
		verifier.verifyTraceCount(1);

	}
}
