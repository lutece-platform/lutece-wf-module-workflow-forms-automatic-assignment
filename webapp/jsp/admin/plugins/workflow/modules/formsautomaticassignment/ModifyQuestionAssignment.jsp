<%@page import="fr.paris.lutece.plugins.workflow.web.WorkflowJspBean"%>
<%@ page errorPage="../../../../ErrorPage.jsp" %>

${ automaticAssignmentJspBean.init( pageContext.request, WorkflowJspBean.RIGHT_MANAGE_WORKFLOW ) }
${ pageContext.setAttribute( 'strContent', automaticAssignmentJspBean.getModifyQuestionAssignments( pageContext.request, pageContext.response ) ) }

<jsp:include page="../../../../AdminHeader.jsp" />

${ pageContext.getAttribute( 'strContent' ) }
<%@ include file="../../../../AdminFooter.jsp" %>