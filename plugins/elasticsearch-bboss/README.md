ElasticSearch is an Open Source (Apache 2), Distributed, RESTful, Search Engine built on top of Apache Lucene.

This agent plugin is worked with elasticsearch bboss highlevel rest client [BBoss][bs].

[BBoss][bs] is a best Java Highlevel Rest client for [ElasticSearch][es].

ElasticsearchBBoss Plugin支持两种工作模式：

独立应用模式：可以在java application中独立运行，上下文中没有root trace，自动产生一个root trace。

嵌入模式：如果上下文中有trace，则作为span event加入上下文trace。

可以运行[DocumentCRUDTest][DocumentCRUDTest]类的main方法验证独立工作模式


ElasticsearchBBoss Plugin support two working modes:

Independent application mode
It can run independently in Java application without root trace in the context, and automatically generate a root trace.

Embedded mode:
If there is trace in the context, add context trace as span event.

You can run the main method of the [DocumentCRUDTest][DocumentCRUDTest] class to verify the independent working mode.

Some work pictures of this plugin:
Applications in tomcat
![GitHub Logo](https://oscimg.oschina.net/oscnet/9c140814559b0c6123bf0e4f8cad51f22ab.jpg)
![GitHub Logo](https://oscimg.oschina.net/oscnet/eb1cf3aa824895b5a8c74a58c4aef438e6f.jpg)
![GitHub Logo](https://oscimg.oschina.net/oscnet/d2a7ea5c30c4ea22b74e78394cb696700ed.jpg)
![GitHub Logo](https://oscimg.oschina.net/oscnet/832bea5ef5064bf6db5544eb4bdc309290d.jpg)

Other Applications 
![GitHub Logo](https://oscimg.oschina.net/oscnet/071ce3018b10fe45136752f2a0ba470c88a.jpg)
![GitHub Logo](https://oscimg.oschina.net/oscnet/91d13619839e9f2d7613afe3e3df09d2cb9.jpg)
![GitHub Logo](https://oscimg.oschina.net/oscnet/3717ae9eb8b3cf6846dada06ee20601ab31.jpg)

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


 