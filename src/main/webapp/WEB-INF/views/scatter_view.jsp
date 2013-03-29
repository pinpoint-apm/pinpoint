<!DOCTYPE html>
<html lang="en">
<head>
    <title>HIPPO</title>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <link href="/common/css/bootstrap/bootstrap.css" rel="stylesheet">
    <link href="/common/css/bootstrap/bootstrap-responsive.css" rel="stylesheet"/>
    <link href="/common/css/hippo/hippo.css" rel="stylesheet"/>
    <link href="/common/css/datepicker.css" rel="stylesheet"/>
    <link href="/select2/select2-customized.css" rel="stylesheet"/>

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

    <script type="text/javascript" src="/common/js/jquery/jquery-1.7.1.min.js"></script>
    <script type="text/javascript" src="/common/js/jquery/jquery-ui-1.8.18.custom.min.js"></script>
    <script type="text/javascript" src="/select2/select2.js"></script>
    <script type="text/javascript" src="/common/js/bootstrap.min.js"></script>
    
	<script type="text/javascript" src="/common/js/modernizr-2.6.2.min.js"></script>
	<script type="text/javascript" src="/common/js/bootstrap-datepicker.js"></script>
    
	<script type="text/javascript" src="/common/js/d3.js"></script>
	<script type="text/javascript" src="/common/js/d3.chart.js"></script>
	<script type="text/javascript" src="/common/js/d3.chart.scatter.js"></script>
    
    <script type="text/javascript" src="/common/js/hippo/chart-scatter2.js"></script>
    <script type="text/javascript" src="/common/js/hippo/chart-springy.js"></script>
    <script type="text/javascript" src="/common/js/hippo/chart-transactions.js"></script>
    
    <script type="text/javascript" src="/common/js/date.format.js"></script>
    <script type="text/javascript" src="/common/js/hippo/hippo.js"></script>
    
    <script type="text/javascript" src="/common/js/sorttable.js"></script>

    <script type="text/javascript" src="/common/js/hippo/servermap/jquery.tmpl.min.js"></script>
    <script type="text/javascript" src="/common/js/hippo/servermap/Point2D.js"></script>
    <script type="text/javascript" src="/common/js/hippo/servermap/intersection.js"></script>
    <script type="text/javascript" src="/common/js/hippo/servermap/springy.js"></script>
    <script type="text/javascript" src="/common/js/hippo/servermap/canvas.roundRect.js"></script>
    <script type="text/javascript" src="/common/js/hippo/servermap/hippoServerMap.js"></script>
</head>
<body>

<p id="scatter"></p>


<script type="text/javascript">
$(document).ready(function () {
   	$('#date').datepicker().on('changeDate', function(ev){
   		updateCharts();
	});
	
	$('#time').bind('focus', function(e){
		$(this).data("time", $(this).val());
	}).bind('blur', function(e){
		if ($(this).data("time") !== $(this).val()) {
	   		updateCharts();
		}
	});
	
	$('#until').delegate("button", "click", function(){
   		var $this = $(this);
   		
   		$this.siblings(".btn-primary").removeClass("btn-primary");
   		$this.addClass("btn-primary");
   		
   		if ($this.attr("id") === "now") {
   			$("#date").attr("disabled", "").val("");
   			$("#time").attr("disabled", "").val("");
	   		updateCharts();
   		} else {
   			setQueryDateToNow();
   			$("#date").attr("disabled", null);
   			$("#time").attr("disabled", null);
   		}
   	});
   	
   	$('#period').delegate("button", "click", function(){
   		var $this = $(this);
   		
   		$this.siblings(".btn-inverse").removeClass("btn-inverse active");
   		$this.addClass("btn-inverse active");
   		updateCharts();
   		$("#auto_refresh").trigger("change");
   	});
   	
   	var timer;
	$("#auto_refresh").bind("change", function(){
		if (this.checked) {
			if (timer != null) return;

			console.log("[auto-refresh] started.")
			
			clearInterval(timer);
			setQueryDateToNow();
		    updateCharts();
		    
		    var from = getQueryEndTime();
		    
			timer = setInterval(function() {
				setQueryDateToNow();
				console.log("[auto-refresh] fetching data from=" + from);
				
		        getRealtimeScatterData(from, function(data) {
		        	console.log("[auto-refresh] data fetched. " + data.scatter.length);
		        	console.log(getQueryStartTime());
		        	console.log(getQueryEndTime());
			        updateScatter(getQueryStartTime(), getQueryEndTime(), data.scatter, "#scatter");
		        	from = data.queryTo + 1;
		        });
			}, 3000);
		} else {
			clearInterval(timer);
			timer = null;
			console.log("[auto-refresh] stopped.")
		}
	});

	function cleanup() {
        $("#springygraph").empty();
        $("#scatter").empty();
        
        $("#businessTransactions TBODY").empty();
        $("#businessTransactionsDetail TBODY").empty();
        
        $('#chartTabs a:first').tab('show');
        $('#transactionTabs a:first').tab('show');
        
        $("#springygraph").css("display", "none");
	}
	
	// TODO 조회 버튼을 두 번 연속 눌렀을 때 앞서 수행되던 결과는 취소시켜야 함.
	// 예를 들어 1일치 조회하다가 모두 조회 되기 전에 30분 데이터 조회할 때...
    function updateCharts() {
    	if($("#application").val() == "") {
    		alert("\n\nSelect an application, please.\n\n");
    		return true;
    	}

    	cleanup();
    	showIndicator();
    	
    	var serverMapCallback = function(data) {
        	if (data.graphdata.nodes.length == 0) {
	        	hideIndicator();
        	}
	        drawSpringy(data.graphdata, "#springygraph", 1100, 500);
        	hideIndicator();
        	
        	$("#springygraph").css("display", "");
        };
    	
    	var businessTransactionCallback = function(data) {
           	showTransactionList(data.businessTransactions);
    	};
    	
        var scatterCallback = function(data) {
        	// 처음 조회된 데이터를 그려준다.
	        updateScatter(getQueryStartTime(), getQueryEndTime(), data.scatter, "#scatter");
	        
	        if (data.scatter.length == 0) {
	        	return;
	        }
	        
	        // 데이터 조회가 추가로 필요한지 확인한다.
	        var lastTimeStamp = data.scatter[data.scatter.length - 1].timestamp;
	        
	        if (lastTimeStamp >= getQueryEndTime()) {
	        	return;
	        }
	        
	        var queryNext = true;
	        
	        var fetch = function() {
        		console.log("fetch scatter data");
        		clearInterval(scatterFetchTimer);
        		
				if(!queryNext || lastTimeStamp >= getQueryEndTime()) {
			        scatter.hideProgressbar();
					console.log("fetching scatter data finished.");
					return;
				}
				
	        	try {
	        		scatter.showProgressbar(lastTimeStamp + 1, getQueryEndTime());
	        		console.log("fetching scatter data.");
	        		
		        	getScatterData($("#application").val(), lastTimeStamp + 1, getQueryEndTime(), function(data2) {
		        		console.log("fetched " + data2.scatter.length);
		        		scatter.hideProgressbar();
		    	        if (data2.scatter.length == 0) {
		    	        	queryNext = false;
		    	        	return;
		    	        }
				        updateScatter(getQueryStartTime(), getQueryEndTime(), data2.scatter, "#scatter");
		        		lastTimeStamp = data2.scatter[data2.scatter.length - 1].timestamp;
		        		scatterFetchTimer = setInterval(fetch, 200);
		        	});
	        	} catch(e) {
	        		console.log(e);
	        	}
	        }
	        var scatterFetchTimer = setInterval(fetch, 200);
        };
    	
        if (isQueryFromNow()) {
	        getLastServerMapData2($("#application").val(), getQueryPeriod(), serverMapCallback);
	        // getLastServerMapData($("#application").val(), getQueryPeriod(), serverMapCallback);
	        //getLastBusinessTransactionsData($("#application").val(), getQueryPeriod(), businessTransactionCallback);
	        
	        //drawScatter($("#application").val(), getQueryStartTime(), getQueryEndTime(), "#scatter");
	        //getLastScatterData($("#application").val(), getQueryPeriod(), scatterCallback);
        } else {
	        getServerMapData2($("#application").val(), getQueryStartTime(), getQueryEndTime(), serverMapCallback);
	        // getServerMapData($("#application").val(), getQueryStartTime(), getQueryEndTime(), serverMapCallback);
	        //getBusinessTransactionsData($("#application").val(), getQueryStartTime(), getQueryEndTime(), businessTransactionCallback);
	        
	        //drawScatter($("#application").val(), getQueryStartTime(), getQueryEndTime(), "#scatter");
	        //getScatterData($("#application").val(), getQueryStartTime(), getQueryEndTime(), scatterCallback);
        }
    }
    
	$.getJSON("/applications.hippo", function(json) {
        var target = $("#application");
		target.empty();
		$(json).each(function(i, e) {
            var html = [];
            html.push('<option value="');
            html.push(e.applicationName);
            html.push("@");
            html.push(e.code);
            html.push('"');
            if (json.length == 1) {
           		html.push(' selected>');
            } else {
           		html.push('>');
            }
            html.push(e.applicationName + "@" + e.serviceType);
            html.push('</option>');
            
            target.append(html.join(''));
            
            $("#application").attr("disabled", null);
            
            function format(state) {
            	var chunk = state.text.split("@");
            	if (chunk.length > 1) {
                	return "<img class='flag' src='/images/icons/" + chunk[1] + ".png'/> " + chunk[0];
            	} else {
            		return state;
            	}
            }
            
		    $("#application").select2({
				placeholder: "Select an application.",
				formatResult: format,
				formatSelection: format
			});
		});
	});
	
	$("#selectedBusinessTransactionsDetailPager li").click(function() {
		alert("[NOT IMPLEMENTED]\n\nNOTE : 한꺼번에 많은 점을 선택했을 때 다 보여주지 않고.. 페이징?? 스크롤 페이징을 사용해도 될 것 같기도 하고...");
	});
	
	$("#application").bind("change", function() {
		updateCharts();
	});
	
	cleanup();
});
</script>
<script id="EdgeBox" type="text/x-jquery-tmpl">
	<div class="EdgeBox" style="position:absolute;border:1px solid #000;background:#fff;padding:5px 10px;-webkit-border-radius: 5px; ">
			<br/><a href="#" onclick="alert('Sorry. Not implemented.');">Focus on passing transactions</a>
			<br/><a href="#" onclick="alert('Sorry. Not implemented.');">Business transactions</a>
			<hr/>
			Response statistics
			<ul>
				{{each(key, value) histogram}}
				<li>&lt; ${key}ms : ${value}</li>
				{{/each}}
				<li>Slow : ${slow}</li>
				<li>Failed : ${error}</li>
			</ul>
		<button style="position:absolute;top:2px;right:2px;" onClick="$(this).parent().remove()">X</button>
	</div>
</script>
<script id="ServerBox" type="text/x-jquery-tmpl">
	<div class="ServerBox" style="position:absolute;border:1px solid #000;background:#fff;padding:5px 10px;-webkit-border-radius: 5px; ">
			<br/><a href="#" onclick="alert('Sorry. Not implemented.');">Focus on passing transactions</a>
			<br/><a href="#" onclick="alert('Sorry. Not implemented.');">Response scatter chart</a>
			<br/><a href="#" onclick="alert('Sorry. Not implemented.');">Show Requests</a>
			<hr/>
			Application Type
				<ul>
					<li>${serviceType}</li>
				</ul>
			Hosts
				<ul>
					{{each(key, value) hosts}}
						<li>
							${value}
							{{if serviceType == "TOMCAT"}}
							<a href="http://nsight.nhncorp.com/dashboard_server/${value.replace('.nhnsystem.com','')}" target="_blank">(NSight)</a>
							{{/if}}
						</li>
					{{/each}}
				</ul>
			Server instances
				<ul>
					<li>Not implemented.</li>
				</ul>
		<button style="position:absolute;top:2px;right:2px;" onClick="$(this).parent().remove()">X</button>
	</div>
</script>
<script id="ClientBox" type="text/x-jquery-tmpl">
	<div class="ServerBox" style="position:absolute;border:1px solid #000;background:#fff;padding:5px 10px;-webkit-border-radius: 5px; ">
			<br/><a href="#" onclick="alert('Sorry. Not implemented.');">Show Requests</a>
			<br/>Calls : Not implemented.
		<button style="position:absolute;top:2px;right:2px;" onClick="$(this).parent().remove()">X</button>
	</div>
</script>
</body>
</html>