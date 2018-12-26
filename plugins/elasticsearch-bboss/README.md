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


