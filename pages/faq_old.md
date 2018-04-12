---
title: FAQ
sidebar: mydoc_sidebar
tags:
keywords: faq, question, answer, frequently asked questions, FAQ, question and answer
last_updated: Feb 1, 2018
permalink: faq_old.html
toc: false
disqus: false
---

For any other questions, please use the [user group](https://groups.google.com/forum/#!forum/pinpoint_user)

<div class="panel-group" id="accordion">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#collapseOne">How do I get the call stack view?</a>
                            </h4>
                        </div>
                        <div id="collapseOne" class="panel-collapse collapse noCrossRef">
                            <div class="panel-body">
                                <p>Click on a server node, which will populate the scatter chart on the right. This chart shows all succeeded/failed requests that went through the server. If there are any requests that spike your interest, simply <strong>drag on the scatter chart</strong> to select them. This will bring up the call stack view containing the requests you've selected.</p>
                            </div>
                        </div>
                    </div>
                    <!-- /.panel -->
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#collapseTwo">How do I change agent's log level?</a>
                            </h4>
                        </div>
                        <div id="collapseTwo" class="panel-collapse collapse noCrossRef">
                            <div class="panel-body">
                                <p>You can change the log level by modifying the agent's <em>log4j.xml</em> located in <em>PINPOINT_AGENT/lib</em> directory.</p>
                            </div>
                        </div>
                    </div>
                    <!-- /.panel -->
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#collapseThree">Request count in the Scatter Chart is different from the ones in Response Summary chart. Why is this?</a>
                            </h4>
                        </div>
                        <div id="collapseThree" class="panel-collapse collapse noCrossRef">
                            <div class="panel-body">
                                <p>The Scatter Chart data have a second granularity, so the requests counted here can be differentiated by a second interval.
                                On the other hand, the Server Map, Response Summary, and Load Chart data are stored in a minute granularity (the Collector aggregates these in memory and flushes them every minute due to performance reasons).
                                For example, if the data is queried from 10:00:30 to 10:05:30, the Scatter Chart will show the requests counted between 10:00:30 and 10:05:30, whereas the server map, response summary, and load chart will show the requests counted between 10:00:00 and 10:05:59.</p>
                            </div>
                        </div>
                    </div>
                    <!-- /.panel -->
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#collapseFour">How do I delete application name and/or agent id from HBase?</a>
                            </h4>
                        </div>
                        <div id="collapseFour" class="panel-collapse collapse">
                            <div class="panel-body">
                                <p>Application names and agent ids, once registered, stay in HBase until their TTL expires (default 1year).
                                You may however delete them proactively using <a href="../blob/master/web/src/main/java/com/navercorp/pinpoint/web/controller/AdminController.java">admin APIs</a> once they are no longer used.</p>
                                <ul>
                                <li>Remove application name - <code>/admin/removeApplicationName.pinpoint?applicationName=$APPLICATION_NAME&amp;password=$PASSWORD</code>
                                </li>
                                <li>Remove agent id - <code>/admin/removeAgentId.pinpoint?applicationName=$APPLICATION_NAME&amp;agentId=$AGENT_ID&amp;password=$PASSWORD</code>
                                Note that the value for the password parameter is what you defined <code>admin.password</code> property in <em>pinpoint-web.properties</em>. Leaving this blank will allow you to call admin APIs without the password parameter.</li>
                                </ul>       
                            </div>
                        </div>
                    </div>
                    <!-- /.panel -->
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#collapseFive">HBase is taking up too much space, which data should I delete first?</a>
                            </h4>
                        </div>
                        <div id="collapseFive" class="panel-collapse collapse">
                            <div class="panel-body">
                                <p>Hbase is very scalable so you can always add more region servers if you're running out of space. Shortening the TTL values, especially for <strong>AgentStatV2</strong> and <strong>TraceV2</strong>, can also help (though you might have to wait for a major compaction before space is reclaimed).</p>
                                <p>However, if you <strong>must</strong> make space asap, data in <strong>AgentStatV2</strong> and <strong>TraceV2</strong> tables are probably the safest to delete. You will lose agent statistic data (inspector view) and call stack data (transaction view), but deleting these will not break anything.</p>
                                <p>Note that deleting *<strong>MetaData</strong> tables will result in *<em>-METADATA-NOT-FOUND</em> being displayed in the call stack and the only way to "fix" this is to restart all the agents, so it is generally a good idea to leave these tables alone.</p>
                            </div>
                        </div>
                    </div>
                    <!-- /.panel -->
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#collapseSix">My custom jar application is not being traced. Help!</a>
                            </h4>
                        </div>
                        <div id="collapseSix" class="panel-collapse collapse">
                            <div class="panel-body">
                                <p>Pinpoint Agent need an entry point to start off a new trace for a transaction. This is usually done by various WAS plugins (such as Tomcat, Jetty, etc) in which a new trace is started when they receive an RPC request.
                                For custom jar applications, you need to set this manually as the Agent does not have knowledge of when and where to start a trace.
                                You can set this by configuring <code>profiler.entrypoint</code> in <em>pinpoint.config</em> file.</p>
                            </div>
                        </div>
                    </div>
                    <!-- /.panel -->
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#collapseSeven">Building is failing after new release. Help!</a>
                            </h4>
                        </div>
                        <div id="collapseSeven" class="panel-collapse collapse">
                            <div class="panel-body">
                                <p>Please remember to run the command <code>mvn clean verify -Dmaven.test.skip=true</code> if you've used a previous version before.</p>
                            </div>
                        </div>
                    </div>
                    <!-- /.panel -->
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#collapseEight">How to set java runtime option when using atlassian OSGi</a>
                            </h4>
                        </div>
                        <div id="collapseEight" class="panel-collapse collapse">
                            <div class="panel-body">
                                <p><code>-Datlassian.org.osgi.framework.bootdelegation=sun.,com.sun.,com.navercorp.*,org.apache.xerces.*</code></p>
                            </div>
                        </div>
                    </div>
                    <!-- /.panel -->
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#collapseNine">Why do I see UI send requests to http://www.google-analytics.com/collect?</a>
                            </h4>
                        </div>
                        <div id="collapseNine" class="panel-collapse collapse">
                            <div class="panel-body">
                                <p>Pinpoint Web module has google analytics attached which tracks the number and the order of button clicks in the Server Map, Transaction List, and the Inspector View.<br>
                                This data is used to better understand how users interact with the Web UI which gives us valuable information on improving Pinpoint Web's user experience. To disable this for any reason, set following option to false in pinpoint-web.properties for your web instance.</p>
                            </div>
                        </div>
                    </div>
</div>
<!-- /.panel-group -->

