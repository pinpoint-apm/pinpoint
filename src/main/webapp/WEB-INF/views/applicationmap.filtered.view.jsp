<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="pinpoint" uri="http://pinpoint.nhncorp.com/pinpoint" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>PINPOINT</title>
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
    <script type="text/javascript" src="/common/js/pinpoint/jquery.tmpl.min.js"></script>
    <script type="text/javascript" src="/common/js/pinpoint/pinpoint.js"></script>
    <script type="text/javascript" src="/common/js/pinpoint/navigationbar.js"></script>
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
    
    <!-- requests list -->
    <script type="text/javascript" src="/common/js/pinpoint/chart-transactions.js"></script>
    
    <!-- help -->
    <script type="text/javascript" src="/common/js/pinpoint/help.js"></script>
    <script type="text/javascript" src="/common/js/pinpoint/message.js"></script>
</head>
<body>


<div class="navbar navbar-fixed-top">
  <div class="navbar-inner">
    <div class="container">
      <button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
      </button>
      <img class="brand" src="/images/logo.png" width="116" height="18" />
      <div class="nav-collapse collapse">
        <ul class="nav">
          <li class="">
	      	<p class="navbar-text">Filtered Map</p>
          </li>
          <li class="divider-vertical"></li>
          <li class="">
          	<p class="navbar-text">${applicationName}</p>
          </li>
          <li class="divider-vertical"></li>
          <li class="">
          	<p class="navbar-text">
	      	<fmt:formatDate value="${fromDate}" pattern="yyyy-MM-dd HH:mm:ss"/> ~ <fmt:formatDate value="${toDate}" pattern="yyyy-MM-dd HH:mm:ss"/>
	      	</p>
          </li>
          <li class="divider-vertical"></li>
          <li class="">
          	<p class="navbar-text">
          		${filter}
          	</p>
          </li>
        </ul>
      </div>
    </div>
  </div>
</div>

<div class="container" id="warningMessage"></div>

<!-- BODY -->
<div class="chart-container">
    <div class="chart-left">
    	<div class="container">
    		<!--
	    	<div class="alert alert-info">
	    		<button type="button" class="close" data-dismiss="alert">&times;</button>
	    		Filter : ${filterText}
	    	</div>
			-->
			<div id="progressbar"><img src="/images/ajaxloader.gif" /></div>
    	</div>
		<div id="servermap" class="servermap"></div>
	</div>
	<div class="chart-right">
		<div id="scatterChartContainer" style="display:none;">
			<span onclick="" style="text-decoration:underline;cursor:pointer">Show all transactions</span>
			&nbsp;&nbsp;&nbsp;
			<i class="icon-fullscreen" onclick="expandScatter($(this));" onmouseover="$(this).tooltip('show');" title="Expand to new popup window." style="cursor:pointer;"></i>
			&nbsp;&nbsp;&nbsp;
			<a href=""><i class="icon-download-alt" onmouseover="$(this).tooltip('show');" title="Download as PNG image format file." style="cursor:pointer;"></i></a>
			<div id="scatterchart"></div>
		</div>
	</div>
</div>
<!-- END OF BODY -->

<script type="text/javascript">
$(document).ready(function () {
	showServerMap("${applicationName}", "${serviceType}", ${from}, ${to}, 0, false, "${filterText}", function() {
		$("#progressbar").hide();
	});
	showResponseScatter("${applicationName}", ${from}, ${to}, 0, false, "${filterText}");
});
</script>
<script id="LinkInfoBox" type="text/x-jquery-tmpl">
	<div class="LinkInfoBox" style="position:absolute;border:1px solid #000;background:#fff;padding:5px 10px;-webkit-border-radius: 5px;z-index:3;">
			Response statistics
			<ul>
				{{var lastKey=''}}
				{{each(key, value) histogram}}
				<li>&lt;= {{= key}}ms : {{= value}}</li>
				{{eval lastKey=key}}
				{{/each}}
				<li>&gt; {{= lastKey}}ms : {{= slow}}</li>
				<li>Failed : {{= error}}</li>
			</ul>
			<hr/>
			<a href="#" onclick="filterPassingTransaction('{{= sourceinfo.applicationName}}', '{{= query.serviceType}}', {{= query.from}}, {{= query.to}}, '{{= sourceinfo.serviceType}}', '{{= sourceinfo.applicationName}}', '{{= targetinfo.serviceType}}', '{{= targetinfo.applicationName}}', '{{= query.filter}}');">Scan passing transactions</a><br/>
			<a href="#" onclick="alert('Sorry. Not implemented.');">Passing transaction list</a>
		<button style="position:absolute;top:2px;right:2px;" onClick="$(this).parent().remove()">X</button>
	</div>
</script>
<script id="ApplicationBox" type="text/x-jquery-tmpl">
	<div class="ServerBox" style="position:absolute;border:1px solid #000;background:#fff;padding:5px 10px;-webkit-border-radius: 5px;z-index:3;">
			Application
				<ul>
					<li>{{= text}}</li>
				</ul>
			Application Type
				<ul>
					<li>{{= category}}</li>
				</ul>
			{{if hosts.length > 0}}
			Hosts
				<ul>
					{{each(key, value) hosts}}
						<li>
							<span class="label label-success">OK</span>
							{{= value}}
							<a href="http://nsight.nhncorp.com/dashboard_server/{{= value.split(':')[0].replace('.nhnsystem.com','')}}" target="_blank">(NSight)</a>
						</li>
					{{/each}}
				</ul>
			{{/if}}
			{{if agents.length > 0}}
			Server instances
				<ul>
					{{each(key, value) agents}}
					<li>
						<span class="label label-success">OK</span>
						{{= value.agentId}}
						<a href="#" onclick="alert('Sorry. Not implemented.');">(Kuvasz)</a>
						<a href="#" onclick="alert('Sorry. Not implemented.');">(Tools)</a>
					</li>
					{{/each}}
				</ul>
			{{/if}}
			<hr/>

			<a href="#" onclick="alert('Sorry. Not implemented.');">Scan passing transactions</a><br/>
			<a href="#" onclick="showResponseScatter('{{= text}}', {{= query.from}}, {{= query.to}}, {{= query.period}}, {{= query.usePeriod}}, '{{= query.filter}}');">Transaction response scatter chart</a><br/>
			<a href="#" onclick="showRequests('{{= text}}', {{= query.from}}, {{= query.to}}, {{= query.period}}, {{= query.usePeriod}});">Transaction list</a>

		<button style="position:absolute;top:2px;right:34px;" onClick="man('applicationmap');"><i class="pinpoint-action-icon icon-question-sign"></i></button>
		<button style="position:absolute;top:2px;right:2px;" onClick="$(this).parent().remove()"><i class="pinpoint-action-icon icon-remove"></i></button>
	</div>
</script>
<script id="ClientBox" type="text/x-jquery-tmpl">
	<div class="ApplicationBox" style="position:absolute;border:1px solid #000;background:#fff;padding:5px 10px;-webkit-border-radius: 5px;z-index:3;">
			Clients
			Calls
				<ul>
					<li>Sorry. Not implemented.</li>
				</ul>
			<hr/>
			<a href="#" onclick="alert('Sorry. Not implemented.');">Show transactions</a>
		<button style="position:absolute;top:2px;right:2px;" onClick="$(this).parent().remove()">X</button>
	</div>
</script>
<script id="UnknownGroupBox" type="text/x-jquery-tmpl">
	<div class="UnknownGroupBox" style="position:absolute;border:1px solid #000;background:#fff;padding:5px 10px;-webkit-border-radius: 5px;z-index:3;">
			Application Group
				<ul>
					{{each(index, value) text.split('\n')}}
					<li>{{= value}}</li>
					{{/each}}
				</ul>
			Group Type
				<ul>
					<li>UNKNOWN</li>
				</ul>
			{{if hosts.length > 0}}
			Hosts
				<ul>
					{{each(key, value) hosts}}
						<li>
							<span class="label label-success">OK</span>
							{{= value}}
							<a href="http://nsight.nhncorp.com/dashboard_server/{{= value.split(':')[0].replace('.nhnsystem.com','')}}" target="_blank">(NSight)</a>
						</li>
					{{/each}}
				</ul>
			{{/if}}
			{{if agents.length > 0}}
			Server instances
				<ul>
					{{each(key, value) agents}}
					<li>
						<span class="label label-success">OK</span>
						{{= value.agentId}}
						<a href="#" onclick="alert('Sorry. Not implemented.');">(Kuvasz)</a>
						<a href="#" onclick="alert('Sorry. Not implemented.');">(Tools)</a>
					</li>
					{{/each}}
				</ul>
			{{/if}}
		<button style="position:absolute;top:2px;right:34px;" onClick="man('applicationmap');"><i class="pinpoint-action-icon icon-question-sign"></i></button>
		<button style="position:absolute;top:2px;right:2px;" onClick="$(this).parent().remove()"><i class="pinpoint-action-icon icon-remove"></i></button>
	</div>
</script>
</body>
</html>