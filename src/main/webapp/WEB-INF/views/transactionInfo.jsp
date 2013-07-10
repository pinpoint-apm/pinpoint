<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="pinpoint" uri="http://pinpoint.nhncorp.com/pinpoint" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">
<head>
    <title>Transaction details (${traceId})</title>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <link href="/common/css/bootstrap/bootstrap.css" rel="stylesheet">
    <link href="/common/css/bootstrap/bootstrap-responsive.css" rel="stylesheet"/>
    <link href="/common/css/pinpoint/pinpoint.css" rel="stylesheet"/>
    <link href="/common/css/pinpoint/sorttable.css" rel="stylesheet"/>
    <link href="/common/css/pinpoint/scatter.css" rel="stylesheet"/>
    <link href="/common/css/datepicker.css" rel="stylesheet"/>
    <link href="/select2/select2-customized.css" rel="stylesheet"/>

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

	<!-- commons -->    
    <script type="text/javascript" src="/common/js/jquery/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="/common/js/jquery/jquery-ui-1.10.2.js"></script>
    <script type="text/javascript" src="/common/js/bootstrap.min.js"></script>
	<script type="text/javascript" src="/common/js/bootstrap-datepicker.js"></script>
	<script type="text/javascript" src="/common/js/modernizr-2.6.2.min.js"></script>
    <script type="text/javascript" src="/common/js/date.js"></script>
	<script type="text/javascript" src="/common/js/pinpoint/scatter/underscore-min.js"></script>
	<script type="text/javascript" src="/common/js/pinpoint/scatter/jquery.Class.js"></script>
    <script type="text/javascript" src="/common/js/pinpoint/pinpoint.js"></script>
    <script type="text/javascript" src="/common/js/pinpoint/jquery.tmpl.min.js"></script>
    <script type="text/javascript" src="/common/js/sorttable.js"></script>
    <script type="text/javascript" src="/select2/select2.js"></script>
    
    <!-- scatter chart -->
    <script type="text/javascript" src="/common/js/pinpoint/chart-scatter4.js"></script>
	<script type="text/javascript" src="/common/js/pinpoint/scatter/jquery.dragToSelect.js"></script>
	<script type="text/javascript" src="/common/js/pinpoint/scatter/jquery.BigScatterChart.js"></script>
    
	<!-- server map -->    
    <script type="text/javascript" src="/common/js/pinpoint/chart-servermap.js"></script>
    <script type="text/javascript" src="/common/js/go.js"></script>
    <script type="text/javascript" src="/common/js/pinpoint/servermap/Point2D.js"></script>
    <script type="text/javascript" src="/common/js/pinpoint/servermap/intersection.js"></script>
    <script type="text/javascript" src="/common/js/pinpoint/servermap/canvas.roundRect.js"></script>
    <script type="text/javascript" src="/common/js/pinpoint/servermap/jquery.ServerMap.js"></script>
    
    <!-- grid -->
    <link type="text/css" rel="stylesheet" href="/common/js/pinpoint/grid/flexigrid.css" />
	<link type="text/css" rel="stylesheet" href="/common/js/pinpoint/grid/jquery.treetable.css" />
	<link type="text/css" rel="stylesheet" href="/common/js/pinpoint/grid/jquery.treetable.theme.simple.css" />
	<script type="text/javascript" src="/common/js/pinpoint/grid/flexigrid.js"></script>
	<script type="text/javascript" src="/common/js/pinpoint/grid/jquery.treetable.js"></script>
	<script type="text/javascript" src="/common/js/pinpoint/grid/jquery.TreeGridTable.js"></script>
    
    <!-- help -->
    <script type="text/javascript" src="/common/js/pinpoint/help.js"></script>
    <script type="text/javascript" src="/common/js/pinpoint/message.js"></script>
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
            text-align:center;
        }
        
        #callStacks TD {
            font-family:consolas;
        }
        
        #callStacks .method {
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        #callStacks .arguments {
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        #callStacks .exectime {
            text-align: center;
        }
        
        #callStacks .exectime.info {
        }
        
        #callStacks .time {
            text-align: right;
        }
        
        #callStacks .gap {
            text-align: right;
        }
        
        #callStacks .gap.info {
        }
        
        #callStacks .service {
			overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
        #callStacks .agent {
			overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        #callStacks .bar {
            vertical-align: middle;
        }
    </style>
</head>
<body>

<button id="btnResize" class="btn btn-small" type="button" style="display:none; position:absolute; top:0px; left:0px;font-size:10px;">
	<i class="icon-resize-vertical"></i>
</button>

<h3>Application : ${applicationName}</h3>
<!-- 
<h5>TraceId : ${traceId.formatString}</h5>
<h5>AgentId : ${recordSet.agentId} &nbsp;&nbsp; ApplicationId : ${recordSet.applicationId}</h5>
-->

<ul class="nav nav-tabs" id="traceTabs">
	<li><a href="#CallStacks" data-toggle="tab">Call Stacks</a></li>
	<li><a href="#ServerMap" data-toggle="tab">Server Map</a></li>
	<li><a href="#Timeline" data-toggle="tab">RPC Timeline</a></li>
	<li><a href="#Details" data-toggle="tab">Details (for PINPOINT developer)</a></li>
</ul>

<div class="tab-content">
	<div class="tab-pane active" id="CallStacks">
		<!-- begin new call stack -->
	    <table id="callStacks">
	        <thead>
	        <tr>
	            <th width="500">Method</th>
	            <th width="400">Argument</th>
	            <th width="80">Exec Time</th>
	            <th width="50">Gap</th>
	            <th width="50">Time[ms]</th>
	            <th width="100">Time[%]</th>
                <th>Class</th>
	            <th>ApiType</th>
	            <th>Agent</th>
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
		                	<tr class="error" data-tt-id="${record.id}" data-tt-parent-id="<c:if test="${record.pId > 0}">${record.pId}</c:if>" data-tt-branch="${record.method}">
						</c:when>
						<c:when test="${record.focused}">
			                <tr class="info" data-tt-id="${record.id}" data-tt-parent-id="<c:if test="${record.pId > 0}">${record.pId}</c:if>" data-tt-branch="${record.method}">
						</c:when>
						<c:otherwise>
							<tr data-tt-id="${record.id}" data-tt-parent-id="<c:if test="${record.pId > 0}">${record.pId}</c:if>" data-tt-branch="${record.method}">
						</c:otherwise>                
					</c:choose>

					<td class="method"><c:if test="${not record.method}"><i class="icon-info-sign"></i></c:if> ${record.title}</td>
					<td class="arguments">${record.arguments}</td>
                    <td class="exectime">
                    	<c:if test="${record.method}">${pinpoint:longToDateStr(record.begin, "HH:mm:ss SSS")}</c:if>
                    </td>
                    <td class="gap"><fmt:formatNumber value="${gap}" type="number" /></td>
                    
                    <td class="time">
                    	<c:if test="${record.method}">
                    	<fmt:formatNumber type="number" value="${record.elapsed}"/>
                    	</c:if>
                    </td>
                    <td class="bar">
                    	<c:if test="${record.method}">
                        <div style="height:8px;width:<fmt:formatNumber value="${((end - begin) * barRatio) + 0.9}" type="number" pattern="#"/>px; background-color:#69B2E9;">&nbsp;</div>
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
	
	<div class="tab-pane" id="ServerMap">
		<div id="servermap" style="width:90%;height:700px; border:1px solid #DDDDDD; overflow:hidden;"></div>
	</div>
	
	<div class="tab-pane" id="Timeline">
        <!-- begin timeline -->
		<div id="timeline" style="background-color:#E8E8E8;width:1000px;font-size:11px;">
			<c:set var="startTime" scope="page" value="${callstackStart}"/>
	        <c:set var="endTime" scope="page" value="${callstackEnd}"/>
	        
	        <c:forEach items="${timeline}" var="record" varStatus="status">
	            <c:set var="depth" scope="page" value="${span.depth}"/>
                <c:set var="begin" scope="page" value="${record.begin}"/>
                <c:set var="end" scope="page" value="${record.begin + record.elapsed}"/>
				<c:if test="${status.first}">
					<c:set var="barRatio" scope="page" value="${1000 / (end - begin)}"/>
				</c:if>
                
				<c:if test="${record.method and not record.excludeFromTimeline and record.service != ''}">
				<div style="width:<fmt:formatNumber value="${((end - begin) * barRatio) + 0.9}" type="number" pattern="#"/>px; background-color:#69B2E9; margin-left:<fmt:formatNumber value="${((begin - startTime) * barRatio) + 0.9}" type="number" pattern="#"/>px; margin-top:3px;" onmouseover="showDetail(${status.count})" onmouseout="hideDetail(${status.count})">
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
                       <td colspan="7">
                       <span onclick="displaySpan('#detailedInfo${status.count}')" style="color:blue;cursor:pointer;">[show span]</span>
                       <div style="display:none" id="detailedInfo${status.count}">${sp}</div>
                       </td>
                   </tr>
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
                       <td colspan="7">
                       <span onclick="displaySpan('#detailedInfo${status.count}')" style="color:blue;cursor:pointer;">[show sub span]</span>
                       <div style="display:none" id="detailedInfo${status.count}">${subSp}</div>
                       </td>
                   </tr>
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

</br>
</br>

<script type="text/javascript">
	function displaySpan(id) {
		var target = $(id);
		var arr = target.text().split('');

		var result = [];
		var disableConvert = false;
		var indent = 0;
		
		var addIndent = function() {
			for(var k = 0; k <= indent; k++) {
				result.push("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
			}
		}
		
		for(var i = 0; i < arr.length; i++) {
			var ch = arr[i];
			
			if (ch == "'" || ch == "\"") {
				disableConvert = !disableConvert;
				result.push(ch);
			} else if (ch == "=") {
				result.push(' ');
				result.push(ch);
				result.push(' ');
			} else if (ch == "{") {
				indent++;
				result.push(' ');
				result.push(ch);
				result.push("<br/>");
				addIndent();
			} else if (ch == "}") {
				indent--;
				result.push("<br/>");
				addIndent();	
				result.push(ch);
			} else if (arr[i] == "," && !disableConvert) {
				result.push(ch);
				result.push("<br/>");
				addIndent();
			} else {
				result.push(ch);
			}
		}
		
		target.text('');
		target.html(result.join(''));
		target.show();
	}

    var data = {
   		"applicationMapData" : {
   			"nodeDataArray": [
   				<c:forEach items="${nodes}" var="node" varStatus="status">
   				    {
   				    	"id" : ${status.count},
   				    	"key" : ${status.count},
   				    	<c:choose>
   				    		<c:when test="${node.serviceType.desc == 'USER'}">
   					    	"text" : "USER",
   				    		</c:when>
   				    		<c:otherwise>
   					    	"text" : "${node}",
   				    		</c:otherwise>
   				    	</c:choose>
   					    "hosts" : [
   					    <c:forEach items="${node.hosts}" var="host" varStatus="status2">
   					        "${host}"
   					        <c:if test="${!status2.last}">,</c:if>
   					    </c:forEach>
   					    ],
   					    "category" : "${node.serviceType.desc}",
   					    "terminal" : "${node.serviceType.terminal}"
   				    } <c:if test="${!status.last}">,</c:if>
   				</c:forEach>
			],
			"linkDataArray": [
   			 	<c:forEach items="${links}" var="link" varStatus="status">
   				    {
   				    	"id" : "${link.from.sequence + 1}-${link.to.sequence + 1}",
   						"from" : ${link.from.sequence + 1},
   						"to" : ${link.to.sequence + 1},
   						"text" : ${link.histogram.totalCount},
   						"error" : ${link.histogram.errorCount},
   						"slow" : ${link.histogram.slowCount},
   						"histogram" : ${link.histogram}
   					} <c:if test="${!status.last}">,</c:if>
   				</c:forEach>   	
   			 ]
   		}
    };
    
	var expandCell = function(e) {
		var target = $(e.target);
		if (target.data("isExpanded") === 'undefined' || target.data("isExpanded") == "T") {
			target.data("isExpanded", "F");
			target.css("white-space", "nowrap");
			target.css("text-overflow", "ellipsis");
            target.css("word-break", "normal");
		} else {
			target.data("isExpanded", "T");
			target.css("white-space", "normal");
			target.css("text-overflow", "initial");
            target.css("word-break", "break-all");
		}
    }
    
    $(".arguments").bind("click", expandCell);
    $(".method").bind("click", expandCell);

    var oServerMap;
    $("#traceTabs li:nth-child(2) a").bind("click", function() {
    	if (oServerMap != null) {
    		return;
    	}
    	
        var containerId = "servermap";

    	if (data.applicationMapData.nodeDataArray.length == 0) {
    		return;
    	}
    	
    	if (oServerMap == null) {
    		oServerMap = new ServerMap({
    	        sContainerId : containerId,
    			"sImageDir" : '/images/icons/',
    			"htIcons" : {
    				'APACHE' : 'APACHE.png',
    				'ARCUS' : 'ARCUS.png',
    				'CUBRID' : 'CUBRID.png',
    				'ETC' : 'ETC.png',
    				'MEMCACHED' : 'MEMCACHED.png',
    				'MYSQL' : 'MYSQL.png',
    				'QUEUE' : 'QUEUE.png',
    				'TOMCAT' : 'TOMCAT.png',
    				'UNKNOWN_CLOUD' : 'UNKNOWN_CLOUD.png',
    				'UNKNOWN_GROUP' : 'UNKNOWN_CLOUD.png',
    				'USER' : 'USER.png'
    			},
    			fOnNodeClick : function(e, data) {
    				// nodeClickHandler(e, data, "#" + containerId);
    			},
    			fOnLinkClick : function(e, data) {
    				// linkClickHandler(e, data, "#" + containerId);
    			}
    	    });
    	}
        oServerMap.load(data.applicationMapData);    	
    });
    
    $(document).ready(function () {
        $('#chartTabs a:first').tab('show');
        $('#traceTabs a:first').tab('show');
        if (!top.btnResize) {
        	$('#btnResize').bind('click', top.resizeFrame);
        	$('#btnResize').show();
        }
        
        var oTreeGridTable = new TreeGridTable({
			tableId : "callStacks",
			height : "600"
		});
    });
</script>
</body>
</html>