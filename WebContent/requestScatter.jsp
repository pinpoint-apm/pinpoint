<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.profiler.data.store.hbase.get2.GetRequestData" %>  
<%@ page import="com.profiler.config.TomcatProfilerReceiverConstant" %>
<%@ page import="java.util.List,java.util.Hashtable,java.util.Set" %>    
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
    <script src="libraries/RGraph.scatter.js" ></script>
    <script src="libraries/RGraph.hbar.js" ></script>
<!--[if lt IE 9]><script src="excanvas/excanvas.original.js"></script><![endif]-->
<script> 
function requestClick(e,scatter) {
	var obj=scatter[0];
	var x=scatter[1];
	var y=scatter[2];
	var w=scatter[3];
	var h=scatter[4];
	var idx=scatter[5];
	window.alert("obj="+obj+" x="+x+" y="+y+" w="+w+" h="+h+" idx="+idx);
}
</script>
</head>
<body>
<input type=button value="Refresh" onclick="javascript:location.reload(true)"><BR>
<%
	GetRequestData get=new GetRequestData(System.currentTimeMillis(),10);
	out.println(get.get());
	List<String> agentList=get.agentNameList;
	int agentCount=agentList.size();
	//Hashtable<String,StringBuilder> graphDataTable=get.graphDataTable;
	long totalDataCount=get.totalDataCount;
%>
<BR><BR>
<script>
    window.onload = function ()
    {
    	var elapsedTimeGraph = new RGraph.Scatter('elapsedTimeGraph', <%=get.scatterChartData %>);
    	elapsedTimeGraph.Set('chart.title','Elapsed time scatter chart');
    	//cross, plus, circle, diamond, square
    	//elapsedTimeGraph.Set('chart.tickmarks', 'diamond');
<%
	if(totalDataCount>1024) {
		out.println("elapsedTimeGraph.Set('chart.tickmarks', 'plus');\n");
		out.println("elapsedTimeGraph.Set('chart.ticksize', 2);");
	} else {
		out.println("elapsedTimeGraph.Set('chart.tickmarks', 'diamond');\n");
		out.println("elapsedTimeGraph.Set('chart.ticksize', 5);");
	}
	StringBuilder zeroToOne=new StringBuilder();
	for(int loop=0;loop<10;loop++) {
		zeroToOne.append(get.zeroToOne[loop]).append(",");
	}
	zeroToOne.deleteCharAt(zeroToOne.length()-1);
%>
    	elapsedTimeGraph.Set('chart.xscale', true );
    	elapsedTimeGraph.Set('chart.xaxispos', 'bottom');
    	elapsedTimeGraph.Set('chart.xscale.numlabels',0);
    	elapsedTimeGraph.Set('chart.gutter.left', 60);
    	elapsedTimeGraph.Set('chart.background.barcolor1', 'rgba(240,255,255,0.5)');
    	elapsedTimeGraph.Set('chart.background.barcolor2', 'rgba(240,255,255,0.5)');
    	//elapsedTimeGraph.Set('chart.events.click', requestClick); // The myClick function is the one above
    	elapsedTimeGraph.Draw();
    	
        var hbar = new RGraph.HBar('zeroToOneDistrubution', [<%=zeroToOne%>]);
        hbar.Set('chart.title', '0~1 seconds Distribution');
        hbar.Set('chart.labels', ['900~999','800~899','700~799','600~699','500~599','400~499',
                                  '300~399','200~299','100~199','0~99']);
        hbar.Set('chart.colors', ['rgba(20,20,20,1)','rgba(40,40,40,1)','rgba(60,60,60,1)','rgba(80,80,80,1)','rgba(100,100,100,1)',
                                   'rgba(120,120,120,1)','rgba(140,140,140,1)','rgba(160,160,160,1)','rgba(180,180,180,1)','rgba(200,200,200,1)',]);
        hbar.Set('chart.colors.sequential',true);
        hbar.Set('chart.strokestyle', 'rgba(0,0,0,0)');
        hbar.Set('chart.labels.above', true);
        hbar.Set('chart.labels.above', true);
        hbar.Set('chart.vmargin', 35);
        hbar.Set('chart.background.grid', false);
		//hbar.Set('chart.tooltips', ['Government of Haiti, multilateral<br />agencies NHOs and private contractors','Budget support to government','Haiti Reconstruction fund','Loans to goverment']);
        //hbar.Set('chart.labels.above.decimals', 1);
        hbar.Set('chart.xlabels', false);
        hbar.Set('chart.gutter.left', 80);
        hbar.Set('chart.gutter.right', 50);
        hbar.Set('chart.gutter.top', 25);
        hbar.Set('chart.noxaxis', true);
        hbar.Set('chart.noxtickmarks', true);
        hbar.Set('chart.noytickmarks', true);
        RGraph.isOld() ? hbar.Draw() : RGraph.Effects.HBar.Grow(hbar);
    }
</script>
<TABLE>
<TR>
	<TD><canvas id="elapsedTimeGraph" width="1024" height="400">[No canvas support]</canvas></TD>
	<TD><canvas id="zeroToOneDistrubution" width="300" height="400">[No canvas support]</canvas></TD>
</TR>
</TABLE>
<BR>
<%
	Hashtable<String,StringBuilder> summaryStatisticsData=get.summaryStatisticsData;
	if(summaryStatisticsData.size()!=0) {
%>
	<TABLE border=1>
		<TR>
			<TD>URL</TD><TD>Reqeust count</TD><TD>Min(ms)</TD><TD>Max(ms)</TD><TD>Total time(sec)</TD><TD>Average(ms)</TD><TD>90th Percentile(ms)</TD>
		</TR>
<%
		Set<String> keySet=summaryStatisticsData.keySet();
		for(String key:keySet) {
			out.println(summaryStatisticsData.get(key));
		}
%>	
	</TABLE>
<%
	}
%>
<BR><BR>
	<TABLE border=1 width='100%'> 
		<TR>
<%
	List<CharSequence> slowRequestDataList=get.slowRequestDataList;
	int listSize=slowRequestDataList.size();
	for(int loop=0;loop<listSize;loop++) {
		if(loop!=0 && loop%2==0) {
			out.println("</TR><TR>");
		}
		out.println("<TD width='50%'>"+slowRequestDataList.get(loop)+"</TD>");
	}
%>
		</TR>
	</TABLE>
<HR>
<a href="index.jsp">Home</a>
</body>
</html>