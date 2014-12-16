<%@ page session="false" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<title>Apis</title>
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.1/css/bootstrap.min.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.1/css/bootstrap-theme.min.css">
</head>
<body>
    <div class="container-fluid">
        <div class="row">
            <c:forEach items="${apiMappings}" var="apiMapping">
            <div class="col-sm-6 col-md-4">
                <div class="thumbnail">
                    <div class="caption">
                        <h5>${apiMapping.key}</h5>
                        <ul class="list-unstyled">
                            <c:forEach items="${apiMapping.value}" var="api">
                            <li><a href="${api}.pinpoint">${api}</a></li>
                            </c:forEach>
                        </ul>
                    </div>
                </div>
            </div>
            </c:forEach>
        </div>
    </div>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.1/js/bootstrap.min.js"></script>
</body>
</html>