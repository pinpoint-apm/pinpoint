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

To prevent duplicate recording of data by httpclient4 ，The follow two methods have been added to [Trace ](https://github.com/yin-bp/pinpoint/blob/master/bootstrap-core/src/main/java/com/navercorp/pinpoint/bootstrap/context/Trace.java) interface:
```java
/**
     * Pause the trace sampled.
     * In some scenarios, it is necessary to pause the plug-in interception work related to the method internal logic.
     * When the method internal logic is executed, the after interception work of this method is continued. For example,
     * ElasticSearch Bboss plugin as a terminal plugin needs to shield the underlying HTTP protocol plugin
     */
    void pauseSampled();
    /**
     * Continue the paused trace sampled.
     * In some scenarios, it is necessary to pause the plug-in interception work related to the method internal logic.
     * When the method internal logic is executed, the after interception work of this method is continued. For example,
     * ElasticSearch Bboss plugin as a terminal plugin needs to shield the underlying HTTP protocol plugin
     */
    void resumeSampled();
```

pauseSampled/resumeSampled have been used in [ElasticsearchExecutorOperationInterceptor](https://github.com/yin-bp/pinpoint/blob/master/plugins/elasticsearch-bboss/src/main/java/com/navercorp/pinpoint/plugin/elasticsearchbboss/interceptor/ElasticsearchExecutorOperationInterceptor.java)::

```java
 @Override
    public void before(Object target, Object[] args) {
        super.before(target,args);
        Trace currentTrace = traceContext.currentTraceObject();
        if(currentTrace != null ){

            currentTrace.pauseSampled();
        }
    }
    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable)     {

        Trace currentTrace = traceContext.currentRawTraceObject();
        if(currentTrace != null){

            currentTrace.resumeSampled();
        }
        super.after(target,args,result,throwable);
    }
```

How to use Elasticsearch BBoss.

First add the maven dependency of BBoss to your pom.xml:

```xml
       <dependency>
            <groupId>com.bbossgroups.plugins</groupId>
            <artifactId>bboss-elasticsearch-rest-jdbc</artifactId>
            <version>5.2.7</version>
        </dependency>
```

If it's a spring boot project, you can replace the Maven coordinates above with the following Maven coordinates:

```xml
        <dependency>
            <groupId>com.bbossgroups.plugins</groupId>
            <artifactId>bboss-elasticsearch-spring-boot-starter</artifactId>
            <version>5.2.7</version>
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
<%@ page import="com.frameworkset.common.poolman.SQLExecutor" %>
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
	long count = clientUtil.countAll("twitter");
	out.println(count);

	//Get All documents of indice twitter,DEFAULT_FETCHSIZE is 5000
	ESDatas<Map> esDatas = clientUtil.searchAll("twitter", Map.class);

	//Get All documents of indice twitter,Set fetchsize to 10000, Using ScrollHandler to process each batch of datas.
	clientUtil.searchAll("twitter",10000,new ScrollHandler<Map>() {
		public void handle(ESDatas<Map> esDatas) throws Exception {
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
			SQLExecutor.queryList(Map.class,"select * from td_sm_user");
		}
	},Map.class);
    //Use slice parallel scoll query all documents of indice  twitter by 2 thread tasks. DEFAULT_FETCHSIZE is 5000
	//You can also use ScrollHandler to process each batch of datas on your own.
	clientUtil.searchAllParallel("twitter", Map.class,2);
	out.println("searchAllParallel:ok");
%>

```

Put the file into the web project that has been connected to pinpoint, run the program, log on pinpoint to see the execution effect of bboss plugin.

For more BBoss Elasticsearch Highlevel Rest Client detail see :
 https://www.oschina.net/p/bboss-elastic

github sourcecode:
https://github.com/bbossgroups/bboss-elastic 

Fast import bboss to your project:
1. For Spring boot project see
https://my.oschina.net/bboss/blog/1835601
2. For normal maven or gradle project see
https://my.oschina.net/bboss/blog/1801273 

ElasticsearchBBoss Demo

[eshelloworld-booter][booter]

[eshelloword-spring-boot-starter][springbooter]

[booter]: https://github.com/bbossgroups/eshelloword-booter
[springbooter]: https://github.com/bbossgroups/eshelloword-spring-boot-starter

[bs]: https://github.com/bbossgroups/bboss-elastic
[es]: http://www.elasticsearch.org
[DocumentCRUDTest]: https://github.com/bbossgroups/eshelloword-booter/blob/master/src/test/java/org/bboss/elasticsearchtest/crud/DocumentCRUDTest.java


