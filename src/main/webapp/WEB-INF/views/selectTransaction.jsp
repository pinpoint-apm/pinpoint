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
    <link href="/common/css/bootstrap/bootstrap.css" rel="stylesheet">
    <link href="/common/css/bootstrap/bootstrap-responsive.css" rel="stylesheet"/>
    <link href="/select2/select2-customized.css" rel="stylesheet"/>

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

    <script type="text/javascript" src="/common/js/jquery/jquery-1.7.1.min.js"></script>
    <script type="text/javascript" src="/common/js/bootstrap.min.js"></script>
    <script type="text/javascript" src="/select2/select2.js"></script>
    <script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script type="text/javascript" src="http://d3js.org/d3.v2.min.js?2.9.1"></script>
    <script type="text/javascript" src="/common/js/sankey/sankey.js"></script>
    <script type="text/javascript" src="/common/js/hippo/hippo.js"></script>
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
</head>
<body>
<h4>TraceId: ${traceId}</h4>

<div class="container">
    <div class="row">
        <div class="span12">Application Flow Map</div>
    </div>
    <div class="row">
        <div class="span12">
            <p id="graph"></p>
        </div>
    </div>

    <div class="row">
        <div class="span12"><br/><br/><br/>Application Timeline</div>
    </div>
    <div class="row">
        <div id="timeline" style="background-color:#E8E8E8;width:1000px;">
            <c:set var="startTime" scope="page" value="0"/>
            <c:set var="endTime" scope="page" value="0"/>
            <c:forEach items="${spanList}" var="span" varStatus="status">
                <c:set var="sp" scope="page" value="${span.span}"/>
                <c:set var="begin" scope="page" value="${sp.startTime}"/>
                <c:set var="end" scope="page" value="${sp.endTime}"/>

                <div id="spanDetail${status.count}"
                     style="display:none; position:absolute; left:0; top:0;width:500px;background-color:#E8CA68;padding:10px;">
                    <ul>
                        <li>AgentId = ${sp.agentId}</li>
                        <li>UUID = ${hippo:longLongToUUID(sp.mostTraceId, sp.leastTraceId)}</li>
                        <li>spanId = ${sp.spanId}</li>
                        <li>parentSpanId = ${sp.parentSpanId}</li>
                        <li>service = ${sp.serviceName}</li>
                        <li>name = ${sp.name}</li>
                        <li>startTime = ${hippo:longToDateStr(sp.startTime)}</li>
                        <li>endTime = ${hippo:longToDateStr(sp.endTime)}</li>
                        <li>endpoint = ${sp.endPoint}</li>
                        <li>terminal = ${sp.terminal}</li>
                        <c:if test="${status.first}">
                            <c:set var="startTime" scope="page" value="${sp.startTime}"/>
                        </c:if>
                        <c:if test="${status.first}">
                            <c:set var="endTime" scope="page" value="${sp.endTime}"/>
                        </c:if>

                        <c:forEach items="${sp.annotationBoList}" var="ano" varStatus="annoStatus">
                            <li>${ano.key} = ${ano.value}</li>
                        </c:forEach>
                    </ul>
                </div>

                <c:if test="${status.first}">
                    <c:set var="barRatio" scope="page" value="${1000 / (end - begin)}"/>
                </c:if>

                <div style="width:<fmt:formatNumber value="${((end - begin) * barRatio) + 0.9}" type="number" pattern="#"/>px; background-color:#69B2E9;margin-left:<fmt:formatNumber value="${((begin - startTime) * barRatio) + 0.9}" type="number" pattern="#"/>px;margin-top:3px;"
                     onmouseover="showDetail(${status.count})" onmouseout="hideDetail(${status.count})">
                    <div style="width:200px;">${sp.serviceName} (${end - begin}ms)</div>
                </div>
            </c:forEach>
        </div>
    </div>

    <div class="row">
        <div class="span12"><br/><br/><br/>Application Details</div>
    </div>
    <div class="row">
        <div class="span">
            <table id="businessTransactions" class="table table-bordered">
                <thead>
                <tr>
                    <th>#</th>
                    <th>Action</th>
                    <th>Arguments</th>
                    <th>EndPoint</th>
                    <th>Total[ms]</th>
                    <th>Exec[ms]</th>
                    <th>Application</th>
                    <th>Agent</th>
                </tr>
                </thead>
                <tbody>

                <c:forEach items="${spanList}" var="span" varStatus="status">
                    <c:set var="sp" scope="page" value="${span.span}"/>
                    <c:forEach items="${sp.annotationBoList}" var="ano" varStatus="annoStatus">
                        <tr>
                            <td>${span.depth}</td>
                            <td>${ano.key}</td>
                            <td>${ano.value}</td>
                            <td><c:if test="${annoStatus.first}">${sp.endPoint}</c:if></td>
                            <td><c:if test="${annoStatus.first}">${sp.endTime - sp.startTime}</c:if></td>
                            <td></td>
                            <td><c:if test="${annoStatus.first}">${sp.serviceName}</c:if></td>
                            <td>${sp.agentId} (${ano.timestamp})</td>
                        </tr>
                        <c:set var="bt" scope="page" value="${ano.timestamp}"/>
                    </c:forEach>
                    <tr>
                        <td colspan="7">&nbsp;</td>
                    </tr>
                </c:forEach>

                </tbody>
            </table>
        </div>
    </div>

    <div class="row">
        <div class="span12"><br/><br/><br/>RPC flow</div>
    </div>
    <div class="row">
        <div class="span12">
            <p id="rpcgraph"></p>
        </div>
    </div>
</div>

<script type="text/javascript">
    var data = {
        "nodes":[
            <c:forEach items="${nodes}" var="node" varStatus="status">
            {
                "name":"${node}",
                "recursiveCallCount":"${node.recursiveCallCount}",
                "agentIds":[
                    <c:forEach items="${node.agentIds}" var="agentId" varStatus="status2">
                    "${agentId}"
                    <c:if test="${!status2.last}">, </c:if>
                    </c:forEach>
                ]
            }
            <c:if test="${!status.last}">,
            </c:if>
            </c:forEach>
        ],
        "links":[
            <c:forEach items="${links}" var="link" varStatus="status">
            {"source": ${link.from.sequence}, "target": ${link.to.sequence}, "value": ${link.callCount}}
            <c:if test="${!status.last}">,
            </c:if>
            </c:forEach>
        ]
    };

    var rpcdata = {
        "nodes":[
            <c:forEach items="${rpcnodes}" var="node" varStatus="status">
            <c:if test="${node.terminal}">
            {"name":"${node.serviceName}:${node.rpc}"}
            </c:if>
            <c:if test="${not node.terminal}">
            {"name":"${node}"}
            </c:if>
            <c:if test="${!status.last}">,
            </c:if>
            </c:forEach>
        ],
        "links":[
            <c:forEach items="${rpclinks}" var="link" varStatus="status">
            {"source":${link.from.sequence}, "target":${link.to.sequence}, "value":${link.callCount}}
            <c:if test="${!status.last}">,
            </c:if>
            </c:forEach>
        ]
    };

    $(document).ready(function () {
        drawSankeyChart(data, "#graph", 960, 400);
        drawSankeyChart(rpcdata, "#rpcgraph", 960, 400);
    });
</script>

</body>
</html>