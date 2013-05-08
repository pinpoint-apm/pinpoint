<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>PINPOINT</title>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <link href="/common/css/bootstrap/bootstrap.css" rel="stylesheet">
    <link href="/common/css/bootstrap/bootstrap-responsive.css" rel="stylesheet"/>
    <link href="/common/css/hippo/sorttable.css" rel="stylesheet"/>

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

        <!-- commons -->    
    <script type="text/javascript" src="/common/js/jquery/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="/common/js/jquery/jquery-ui-1.10.2.js"></script>
    <script type="text/javascript" src="/common/js/sorttable.js"></script>
    <script type="text/javascript" src="/common/js/date.format.js"></script>
    <script type="text/javascript" src="/common/js/hippo/hippo.js"></script>
    <style type="text/css">
        html, body {
                height: 100%;
        }
        </style>
</head>
<body>
<div style="max-height:220px;min-height:200px;overflow-y:scroll;">
        <table id="selectedBusinessTransactionsDetail" class="table table-bordered table-condensed table-hover sortable" style="font-size:12px;">
                <thead>
                <tr>
                    <th class="sorttable_numeric">#</th>
                    <th class="sorttable_numeric">Time</th>
                    <th>TraceId</th>
                    <th class="sorttable_numeric">Res. Time (ms)</th>
                    <th>Exception</th>
                    <th>Application</th>
                    <th>AgentId</th>
                    <th>ClientIP</th>
                </tr>
                </thead>
                <tbody>
                </tbody>
        </table>
</div>
<div id="loader">
        <img src="/images/ajaxloader.gif" />
</div>

<iframe id="transactionView" name="transactionView" src="" style="width:100%;height:100%;padding-bottom:300px;"></iframe>

<script type="text/javascript">
var selectedRow;

function selectRow(row) {
        if (selectedRow) {
                $(selectedRow).css({'background-color':'#FFFFFF'});
        }
        selectedRow = row;
        $(row).css({'background-color':'#FFFF00'});
        $("#transactionView").hide();
        $("#loader").show();
        return false;
}

$(document).ready(function () {
        if(!opener) {
                return;
        }
        
        $("#transactionView").bind("load", function() {
                $("#loader").hide();
                $("#transactionView").show();
        });
        $("#transactionView").hide();
        
        var traces = opener.selectdTracesBox[window.name];
        
        // 새로고침 때문에 chart가 변경될 때 delete하도록 변경.
        //delete opener.selectdTracesBox[window.name];
        
        if (!traces) {
                alert("Query parameter 캐시가 삭제되었기 때문에 데이터를 조회할 수 없습니다.\n\n이러한 현상은 scatter chart를 새로 조회했을 때 발생할 수 있습니다.");
                $("#loader").hide();
                return;
        }
        
        var query = [];
        var temp = {};
        for (var i = 0; i < traces.length; i++) {
                if (i > 0) {
                        query.push("&");
                }
                query.push("tr");
                query.push(i);
                query.push("=");
                query.push(traces[i].traceId);
                
                query.push("&ti");
                query.push(i);
                query.push("=");
                query.push(traces[i].x)
                
                query.push("&re");
                query.push(i);
                query.push("=");
                query.push(traces[i].y)
        }
        
        $.post("/transactionmetadata.hippo", query.join(""), function(d) {
                writeContents(d);
                $("#loader").hide();
        })
        .fail(function() {
                alert("Failed to fetching the request informations.");
        });
});

var writeContents = function(d) {
        $("#selectedBusinessTransactionsDetail TBODY").empty();
        
        var data = d.metadata;
        
        var html = [];
        for (var i = 0; i < data.length; i++) {
                if(data[i].exception) {
                        html.push("<tr class='error' onclick='selectRow(this);'>");
                } else {
                        html.push("<tr onclick='selectRow(this);'>");
                }

                html.push("<td style='padding-right:5px;text-align:right'>");
                html.push(i + 1);
                html.push("</td>");

                html.push("<td sorttable_customkey='");
                html.push(data[i].startTime);
                html.push("'>");
                html.push(new Date(data[i].startTime).format("HH:MM:ss l"));
                html.push("</td>");
                
                html.push("<td>");
                html.push("<a href='");
                html.push("/transactionInfo.hippo?traceId=" + data[i].traceId + "&focusTimestamp=" + data[i].collectorAcceptTime);
                html.push("' target='transactionView'>");
                html.push(data[i].traceId);
                html.push("</a>");
                html.push("</td>");

                html.push("<td style='padding-right:30px;text-align:right'>");
                html.push(formatNumber(data[i].elapsed));
                html.push("</td>");

                html.push("<td>");
                if (data[i].exception) {
                        html.push(data[i].exception);
                }
                html.push("</td>");
                
                html.push("<td>");
                html.push(data[i].application);
                html.push("</td>");
                
                html.push("<td>");
                html.push(data[i].agentId);
                html.push("</td>");
                
                html.push("<td>");
                html.push("<a href='#' onclick=\"alert('not implemented. ip정보 조회 페이지로 연결.');\">");
                html.push(data[i].remoteAddr);
                html.push("</a>");
                html.push("</td>");

                html.push("</tr>");
        }

        $("#selectedBusinessTransactionsDetail TBODY").append(html.join(''));
}
</script>
</body>
</html>