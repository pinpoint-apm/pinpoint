ElasticSearch is an Open Source (Apache 2), Distributed, RESTful, Search Engine built on top of Apache Lucene.

This agent plugin is worked with elasticsearch bboss highlevel rest client [BBoss][bs].

[BBoss][bs] is a best Java Highlevel Rest client for [ElasticSearch][es].


If there is trace in the context, add context trace as span event.

Some work pictures of this plugin:
Elasticsearch Bboss work in tomcat
![GitHub Logo](https://oscimg.oschina.net/oscnet/a6aa8b7e84db0437dd6cbff88bdf1160fab.jpg)
![GitHub Logo](https://oscimg.oschina.net/oscnet/9665c0376579bbf1ca6093c1a0cf11c6c45.jpg)
Async parallel slice scoll query
![GitHub Logo](https://oscimg.oschina.net/oscnet/2ad63bcb0ad2de30a2cc13aa5f8a8ea86b4.jpg)
Simple indice and indice type exist query
![GitHub Logo](https://oscimg.oschina.net/oscnet/4c2e63e159786c28909ca2a003c8ee28432.jpg)
dsl query
![GitHub Logo](https://oscimg.oschina.net/oscnet/90fe224aee8b52c50b22fdfe0860658324d.jpg)
 

How to use Elasticsearch BBoss.

First add the maven dependency of BBoss to your pom.xml:

```xml
       <dependency>
            <groupId>com.bbossgroups.plugins</groupId>
            <artifactId>bboss-elasticsearch-rest-jdbc</artifactId>
            <version>5.6.9</version>
        </dependency>
```

If it's a spring boot project, you can replace the Maven coordinates above with the following Maven coordinates:

```xml
        <dependency>
            <groupId>com.bbossgroups.plugins</groupId>
            <artifactId>bboss-elasticsearch-spring-boot-starter</artifactId>
            <version>5.6.9</version>
        </dependency>
```



Next, add the Elasticsearch addresses to the application.properties file under the project resource directory, and create a new one if the file does not exist:

```properties
elasticsearch.rest.hostNames=10.21.20.168:9200

#Cluster addresses are separated by commas

#elasticsearch.rest.hostNames=10.180.211.27:9200,10.180.211.28:9200,10.180.211.29:9200
```



And last  create a jsp file named testElasticsearch.jsp :

```jsp
<%@ page import="org.frameworkset.elasticsearch.ElasticSearchHelper" %>
<%@ page import="org.frameworkset.elasticsearch.client.ClientInterface" %>
<%@ page import="org.frameworkset.elasticsearch.entity.ESDatas" %>
<%@ page import="org.frameworkset.elasticsearch.scroll.ScrollHandler" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.frameworkset.elasticsearch.scroll.HandlerInfo" %>
<%@ page language="java" pageEncoding="UTF-8"%>

<%
	ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
	//get elasticsearch cluster state
	String result = clientUtil.executeHttp("_cluster/state?pretty",ClientInterface.HTTP_GET);

	//check indice twitter and index type tweet exist or not.
	boolean exist1 = clientUtil.existIndiceType("twitter","tweet");
	out.println("twitter  tweet type exist:"+exist1);
	//check indice twitter exist or not
	exist1 = clientUtil.existIndice("twitter");
	out.println("twitter exist:"+exist1);
	//count documents in indice twitter
	long count = clientUtil.countAll("demo");
	out.println(count);

	//Get All documents of indice twitter,DEFAULT_FETCHSIZE is 5000
	ESDatas<Map> esDatas = clientUtil.searchAll("demo", Map.class);

	//Get All documents of indice twitter,Set fetchsize to 10000, Using ScrollHandler to process each batch of datas.
	clientUtil.searchAll("demo",10000,new ScrollHandler<Map>() {
		public void handle(ESDatas<Map> esDatas, HandlerInfo handlerInfo) throws Exception {
			List<Map> dataList = esDatas.getDatas();
			System.out.println("TotalSize:"+esDatas.getTotalSize());
			if(dataList != null) {
				System.out.println("dataList.size:" + dataList.size());
			}
			else
			{
				System.out.println("dataList.size:0");
			}
			//do something other such as do a db query.
			//SQLExecutor.queryList(Map.class,"select * from td_sm_user");
		}
	},Map.class);
    //Use slice parallel scoll query all documents of indice  twitter by 2 thread tasks. DEFAULT_FETCHSIZE is 5000
	//You can also use ScrollHandler to process each batch of datas on your own.
	clientUtil.searchAllParallel("demo", Map.class,2);
	//use query dsl to execute scroll search and use ScrollHandler to parallel handle each page scroll search result.
	clientUtil.scrollParallel("demo/_search","{ \"size\":10,\"query\": {\"match_all\": {}},\"sort\": [\"_doc\"]}","1m",Map.class,new ScrollHandler<Map>() {
		public void handle(ESDatas<Map> esDatas, HandlerInfo handlerInfo) throws Exception {
			List<Map> dataList = esDatas.getDatas();
			System.out.println("TotalSize:"+esDatas.getTotalSize());
			if(dataList != null) {
				System.out.println("dataList.size:" + dataList.size());
			}
			else
			{
				System.out.println("dataList.size:0");
			}
			//do something other such as do a db query.
			//SQLExecutor.queryList(Map.class,"select * from td_sm_user");
		}
	});
	out.println("searchAllParallel:ok");
%>

```

Put the file into the web project that has been connected to pinpoint, run the program, log on pinpoint to see the execution effect of bboss plugin.

For more BBoss Elasticsearch Highlevel Rest Client detail see :
 https://esdoc.bbossgroups.com/#/README

github sourcecode:
https://github.com/bbossgroups/bboss-elasticsearch

Fast import bboss to your project:
1. For Spring boot project see
https://esdoc.bbossgroups.com/#/spring-booter-with-bboss
2. For normal maven or gradle project see
https://esdoc.bbossgroups.com/#/common-project-with-bboss

ElasticsearchBBoss Demo

[eshelloworld-booter][booter]

[eshelloword-spring-boot-starter][springbooter]

[booter]: https://github.com/bbossgroups/elasticsearch-example
[springbooter]: https://github.com/bbossgroups/elasticsearch-springboot-example

[bs]: https://github.com/bbossgroups/bboss-elasticsearch
[es]: http://www.elasticsearch.org
[DocumentCRUDTest]: https://github.com/bbossgroups/elasticsearch-example/blob/master/src/test/java/org/bboss/elasticsearchtest/crud/DocumentCRUDTest.java


