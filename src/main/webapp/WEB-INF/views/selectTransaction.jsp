<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="hippo" uri="http://hippo.nhncorp.com/hippo" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">
<head>
    <title>Transaction details (${traceId})</title>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <link href="/common/css/hippo/hippo.css" rel="stylesheet">
    <link href="/common/css/bootstrap/bootstrap.css" rel="stylesheet">
    <link href="/common/css/bootstrap/bootstrap-responsive.css" rel="stylesheet"/>

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

    <script type="text/javascript" src="/common/js/jquery/jquery-1.7.1.min.js"></script>
    <script type="text/javascript" src="/common/js/bootstrap.min.js"></script>
    
    <script type="text/javascript" src="/common/js/hippo/chart-scatter.js"></script>
    <script type="text/javascript" src="/common/js/hippo/chart-springy.js"></script>
    
    <script type="text/javascript" src="/common/js/hippo/servermap/jquery.tmpl.min.js"></script>
    <script type="text/javascript" src="/common/js/hippo/servermap/Point2D.js"></script>
    <script type="text/javascript" src="/common/js/hippo/servermap/intersection.js"></script>
    <script type="text/javascript" src="/common/js/hippo/servermap/springy.js"></script>
    <script type="text/javascript" src="/common/js/hippo/servermap/canvas.roundRect.js"></script>
    <script type="text/javascript" src="/common/js/hippo/servermap/hippoServerMap.js"></script>
    
    <script type="text/javascript" src="/common/js/sorttable.js"></script>
    <script type="text/javascript" src="/common/js/resizable-table.js"></script>
    <script type="text/javascript">
        function showDetail(id) {
            $("#spanDetail" + id).css("display", "");
            $("#spanDetail" + id).css("top", event.pageY);
            $("#spanDetail" + id).css("left", event.pageX);
        }

        function hideDetail(id) {
            $("#spanDetail" + id).css("display", "none");
        }
    </script>
    <style type="text/css">
		body {
		    padding-top: 5px;
		    padding-left:30px;
		    padding-right:30px;
		}
        #callStacks TH {
            padding: 3px;
            font-size:12px;
            text-align:center;
        }
        
        #callStacks TD {
            padding: 3px;
            font-size:12px;
        }

        #callStacks .seq {
            overflow: hidden;
            text-overflow: ellipsis;
            text-align:center;
        }
        
        #callStacks .seq.info {
        	border-right:0px;
        }
        
        #callStacks .method {
            overflow: hidden;
            text-overflow: ellipsis;
            max-width: 300px;
            white-space: nowrap;
            font-family:consolas;
            font-weight:normal;
        }
        
        #callStacks .method.info {
            font-weight:normal;
            border-left:0px;
        }

        #callStacks .arguments {
            overflow: hidden;
            text-overflow: ellipsis;
            max-width: 300px;
            white-space: nowrap;
            font-family:consolas;
        }

        #callStacks .exectime {
            text-align: center;
            width:80px;
        }
        
        #callStacks .exectime.info {
        	border-left:0px;
        }
        
        #callStacks .time {
            text-align: right;
            padding-right: 10px;
            width:60px;
        }
        
        #callStacks .gap {
            text-align: right;
            padding-right: 10px;
            width:40px;
        }
        
        #callStacks .gap.info {
        	border-left:0px;
        }
        
        #callStacks .service {
            width:110px;
			overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
        #callStacks .agent {
            width:110px;
			overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        #callStacks .bar {
            width: 100px;
        }
    </style>
</head>
<body>

<h3>Application : ${applicationName}</h3>
<h5>TraceId : ${traceId.formatString}</h5>
<h5>AgentId : ${recordSet.agentId} &nbsp;&nbsp; ApplicationId : ${recordSet.applicationId}</h5>
  	<br/>

<ul class="nav nav-tabs" id="traceTabs">
	<li><a href="#CallStacks" data-toggle="tab">Call Stacks</a></li>
	<li><a href="#Timeline" data-toggle="tab">RPC Timeline</a></li>
	<li><a href="#Details" data-toggle="tab">Details (for HIPPO developer)</a></li>
</ul>

<div class="tab-content">
	<div class="tab-pane active" id="CallStacks" style="overflow:hidden;">
		<!-- begin new call stack -->
	    <table id="callStacks" class="table table-bordered table-hover sortable resizable">
	        <thead>
	        <tr>
	        	<th class="sorttable_numeric">Seq</th>
	            <th class="sorttable_nosort">Exec Time</th>
	            <th class="sorttable_nosort">Gap</th>
	            <th class="sorttable_nosort">Method</th>
	            <th class="sorttable_nosort">Argument</th>
	            <th class="sorttable_numeric">Time[ms]</th>
	            <th class="sorttable_nosort">Time[%]</th>
                <th class="sorttable_nosort">Class</th>
	            <th class="sorttable_nosort">ApiType</th>
	            <th class="sorttable_nosort">Agent</th>
	        </tr>
	        </thead>
	        <tbody>
	        <c:set var="startTime" scope="page" value="${callstackStart}"/>
	        <c:set var="endTime" scope="page" value="${callstackEnd}"/>
	        <c:set var="seq" scope="page" value="0"/>
			<c:set var="gap" scope="page" value="0"/>
	        
	        <c:forEach items="${callstack}" var="record" varStatus="status">
	            <c:set var="depth" scope="page" value="${span.depth}"/>
	            <c:if test="${record.method}">
	            	<c:if test="${not status.first}">
               			<c:set var="gap" scope="page" value="${record.begin - begin}"/>
               		</c:if>
	                <c:set var="begin" scope="page" value="${record.begin}"/>
	                <c:set var="end" scope="page" value="${record.begin + record.elapsed}"/>
               	</c:if>
                
				<c:if test="${status.first}">
					<c:set var="barRatio" scope="page" value="${100 / (end - begin)}"/>
				</c:if>

				<c:choose>
					<c:when test="${record.title == 'Exception'}">
	                	<tr class="error">
					</c:when>
					<c:when test="${record.focused}">
		                <tr class="info">
					</c:when>
					<c:otherwise>
						<tr>
					</c:otherwise>                
				</c:choose>
				                
                	<c:if test="${record.method}">
                	<c:set var="seq" scope="page" value="${seq + 1}"/>
                	<td sorttable_customkey="${status.count}" class="seq">${seq}</td>
                    <td class="exectime">
                    	<c:if test="${record.method}">
                    		${hippo:longToDateStr(record.begin, "HH:mm:ss SSS")}
                    	</c:if>
                    </td>
                    <td class="gap">${gap}</td>
                    <td class="method">
                    </c:if>
                    
                	<c:if test="${not record.method}">
                	<td sorttable_customkey="${status.count}" class="seq info"></td>
                    <td class="exectime info">
                    	<c:if test="${record.method}">
                    		${hippo:longToDateStr(record.begin, "HH:mm:ss SSS")}
                    	</c:if>
                    </td>
                    <td class="gap info"></td>
                    <td class="method">
                    </c:if>

                    	<c:if test="${record.tab > 1}">
                        	<c:forEach begin="2" end="${record.tab}">&nbsp;</c:forEach>
                        </c:if>
                        <c:choose>
                        	<c:when test="${not record.method}"><i class="icon-info-sign"></i></c:when>
                        	<c:otherwise> </c:otherwise>
                        </c:choose>
						${record.title}
                    </td>
                    
                    <td class="arguments">${record.arguments}</td>
                    <td class="time" sorttable_customkey="${record.elapsed}">
                    	<c:if test="${record.method}">
                    	<fmt:formatNumber type="number" value="${record.elapsed}"/>
                    	</c:if>
                    </td>
                    <td class="bar">
                    	<c:if test="${record.method}">
                        <div style="width:<fmt:formatNumber value="${((end - begin) * barRatio) + 0.9}" type="number" pattern="#"/>px; background-color:#69B2E9;">&nbsp;</div>
                    	</c:if>
                    </td>
                    <td class="simpleClassName">${record.simpleClassName}</td>
                    <td class="apiType">${record.apiType}</td>
                    <td class="agent">${record.agent}</td>
                </tr>
	        </c:forEach>
	        </tbody>
	    </table>
	    <!-- end of new call stack -->
	</div>
	<div class="tab-pane" id="Timeline">
	        <!-- begin timeline -->
	        <div id="timeline" style="background-color:#E8E8E8;width:1000px;">
				<c:set var="startTime" scope="page" value="${callstackStart}"/>
		        <c:set var="endTime" scope="page" value="${callstackEnd}"/>
		        
		        <c:forEach items="${timeline}" var="record" varStatus="status">
		            <c:set var="depth" scope="page" value="${span.depth}"/>
	                <c:set var="begin" scope="page" value="${record.begin}"/>
	                <c:set var="end" scope="page" value="${record.begin + record.elapsed}"/>
					<c:if test="${status.first}">
						<c:set var="barRatio" scope="page" value="${1000 / (end - begin)}"/>
					</c:if>
	                
                   	<c:if test="${record.method and not record.excludeFromTimeline}">
                        <div style="width:<fmt:formatNumber value="${((end - begin) * barRatio) + 0.9}" type="number" pattern="#"/>px; background-color:#69B2E9; margin-left:<fmt:formatNumber value="${((begin - startTime) * barRatio) + 0.9}" type="number" pattern="#"/>px; margin-top:3px;"
                        	onmouseover="showDetail(${status.count})" onmouseout="hideDetail(${status.count})">
							<div style="width:200px;">${record.service} (${end - begin}ms)</div>
                        </div>
                        
						<div id="spanDetail${status.count}" style="display:none; position:absolute; left:0; top:0;width:500px;background-color:#E8CA68;padding:10px;">
	                    <ul>
	                        <li>${record}</li>
	                    </ul>
		                </div>
                   	</c:if>
		        </c:forEach>
	        </div>
	        <!-- end timeline -->
	</div>
	<div class="tab-pane" id="Details">
	
		<!-- begin details -->
		<table id="businessTransactions" class="table table-bordered table-hover" style="font-size:12px;">
           <thead>
           <tr>
               <th>#</th>
               <th>Action</th>
               <th>Arguments</th>
               <th>EndPoint</th>
               <th>Total[ms]</th>
               <th>Application</th>
               <th>Agent</th>
           </tr>
           </thead>
           <tbody>

           <c:forEach items="${spanList}" var="span" varStatus="status">
               <c:if test="${span.span}">
                   <c:set var="sp" scope="page" value="${span.spanBo}"/>
                   <c:forEach items="${sp.annotationBoList}" var="ano" varStatus="annoStatus">
                       <tr>
                           <td>${span.depth}</td>
                           <td>${ano.keyName}</td>
                           <td>${ano.value}</td>
                           <td><c:if test="${annoStatus.first}">${sp.endPoint}</c:if></td>
                           <td><c:if test="${annoStatus.first}">${sp.elapsed}</c:if></td>
                           <td></td>
                           <td>
                               <%--<c:if test="${annoStatus.first}">${sp.serviceName}</c:if>--%>
                           </td>
                       </tr>
                   </c:forEach>
                   <tr>
                       <td colspan="7">&nbsp;</td>
                   </tr>
               </c:if>
               <c:if test="${!span.span}">
                   <c:set var="subSp" scope="page" value="${span.spanEventBo}"/>
                   <c:forEach items="${subSp.annotationBoList}" var="ano" varStatus="annoStatus">
                       <tr>
                           <td>${span.depth}</td>
                           <td>${ano.keyName}</td>
                           <td>${ano.value}</td>
                           <td><c:if test="${annoStatus.first}">${subSp.endPoint}</c:if></td>
                           <td><c:if test="${annoStatus.first}">${subSp.endElapsed}</c:if></td>
                           <td></td>
                           <td>
                               <%--<c:if test="${annoStatus.first}">${subSp.serviceName}</c:if>--%>
                           </td>
                       </tr>
                   </c:forEach>
                   <tr>
                       <td colspan="7">&nbsp;</td>
                   </tr>
               </c:if>
           </c:forEach>
           </tbody>
       	</table>
		<!-- end of details -->
	
	
	</div>
</div>


<ul class="nav nav-tabs" id="chartTabs">
	<li><a href="#Graph" data-toggle="tab">Server Graph</a></li>
</ul>

<div class="tab-content">
	<div class="tab-pane active" id="Graph">
		<div id="springygraph" style="width:99%;height:700px;border:1px solid #000;overflow:hidden;position:relative"></div>
	</div>
</div>

<br/>
<br/>
<br/>
<br/>
<br/>
<br/>

<script type="text/javascript">
    var data = {
        "nodes":[
            <c:forEach items="${nodes}" var="node" varStatus="status">
            {
                "name":"${node}",
                "hosts":[
                    <c:forEach items="${node.hosts}" var="host" varStatus="status2">
                    "${host}"
                    <c:if test="${!status2.last}">, </c:if>
                    </c:forEach>
                ],
                "serviceType":"${node.serviceType.desc}",
                "terminal":"${node.serviceType.terminal}"
            }
            <c:if test="${!status.last}">,
            </c:if>
            </c:forEach>
        ],
        "links":[
            <c:forEach items="${links}" var="link" varStatus="status">
            {
                "source": ${link.from.sequence},
                "target": ${link.to.sequence},
                "value" : ${link.histogram.totalCount},
                "error" : ${link.histogram.errorCount},
                "slow" : ${link.histogram.slowCount},
                "histogram" : ${link.histogram}
            }
            <c:if test="${!status.last}">,
            </c:if>
            </c:forEach>
        ]
    };

    $(document).ready(function () {
        drawSpringy(data, "#springygraph", 960, 500);
        $('#chartTabs a:first').tab('show');
        $('#traceTabs a:first').tab('show');
    });
</script>
</body>
</html>