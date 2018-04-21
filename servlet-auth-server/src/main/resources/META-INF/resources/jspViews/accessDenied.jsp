<%--
  Created by IntelliJ IDEA.
  User: Lenovo
  Date: 02/07/2016
  Time: 10:53 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>AccessDenied page</title>
</head>
<body>
Dear <strong>${user}</strong>, You are not authorized to access this page
<a href="<c:url value="/logout" />">Logout</a>
</body>
</html>
