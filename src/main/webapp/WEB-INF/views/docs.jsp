<%@ page session="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<title>Endpoint list</title>
</head>
<body>
	<table border="1">
		<thead>
			<tr>
				<th>Controller</th>
				<th>URL</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach items="${mapping}" var="item">
				<tr>
					<td>${item.key}</td>
					<td>
						<ul>
						<c:forEach items="${item.value}" var="value">
							<li><a href="${value}.pinpoint">${value}.pinpoint</a></li>
						</c:forEach>
						</ul>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</body>
</html>