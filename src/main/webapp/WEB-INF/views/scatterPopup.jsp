<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="hippo" uri="http://hippo.nhncorp.com/hippo" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>PINPOINT - ${applicationName} request list</title>
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
    
    <script type="text/javascript" src="/select2/select2.js"></script>
    <script type="text/javascript" src="/common/js/bootstrap.min.js"></script>
	<script type="text/javascript" src="/common/js/modernizr-2.6.2.min.js"></script>
	<script type="text/javascript" src="/common/js/bootstrap-datepicker.js"></script>
    <script type="text/javascript" src="/common/js/sorttable.js"></script>
    <script type="text/javascript" src="/common/js/date.format.js"></script>
    <script type="text/javascript" src="/common/js/hippo/hippo.js"></script>

	<script type="text/javascript" src="/common/js/d3.js"></script>
    
    <!-- scatter chart -->
    <script type="text/javascript" src="/common/js/hippo/chart-scatter4.js"></script>
	<script type="text/javascript" src="/common/js/hippo/scatter/underscore-min.js"></script>
    <script type="text/javascript" src="/common/js/hippo/scatter/date.js"></script>
	<script type="text/javascript" src="/common/js/hippo/scatter/jquery.Class.js"></script>
	<script type="text/javascript" src="/common/js/hippo/scatter/jquery.dragToSelect.js"></script>
	<script type="text/javascript" src="/common/js/hippo/scatter/jquery.BigScatterChart.js"></script>
    
    <!-- help -->
    <script type="text/javascript" src="/common/js/hippo/help.js"></script>
    <script type="text/javascript" src="/common/js/hippo/message.js"></script>
    <style type="text/css">
    body {
    	padding: 30px;
	}
    </style>
</head>
<body>
<div class="container">
	<div class="row">
		<div class="span4">
			<div id="scatterchart"></div>
		</div>
	</div>
</div>

<!-- MODAL -->
<div class="modal hide fade" id="traceIdSelectModal" style="width:1200px; margin-left:-600px">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal">×</button>
        <h3>Selected Traces</h3>
    </div>
    <div class="modal-body">
		<table id="selectedBusinessTransactionsDetail" class="table table-bordered table-hover sortable">
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
    <div class="modal-footer">
        <a href="#" class="btn" data-dismiss="modal">Close</a>
    </div>
</div>
<!-- END OF MODAL -->

<script type="text/javascript">
$(document).ready(function () {
	showResponseScatter("${applicationName}", ${from}, ${to}, ${period}, ${usePeriod}, 700, 450);
});
</script>
</body>
</html>