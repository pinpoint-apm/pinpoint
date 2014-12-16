<%@ page session="false" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<title>Apis</title>
<link href="css/bootstrap.min.css" rel="stylesheet">
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
     <script src="js/bootstrap.min.js"></script>
</body>
</html>