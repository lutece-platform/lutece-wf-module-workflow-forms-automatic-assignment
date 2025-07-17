<%@page import="fr.paris.lutece.plugins.workflow.web.WorkflowJspBean"%>
<%@ page errorPage="../../../../ErrorPage.jsp" %>
<jsp:include page="../../../../AdminHeader.jsp" />

${ automaticAssignmentJspBean.init( pageContext.request, WorkflowJspBean.RIGHT_MANAGE_WORKFLOW ) }
${ automaticAssignmentJspBean.getModifyQuestionAssignments( pageContext.request ) }
<%@ include file="../../../../AdminFooter.jsp" %>