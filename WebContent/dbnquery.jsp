<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.profiler.data.store.hbase.get2.GetDatabaseData" %>    
<%@ page import="com.profiler.data.store.hbase.thrift.Thrift2ClientManager" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link href="css/hippocommon.css" rel="stylesheet" type="text/css"/>
<title>Hippo Data Fetch</title>
<script>
function updateServer(agentName,index) {
	//window.alert(agentName);
	var instanceName=eval("document.mainForm.instanceName"+index);
	var serviceName=eval("document.mainForm.serviceName"+index);
	var serverGroupName=eval("document.mainForm.serverGroupName"+index);
	window.alert("agentName="+agentName+"\ninstanceName="+instanceName.value+"\nserviceName="+serviceName.value+"\nserverGroupName="+serverGroupName.value+"\nYou must implements this function. kk");
}
</script>
</head>
<body>
<input type=button value="Refresh" onclick="javascript:location.reload(true)"><BR>
<form name=mainForm>
<%
	GetDatabaseData get=new GetDatabaseData();
	out.println(get.get());
%>
</form>
<BR><BR><HR>
<a href="index.jsp">Home</a>
</body>
</html>