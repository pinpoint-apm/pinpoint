<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="hippo" uri="http://hippo.nhncorp.com/hippo" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>PINPOINT</title>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <link href="/common/css/bootstrap/bootstrap.css" rel="stylesheet">
    <link href="/common/css/bootstrap/bootstrap-responsive.css" rel="stylesheet"/>
    <link href="/common/css/hippo/hippo.css" rel="stylesheet"/>
    <link href="/common/css/hippo/sorttable.css" rel="stylesheet"/>
    <link href="/common/css/hippo/scatter.css" rel="stylesheet"/>
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
	<script type="text/javascript" src="/common/js/hippo/scatter/underscore-min.js"></script>
	<script type="text/javascript" src="/common/js/hippo/scatter/jquery.Class.js"></script>
    <script type="text/javascript" src="/common/js/hippo/jquery.tmpl.min.js"></script>
    <script type="text/javascript" src="/common/js/hippo/hippo.js"></script>
    <script type="text/javascript" src="/common/js/hippo/navigationbar.js"></script>
    <script type="text/javascript" src="/select2/select2.js"></script>
    
    <!-- scatter chart -->
    <script type="text/javascript" src="/common/js/hippo/chart-scatter4.js"></script>
	<script type="text/javascript" src="/common/js/hippo/scatter/jquery.dragToSelect.js"></script>
	<script type="text/javascript" src="/common/js/hippo/scatter/jquery.BigScatterChart.js"></script>
    
	<!-- server map -->    
    <script type="text/javascript" src="/common/js/hippo/chart-servermap.js"></script>
    <script type="text/javascript" src="/common/js/go.js"></script>
    <script type="text/javascript" src="/common/js/hippo/servermap/Point2D.js"></script>
    <script type="text/javascript" src="/common/js/hippo/servermap/intersection.js"></script>
    <script type="text/javascript" src="/common/js/hippo/servermap/canvas.roundRect.js"></script>
    <script type="text/javascript" src="/common/js/hippo/servermap/jquery.ServerMap.js"></script>
    
    <!-- requests list -->
    <script type="text/javascript" src="/common/js/hippo/chart-transactions.js"></script>
    
    <!-- help -->
    <script type="text/javascript" src="/common/js/hippo/help.js"></script>
    <script type="text/javascript" src="/common/js/hippo/message.js"></script>
</head>
<body>


<div class="navbar navbar navbar-fixed-top">
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
			<h5 id="scattercharttitle">Transaction response scatter</h5>
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
});
</script>
</body>
</html>