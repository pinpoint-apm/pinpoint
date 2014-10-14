<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %> 
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>User Group</title>

<SCRIPT type="text/javascript">

	var groupSize = ${fn:length(groupMember)};

	// add input form when clicked add button
	function append_member_input() {
		groupSize++;
		var table = document.getElementById("empGroup");
		var row = table.insertRow(table.rows.length-2);
		row.insertCell(0).innerHTML = '<input name="choose" type="checkbox"/>';
		row.insertCell(1).innerHTML = '<input type="hidden" name=emps[' + groupSize + '].id value="0"/><input name=emps[' + groupSize + '].groupName type="text"/>';
		row.insertCell(2).innerHTML = '<input name=emps[' + groupSize + '].empId type="text" />';
	}
	
	//delete input form when clicked delete button
	function remove_member_input() {
		var table = document.getElementById("empGroup");
		var choose = document.getElementsByName("choose");
		
		for (var i=0; i<choose.length; i++){
			if (choose[i].checked){
				if (choose.length > 1) {
			    	table.deleteRow(i + 3);
					i--;
				}
			}
		}
	}
	
	function onSubmitForm()
	{
		if(document.pressed == 'insert') {
 			document.empGroupForm.action ="./insertMember.pinpoint";
		} else if(document.pressed == 'update') {
			document.empGroupForm.action ="./updateMember.pinpoint";
		} else if(document.pressed == 'delete') {
			document.empGroupForm.action ="./deleteMember.pinpoint";
		}

		return true;
	}

</SCRIPT>

</head>
<body>
<!-- 화면 이름 -->
<center>
	<h1>사용자 그룹 설정</h1>
</center>

<!-- 사원 그룹 검색 -->
<center>
	<form action="./getMember.pinpoint">
		<select name="groupName">
			<c:forEach var="groupName" items="${groupNameList}">
				<option value="${groupName}">${groupName}</option>
			</c:forEach>
		</select>
		<button value="검색">검색</button>
	</form>
	</br>
</center>

<!-- 사원 리스트 출력 -->
<center>
	<form name="empGroupForm" method="post" onsubmit="return onSubmitForm();">
		<table id="empGroup" frame="void" border="1">
			<!-- 사원 추가 -->
			<tr bordercolor="white">
					<td align="left" colspan="3">
						<button type="button" onclick='append_member_input()'>행추가</button>
						<button type="button" onclick='remove_member_input()'>행삭제</button>
					</td>
			</tr>
			<tr bordercolor="white">
			</tr>
						
			<!-- 사원 정보 리스트 출력 -->
			<tr>
				<th>삭제</th>
				<th>그룹 이름</th>
				<th>사용자 아이디</th>
			</tr>
			<c:forEach var="emp" items="${groupMember}" varStatus="empIndex">
					<tr>
						<td><input name="choose" type="checkbox"/></td>
	 					<td><input type="hidden" name="emps[${empIndex.index}].id" value="${emp.id}"/><input type="text" name="emps[${empIndex.index}].groupName" value="${emp.groupName}"/></td>
						<td><input type="text" name="emps[${empIndex.index}].empId" value="${emp.empId}"/></td>
					</tr>
			</c:forEach>
			<!-- 사원 정보 등록 & 수정 -->
			
			
			<tr bordercolor="white">
			</tr>
			<tr bordercolor="white">
				<td align="right" colspan="3">
					<button onclick="document.pressed=this.value" value="insert">신규등록</button>
					<button onclick="document.pressed=this.value" value="update">저장</button>
					<button onclick="document.pressed=this.value" value="delete">그룹삭제</button>
				</td>
			</tr>
		</table>
	</form>
</center>




</body>
</html>