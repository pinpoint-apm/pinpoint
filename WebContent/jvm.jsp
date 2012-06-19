<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.profiler.data.store.hbase.get2.GetJVMData" %>  
<%@ page import="com.profiler.config.TomcatProfilerReceiverConstant" %>
<%@ page import="java.util.List,java.util.Hashtable" %>    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Hippo Data Fetch</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link href="css/hippocommon.css" rel="stylesheet" type="text/css"/>
<link rel="stylesheet" href="css/website.css" type="text/css" media="screen" />
<!-- Place this tag in your head or just before your close body tag -->
    <script type="text/javascript" src="https://apis.google.com/js/plusone.js"></script>

    <script src="libraries/RGraph.common.core.js" ></script>
    <script src="libraries/RGraph.common.dynamic.js" ></script>
    <script src="libraries/RGraph.common.tooltips.js" ></script>
    <script src="libraries/RGraph.common.effects.js" ></script>
    <script src="libraries/RGraph.common.key.js" ></script>
    <script src="libraries/jquery.min.js" ></script>
    <script src="libraries/RGraph.line.js" ></script>
    <script src="libraries/RGraph.scatter.js" ></script>
    <script src="libraries/RGraph.bar.js" ></script>
<!--[if lt IE 9]><script src="../excanvas/excanvas.original.js"></script><![endif]-->

<script>
function changePeriod() {
	//window.alert("changePeriod");
	var selectedIndex=document.mainForm.period.selectedIndex;
	location.href="jvm.jsp?period="+selectedIndex;
}
</script>
</head>
<body>
<form name=mainForm>
<input type=button value="Refresh" onclick="javascript:location.reload(true)">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<select id=period onchange="javascript:changePeriod()">
<% 
	String periodParam=request.getParameter("period");
	int periodParamInt=0;
	if(periodParam!=null) {
		//System.out.println(periodParam);
		try {
			periodParamInt=Integer.parseInt(periodParam);
		} catch(Exception e) {}
	}
	String [] periods={"10min","30min","1hour","3hour","Today"};
	long [] periodLong={600000,1800000,3600000,10800000,-1};
	int periodsLength=periods.length;
	for(int loop=0;loop < periodsLength;loop++) {
		String selectedString="";
		if(periodParamInt==loop) selectedString="selected";
%>
	<option value="period" <%=selectedString %>><%=periods[loop] %></option>
<%
	}
%>
</select>
</form>
<BR>
<%
	GetJVMData get=new GetJVMData(System.currentTimeMillis(),periodLong[periodParamInt]);
	out.println(get.get());
	List<String> agentList=get.agentHashCodeList;
	List<String> agentStringNameList=get.agentStringNameList;
	int agentCount=agentList.size();
%>
<BR>
<script>
    window.onload = function ()
    {
<%
	for(int loop=0;loop<agentCount;loop++) {
		out.println("func"+loop+"();");
	}
%>
    }
</script>
<%
	Hashtable<String,StringBuilder> graphDataTable=get.graphDataTable;
	for(int loop=0;loop<agentCount;loop++) {
		String currentFunctionName="func"+loop;
		String agentHashCode=agentList.get(loop);
		
%>
<H3>Agent : <%=agentStringNameList.get(loop) %></H3>
<script>
	function <%=currentFunctionName%> (){
		var reqTPS = new RGraph.Line('reqTPS_<%=loop%>', 
    			[<%=graphDataTable.get(TomcatProfilerReceiverConstant.HBASE_TPS_COLUMN_REQUEST_TPS+agentHashCode)%>]);
		reqTPS.Set('chart.title','Request Per Seconds');
		reqTPS.Set('chart.linewidth', 2);
		reqTPS.Set('chart.hmargin', 5);
		reqTPS.Set('chart.xaxispos', 'bottom');
		reqTPS.Set('chart.shadow', true);
    	reqTPS.Set('chart.shadow.offsetx', 3);
    	reqTPS.Set('chart.shadow.offsety', 3);
    	reqTPS.Set('chart.colors', ['rgba(0,100,0,1)']);
    	reqTPS.Set('chart.background.grid.autofit.numhlines', 10);
    	reqTPS.Set('chart.background.barcolor1', 'rgba(250,250,210,0.5)');
    	reqTPS.Set('chart.background.barcolor2', 'rgba(250,250,210,0.5)');
        reqTPS.Draw();
        
        var resTPS = new RGraph.Line('resTPS_<%=loop%>', 
    			[<%=graphDataTable.get(TomcatProfilerReceiverConstant.HBASE_TPS_COLUMN_RESPONSE_TPS+agentHashCode)%>]);
        resTPS.Set('chart.title','Response Per Seconds');
        resTPS.Set('chart.linewidth', 2);
        resTPS.Set('chart.hmargin', 5);
        resTPS.Set('chart.xaxispos', 'bottom');
        resTPS.Set('chart.shadow', true);
		resTPS.Set('chart.shadow.offsetx', 3);
		resTPS.Set('chart.shadow.offsety', 3);
		resTPS.Set('chart.colors', ['rgba(138,43,226,1)']);
		resTPS.Set('chart.background.grid.autofit.numhlines', 10);
		resTPS.Set('chart.background.barcolor1', 'rgba(250,250,210,0.5)');
		resTPS.Set('chart.background.barcolor2', 'rgba(250,250,210,0.5)');
        resTPS.Draw();
		
		var activeThreadCount = new RGraph.Line('activeThreadCount_<%=loop%>', 
    			[<%=graphDataTable.get(TomcatProfilerReceiverConstant.HBASE_JVM_COLUMN_ACTIVE_THREAD_COUNT+agentHashCode)%>]);
        activeThreadCount.Set('chart.title','Active Threads');
    	activeThreadCount.Set('chart.linewidth', 2);
    	activeThreadCount.Set('chart.hmargin', 5);
    	activeThreadCount.Set('chart.xaxispos', 'bottom');
    	activeThreadCount.Set('chart.shadow', true);
    	activeThreadCount.Set('chart.shadow.offsetx', 3);
    	activeThreadCount.Set('chart.shadow.offsety', 3);
    	activeThreadCount.Set('chart.colors', ['blue']);
    	activeThreadCount.Set('chart.background.grid.autofit.numhlines', 10);
        activeThreadCount.Draw();
    	
        var heapUsed = new RGraph.Line('heapUsed_<%=loop%>', 
        		[<%=graphDataTable.get(TomcatProfilerReceiverConstant.HBASE_JVM_DATA_HEAP+agentHashCode)%>]);
        heapUsed.Set('chart.title','HeapUsed');
        heapUsed.Set('chart.linewidth', 2);
        heapUsed.Set('chart.hmargin', 5);
        heapUsed.Set('chart.xaxispos', 'bottom');
        heapUsed.Set('chart.shadow', true);
        heapUsed.Set('chart.shadow.offsetx', 3);
        heapUsed.Set('chart.shadow.offsety', 3);
        heapUsed.Set('chart.colors', ['blue']);
        heapUsed.Set('chart.background.grid.autofit.numhlines', 10);
        heapUsed.Set('chart.ylabels.inside',true);
        heapUsed.Draw();
        
        var processCPU = new RGraph.Line('processCPU_<%=loop%>', 
        		[<%=graphDataTable.get(TomcatProfilerReceiverConstant.HBASE_JVM_COLUMN_PROCESS_CPU_TIME+agentHashCode)%>]);
        processCPU.Set('chart.title','Process CPU');
        processCPU.Set('chart.linewidth', 2);
        processCPU.Set('chart.hmargin', 5);
        processCPU.Set('chart.xaxispos', 'bottom');
        processCPU.Set('chart.shadow', true);
        processCPU.Set('chart.shadow.offsetx', 3);
        processCPU.Set('chart.shadow.offsety', 3);
        processCPU.Set('chart.colors', ['blue']);
        processCPU.Set('chart.background.grid.autofit.numhlines', 10);
        //processCPU.Set('chart.ylabels.inside',true);
        processCPU.Draw();

        var gc1Time = new RGraph.Scatter('gc1Time_<%=loop%>', 
        	<%=graphDataTable.get(TomcatProfilerReceiverConstant.HBASE_JVM_COLUMN_GC1_TIME+agentHashCode)%>);
        gc1Time.Set('chart.title','Minor GC Time(ms)');
        gc1Time.Set('chart.tickmarks', 'circle');
        gc1Time.Set('chart.xscale', true );
        gc1Time.Set('chart.xaxispos', 'bottom');
        gc1Time.Set('chart.xscale.numlabels',0);
        gc1Time.Set('chart.background.barcolor1', 'rgba(240,255,255,0.5)');
        gc1Time.Set('chart.background.barcolor2', 'rgba(240,255,255,0.5)');
        gc1Time.Draw();
        
        var gc2Time = new RGraph.Scatter('gc2Time_<%=loop%>', 
        	<%=graphDataTable.get(TomcatProfilerReceiverConstant.HBASE_JVM_COLUMN_GC2_TIME+agentHashCode)%>);
        gc2Time.Set('chart.title','Full GC Time(ms)');
        gc2Time.Set('chart.tickmarks', 'circle');
        gc2Time.Set('chart.xscale', true );
        gc2Time.Set('chart.xaxispos', 'bottom');
        gc2Time.Set('chart.xscale.numlabels',0);
        gc2Time.Set('chart.background.barcolor1', 'rgba(240,255,255,0.5)');
        gc2Time.Set('chart.background.barcolor2', 'rgba(240,255,255,0.5)');
        gc2Time.Draw();
        
        var gcCounts = new RGraph.Bar('gcCounts_<%=loop%>', 
        		[[<%=graphDataTable.get(TomcatProfilerReceiverConstant.HBASE_JVM_DATA_GC_COUNT+agentHashCode)%>]]);
        gcCounts.Set('chart.title','Total GC Counts');
        gcCounts.Set('chart.key', ['Minor', 'Full']);
        //gcCounts.Set('chart.key.position', 'gutter');
        gcCounts.Set('chart.key.position.y',1);
        gcCounts.Set('chart.title.vpos',0.5);
        gcCounts.Set('chart.title.size',12);
        gcCounts.Set('chart.colors', ['#2A17B1', '#98ED00']); 
        gcCounts.Set('chart.numyticks', 5);
        gcCounts.Set('chart.ylabels.count', 5);
        gcCounts.Set('chart.gutter.left', 35);
        gcCounts.Set('chart.variant', '3d');
        gcCounts.Set('chart.strokestyle', 'rgba(0,0,0,0.1)');
        gcCounts.Set('chart.scale.round', true);
        gcCounts.Set('chart.labels.above',true);
        gcCounts.Set('chart.text.size',20);
        gcCounts.Set('chart.ylabels',false);
        gcCounts.Set('chart.background.barcolor1', 'rgba(240,255,255,0.5)');
        gcCounts.Set('chart.background.barcolor2', 'rgba(240,255,255,0.5)');
        gcCounts.Draw();
    }
</script>
<script>
    if (RGraph.isOld()) {
        document.write('<div style="background-color: #fee; border: 2px dashed red; padding: 5px"><b>Important</b><br /><br /> Internet Explorer does not natively support the HTML5 canvas tag, so if you want to see the charts, you can either:<ul><li>Install <a href="http://code.google.com/chrome/chromeframe/">Google Chrome Frame</a></li><li>Use ExCanvas. This is provided in the RGraph Archive.</li><li>Use another browser entirely. Your choices are Firefox 3.5+, Chrome 2+, Safari 4+ or Opera 10.5+. </li></ul> <b>Note:</b> Internet Explorer 9 fully supports the canvas tag.</div>');
    }
</script>
<BR>
<Table>
	<TR>
		<TD><canvas id="reqTPS_<%=loop%>" width="450" height="150">[No canvas support]</canvas></TD>
		<TD><canvas id="resTPS_<%=loop%>" width="450" height="150">[No canvas support]</canvas></TD>
	</TR>
</Table>
<Table>
	<TR>
		<TD><canvas id="activeThreadCount_<%=loop%>" width="300" height="150">[No canvas support]</canvas></TD>
		<TD><canvas id="heapUsed_<%=loop%>" width="300" height="150">[No canvas support]</canvas></TD>
		<TD><canvas id="processCPU_<%=loop%>" width="300" height="150">[No canvas support]</canvas></TD>
	</TR>
	<TR>
		<TD><canvas id="gcCounts_<%=loop%>" width="300" height="150">[No canvas support]</canvas></TD>
		<TD><canvas id="gc1Time_<%=loop%>" width="300" height="150">[No canvas support]</canvas></TD>
		<TD><canvas id="gc2Time_<%=loop%>" width="300" height="150">[No canvas support]</canvas></TD>
	</TR>
</Table>
<BR>
<HR>
<%
	} // end of Agent for loop
%>

<a href="index.jsp">Home</a>
</body>
</html>