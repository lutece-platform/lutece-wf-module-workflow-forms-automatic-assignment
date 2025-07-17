<%@page import="fr.paris.lutece.plugins.workflow.web.WorkflowJspBean"%>
<%@ page errorPage="../../../../ErrorPage.jsp" %>

${ automaticAssignmentJspBean.init( pageContext.request, WorkflowJspBean.RIGHT_MANAGE_WORKFLOW ) }
${ pageContext.response.sendRedirect( automaticAssignmentJspBean.doUpdateForm( pageContext.request ) ) }
