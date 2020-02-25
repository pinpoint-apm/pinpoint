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
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.elasticsearch.entity.ESDatas;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
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


public abstract class ElasticsearchExecutorIT {
    private static EmbeddedElastic embeddedElastic;
    private static ClientInterface clientInterface;
    private static ClientInterface configRestClientInterface;
    private String serviceType = "ElasticsearchBBoss";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // BBoss support elasticsearch 1.x,2.x,5.x,6.x,7.x,+
        // and we use elasticsearch 6.3.0 to test the Elasticsearch BBoss client plugin.

        // BBoss connect elasticsearch use localhost and http port 9200 default.

//		Here is a bboss web demo base spring boot and elasticsearch 5.x,6.x,7.x,8.x:
//		https://github.com/bbossgroups/es_bboss_web
//
//		Here is a quickstart tutorial:
//		https://esdoc.bbossgroups.com/#/quickstart
        embeddedElastic = EmbeddedElastic.builder()
                .withElasticVersion("6.8.0")
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
        if (embeddedElastic != null) {
            embeddedElastic.stop();
        }
    }


    @Test
    public void indiceCreate() throws Exception {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        Class configClass = Class.forName("org.frameworkset.elasticsearch.client.ConfigRestClientUtil");
        Method createIndiceMappingMethod = configClass.getMethod("createIndiceMapping", String.class, String.class);
        try {
            configRestClientInterface.createIndiceMapping("cars", "createCarIndice");

        } catch (Exception e) {

        }

        verifier.verifyTrace(event(serviceType, createIndiceMappingMethod));


    }

    @Test
    public void indiceDrop() throws Exception {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        Class configClass = Class.forName("org.frameworkset.elasticsearch.client.ConfigRestClientUtil");
        Method dropIndiceMethod = configClass.getMethod("dropIndice", String.class);
        try {
            configRestClientInterface.dropIndice("cars");
        } catch (Exception e) {

        }

        verifier.verifyTrace(event(serviceType, dropIndiceMethod));


    }

    @Test
    public void indiceExist() throws Exception {

        //Validate the indice twitter exist or not
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        //Validate the indice cars exist or not
        boolean existIndice = clientInterface.existIndice("cars");

//		Assert.assertEquals(existIndice,true);
        Class restClientUtilClass = Class.forName("org.frameworkset.elasticsearch.client.RestClientUtil");
        Method existIndiceMethod = restClientUtilClass.getMethod("existIndice", String.class);
        verifier.verifyTrace(event(serviceType, existIndiceMethod));

    }

    @Test
    public void addDocument() throws Exception {
        Class restClientUtilClass = Class.forName("org.frameworkset.elasticsearch.client.RestClientUtil");
        Method addDocumentMethod = restClientUtilClass.getDeclaredMethod("addDocument", String.class, String.class,
                Object.class, String.class);
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        Car car = new Car();
        //set carid as the index documentid
        car.setCarId("1");
        car.setManufacturer("Volkswagenwerk");
        car.setModel("passat1.8T");
        car.setDescription("passat 2018");

        //add data to cars/car indice. Use force refresh when test case,but product mode does not use forcerefresh
        // and should use：
        // clientInterface.addDocument("cars",car).
        try {
            clientInterface.addDocument("cars", "car", car, "refresh=true");
        } catch (Exception e) {

        }

        verifier.verifyTrace(event(serviceType, addDocumentMethod));

    }

    @Test
    public void getDocument() throws Exception {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        //get car by document id "1"
        try {
            Car car = clientInterface.getDocument("cars", "1", Car.class);
        } catch (Exception e) {

        }


        Class restClientUtilClass = Class.forName("org.frameworkset.elasticsearch.client.RestClientUtil");
        Method getDocumentMethod = restClientUtilClass.getDeclaredMethod("getDocument", String.class,
                String.class, Class.class);
        verifier.verifyTrace(event(serviceType, getDocumentMethod));
    }

    @Test
    public void bulkDocuments() throws Exception {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
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
        // clientInterface.addDocuments("cars",cars);

        try {
            clientInterface.addDocuments("cars", "car", cars, "refresh=true");
        } catch (Exception e) {

        }
        Class restClientUtilClass = Class.forName("org.frameworkset.elasticsearch.client.RestClientUtil");
        Method addDocumentsMethod = restClientUtilClass.getDeclaredMethod("addDocuments", String.class, String.class,
                List.class, String.class);
        verifier.verifyTrace(event(serviceType, addDocumentsMethod));

    }


    @Test
    public void searchDocuments() throws Exception {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        try {
            Map<String, Object> condition = new HashMap<>();
            //set description as search condition
            condition.put("description", "passat");

            //search data to cars/car indice that match condition with a simple query dsl named testSearch defined in elasticsearchbboss/car-mapping.xml.
            ESDatas<Car> carESDatas = configRestClientInterface.searchList("cars/_search", "testSearch", condition, Car.class);
            //datas that match condition
            List<Car> cars = carESDatas.getDatas();
            //totalsize that match condition
            long totalSize = carESDatas.getTotalSize();
        } catch (Exception e) {

        }

        Class configClass = Class.forName("org.frameworkset.elasticsearch.client.ConfigRestClientUtil");
        Method searchListMethod = configClass.getDeclaredMethod("searchList", String.class,
                String.class, Map.class, Class.class);
        verifier.verifyTrace(event(serviceType, searchListMethod));
    }

    @Test
    public void updateDocument() throws Exception {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
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
        // clientInterface.updateDocument("cars","1",car);

        try {
            clientInterface.updateDocument("cars", "car", "1", car, "refresh=true");
        } catch (Exception e) {

        }
        Class restClientUtilClass = Class.forName("org.frameworkset.elasticsearch.client.RestClientUtil");
        Method updateDocumentMethod = restClientUtilClass.getDeclaredMethod("updateDocument", String.class, String.class,
                Object.class, Object.class, String.class);
        verifier.verifyTrace(event(serviceType, updateDocumentMethod));

    }

    @Test
    public void deleteDocument() throws Exception {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        // Delete data on cars/car indice that document id is 1.
        // Use force refresh when test case,but product mode does not use forcerefresh
        // and should use：
        // clientInterface.deleteDocument("cars","1");

        try {
            clientInterface.deleteDocument("cars", "car", "1", "refresh=true");
        } catch (Exception e) {

        }
        Class restClientUtilClass = Class.forName("org.frameworkset.elasticsearch.client.RestClientUtil");
        Method deleteDocument = restClientUtilClass.getDeclaredMethod("deleteDocument", String.class, String.class,
                String.class, String.class);
        verifier.verifyTrace(event(serviceType, deleteDocument));

    }

}