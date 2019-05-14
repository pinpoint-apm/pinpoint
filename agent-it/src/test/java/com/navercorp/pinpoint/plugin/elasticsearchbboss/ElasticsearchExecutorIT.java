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


import com.navercorp.pinpoint.plugin.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic;
import pl.allegro.tech.embeddedelasticsearch.PopularProperties;

import static java.util.concurrent.TimeUnit.MINUTES;


/**
 * @author yinbp[yin-bp@163.com]
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@JvmVersion(7)
@Dependency({"com.bbossgroups.plugins:bboss-elasticsearch-rest-jdbc:5.6.9",
		"pl.allegro.tech:embedded-elasticsearch:2.7.0"})
public class ElasticsearchExecutorIT {
	private static EmbeddedElastic embeddedElastic;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// BBoss support elasticsearch 1.x,2.x,5.x,6.x,7.x,+
		// and we use elasticsearch 6.3.0 to test the Elasticsearch BBoss client plugin.

		// BBoss connect elasticsearch use localhost and http port 9200 default.

		embeddedElastic = EmbeddedElastic.builder()
				.withElasticVersion( "6.3.0")
				 .withSetting(PopularProperties.HTTP_PORT, 9200)
				 .withEsJavaOpts("-Xms128m -Xmx512m")
				 .withStartTimeout(2, MINUTES)
				.build()
				.start();

		//Build a elasticsearch client instance(Return a single instance but multithreaded security) with dsl config file elasticsearch/car-mapping.xml.
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("elasticsearch/car-mapping.xml");
		//create cars indice with car indexType by bboss
		// read indice mapping and setting with dsl name "createCarIndice" from configfile elasticsearch/car-mapping.xml
		clientUtil.createIndiceMapping("cars","createCarIndice");
	}

	@AfterClass
	public static void tearDownAfterClass() {
		if(embeddedElastic != null)
			embeddedElastic.stop();
	}
	@Test
	public void test(){
		try {
			//build a elasticsearch client instance(Return a single instance but multithreaded security).
			ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
			//Validate the indice twitter exist or not
			boolean existIndice = clientUtil.existIndice("twitter");
			Assert.assertEquals(existIndice,false);
			//Validate the indice cars exist or not
			existIndice = clientUtil.existIndice("cars");
			Assert.assertEquals(existIndice,true);
//			BaseApplicationContext.shutdown();
		}
		catch (Exception e){
			e.printStackTrace();
		}

//		PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
//		verifier.printCache();
//		verifier.verifyTraceCount(1);

	}
}
