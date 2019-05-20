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
import com.navercorp.pinpoint.plugin.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.elasticsearch.entity.ESDatas;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic;
import pl.allegro.tech.embeddedelasticsearch.PopularProperties;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;
import static java.util.concurrent.TimeUnit.MINUTES;


/**
 * @author yinbp[yin-bp@163.com]
 */

@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@Dependency({"com.bbossgroups.plugins:bboss-elasticsearch-rest-jdbc:[5.6.9,]",
		"pl.allegro.tech:embedded-elasticsearch:2.8.0"})
@JvmVersion(8)
public class ElasticsearchExecutorIT {
	private static EmbeddedElastic embeddedElastic;
	private static ClientInterface clientInterface;
	private static ClientInterface configRestClientInterface ;
	private String serviceType = "ElasticsearchBBoss";
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// BBoss support elasticsearch 1.x,2.x,5.x,6.x,7.x,+
		// and we use elasticsearch 6.3.0 to test the Elasticsearch BBoss client plugin.

		// BBoss connect elasticsearch use localhost and http port 9200 default.

//		Here is a bboss web demo base spring boot and elasticsearch 5.x,6.x:
//		https://github.com/bbossgroups/es_bboss_web
//
//		Here is a quickstart tutorial:
//		https://esdoc.bbossgroups.com/#/quickstart
		embeddedElastic = EmbeddedElastic.builder()
				.withElasticVersion( "5.0.0")
				 .withSetting(PopularProperties.HTTP_PORT, 9200)
				 .withEsJavaOpts("-Xms128m -Xmx512m")
				 .withStartTimeout(2, MINUTES)
				.build()
				.start();

		//Build a elasticsearch client instance(Return a single instance but multithreaded security) with dsl config file elasticsearchbboss/car-mapping.xml.
		configRestClientInterface = ElasticSearchHelper.getConfigRestClientUtil("elasticsearchbboss/car-mapping.xml");
		// Create an elasticsearch client interface instance with a specific Elasticserch datasource name  and with dsl config file elasticsearchbboss/car-mapping.xml.
		//configRestClientInterface = ElasticSearchHelper.getConfigRestClientUtil("esdatasourceName","elasticsearchbboss/car-mapping.xml");

		//build a elasticsearch client instance(Return a single instance but multithreaded security) for do not need dsl or direct dsl operations.
		clientInterface = ElasticSearchHelper.getRestClientUtil();
		// Create an elasticsearch client interface instance with a specific Elasticserch datasource name
		//clientInterface = ElasticSearchHelper.getRestClientUtil("esdatasourceName");

		// A multidatasource spring boot demo: https://github.com/bbossgroups/es_bboss_web/tree/multiesdatasource
	}

	@AfterClass
	public static void tearDownAfterClass() {
		if(embeddedElastic != null)
			embeddedElastic.stop();
	}
	@Test
	public void testClientInterface() throws Exception {
		PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
		indiceCreate(verifier);
		indiceExist(  verifier);
		add2ndGetDocument(verifier);
		bulkDocuments(verifier);
		searchDocuments(verifier);
		updateAndDeleteDocument(verifier);
	}
	public void indiceCreate(PluginTestVerifier verifier) throws Exception {
		boolean existIndice = configRestClientInterface.existIndice("cars");
		Method createIndiceMappingMethod = configRestClientInterface.getClass().getDeclaredMethod("createIndiceMapping",String.class,String.class);

		if(!existIndice){
			// Create cars indice with car indexType by bboss
			// Get indice mappings and settings with dsl name "createCarIndice" from configfile elasticsearchbboss/car-mapping.xml
			configRestClientInterface.createIndiceMapping("cars","createCarIndice");
			verifier.verifyTrace(event(serviceType, createIndiceMappingMethod));
		}
		else{
			//drop and create indice
			configRestClientInterface.dropIndice("cars");
			configRestClientInterface.createIndiceMapping("cars","createCarIndice");
			Method dropIndiceMethod = configRestClientInterface.getClass().getDeclaredMethod("dropIndice",String.class);
			verifier.verifyTrace(event(serviceType, dropIndiceMethod));
			verifier.verifyTrace(event(serviceType, createIndiceMappingMethod));
		}

	}
	public void indiceExist(PluginTestVerifier verifier) throws Exception{

		//Validate the indice twitter exist or not

		boolean existIndice = clientInterface.existIndice("twitter");

		//TODO just a sample
		Assert.assertEquals(existIndice,false);
		//Validate the indice cars exist or not
		existIndice = clientInterface.existIndice("cars");
		Assert.assertEquals(existIndice,true);
		Method existIndiceMethod = clientInterface.getClass().getDeclaredMethod("existIndice",String.class);
		verifier.verifyTrace(event(serviceType, existIndiceMethod));

	}


	public void add2ndGetDocument(PluginTestVerifier verifier) throws Exception{
		Car car = new Car();
		//set carid as the index documentid
		car.setCarId("1");
		car.setManufacturer("Volkswagenwerk");
		car.setModel("passat1.8T");
		car.setDescription("passat 2018");

		//add data to cars/car indice. Use force refresh when test case,but product mode does not use forcerefresh
		// and should use：
		// clientInterface.addDocument("cars","car",car).
		clientInterface.addDocument("cars","car",car,"refresh=true");
		//get car by document id "1"
		car = clientInterface.getDocument("cars","car","1",Car.class);
		Assert.assertNotNull(car);
		Assert.assertEquals("1",car.getCarId());
		Method addDocumentMethod = clientInterface.getClass().getDeclaredMethod("addDocument",String.class,
				String.class,Object.class,String.class);
		verifier.verifyTrace(event(serviceType, addDocumentMethod));
		Method getDocumentMethod = clientInterface.getClass().getDeclaredMethod("getDocument",String.class,
				String.class,String.class,Class.class);
		verifier.verifyTrace(event(serviceType, getDocumentMethod));
	}

	public void bulkDocuments(PluginTestVerifier verifier)throws Exception{
		List<Car> cars = new ArrayList<Car>();
		Car car = new Car();
		//set carid as the index documentid
		car.setCarId("2");
		car.setManufacturer("Volkswagenwerk");
		car.setModel("passat1.8T");
		car.setDescription("passat 2018");
		cars.add(car);
		car = new Car();
		//set carid as the index documentid
		car.setCarId("3");
		car.setManufacturer("Volkswagenwerk");
		car.setModel("2.0T");
		car.setDescription("passat 2019");
		cars.add(car);
		//add datas to cars/car indice.Use force refresh when test case,but product mode does not use forcerefresh
		// and should use：
		// clientInterface.addDocuments("cars","car",cars);
		clientInterface.addDocuments("cars","car",cars,"refresh=true");
		//get car by document id "2"
		car = clientInterface.getDocument("cars","car","2",Car.class);
		Assert.assertNotNull(car);
		Assert.assertEquals("2",car.getCarId());
		Method addDocumentsMethod = clientInterface.getClass().getDeclaredMethod("addDocuments",String.class,
				String.class,List.class,String.class);
		verifier.verifyTrace(event(serviceType, addDocumentsMethod));
		Method getDocumentMethod = clientInterface.getClass().getDeclaredMethod("getDocument",String.class,
				String.class,String.class,Class.class);
		verifier.verifyTrace(event(serviceType, getDocumentMethod));
	}



	public void searchDocuments(PluginTestVerifier verifier)throws Exception{
		Map<String,Object> condition = new HashMap<>();
		//set description as search condition
		condition.put("description","passat");

		//search data to cars/car indice that match condition with a simple query dsl named testSearch defined in elasticsearchbboss/car-mapping.xml.
		ESDatas<Car> carESDatas = configRestClientInterface.searchList("cars/_search","testSearch",condition,Car.class);
		//datas that match condition
		List<Car> cars = carESDatas.getDatas();
		//totalsize that match condition
		long totalSize = carESDatas.getTotalSize();
		Assert.assertTrue(totalSize > 0);
		Method searchListMethod = configRestClientInterface.getClass().getDeclaredMethod("searchList",String.class,
				String.class,Map.class,Class.class);
		verifier.verifyTrace(event(serviceType, searchListMethod));
	}

	public void updateAndDeleteDocument(PluginTestVerifier verifier)throws Exception{
		Car car = new Car();
		//set carid as the index documentid
		car.setCarId("1");
		car.setManufacturer("Volkswagenwerk");
		// Update model from 1.4T to 1.0T
		car.setModel("1.0T");
		car.setDescription("passat 2017");
		// Update data on cars/car indice that document id is 1.
		// Use force refresh when test case,but product mode does not use forcerefresh
		// and should use：
		// clientInterface.updateDocument("cars","car","1",car);
		clientInterface.updateDocument("cars","car","1",car,"refresh=true");
		// Get modified car
		car = clientInterface.getDocument("cars","car","1",Car.class);
		Assert.assertEquals("1.0T",car.getModel());
		// Delete data on cars/car indice that document id is 1.
		// Use force refresh when test case,but product mode does not use forcerefresh
		// and should use：
		// clientInterface.deleteDocument("cars","car","1");
		clientInterface.deleteDocument("cars","car","1","refresh=true");
		// Get delete car. will get a null object.
		car = clientInterface.getDocument("cars","car","1",Car.class);
		Assert.assertNull(car);
		Method updateDocumentMethod = clientInterface.getClass().getDeclaredMethod("updateDocument",String.class,
				String.class,Object.class,Object.class,String.class);
		verifier.verifyTrace(event(serviceType, updateDocumentMethod));
		Method getDocumentMethod = clientInterface.getClass().getDeclaredMethod("getDocument",String.class,
				String.class,String.class,Class.class);
		verifier.verifyTrace(event(serviceType, getDocumentMethod));
		Method deleteDocumentMethod = clientInterface.getClass().getDeclaredMethod("deleteDocument",String.class,
				String.class,String.class,String.class);
		verifier.verifyTrace(event(serviceType, deleteDocumentMethod));
	}

}
