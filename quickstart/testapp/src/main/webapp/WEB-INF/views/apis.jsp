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
                        <dl>
                            <c:forEach items="${apiMapping.value}" var="api">
                            <dt><a href="${api.mappedUri}.pinpoint">${api.mappedUri}</a></dt>
                            <dd><small>${api.description}</small></dd>
                            </c:forEach>
                        </dl>
                    </div>
                </div>
            </div>
            </c:forEach>
        </div>
    </div>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.1/js/bootstrap.min.js"></script>
</body>
</html>