<%--
documentr - Edit, maintain, and present software documentation on the web.
Copyright (C) 2012 Maik Schreiber

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>
<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="/WEB-INF/view/header.jsp"/>

<c:if test="${!empty requestScope._breadcrumbs}"><c:out value="${requestScope._breadcrumbs}" escapeXml="false"/></c:if>

<c:set var="pageContents"><jsp:doBody/></c:set>
<c:if test="${!empty pageContents}">
	<div class="container">
		<c:out value="${pageContents}" escapeXml="false"/>
	</div>
</c:if>

<jsp:include page="/WEB-INF/view/footer.jsp"/>
