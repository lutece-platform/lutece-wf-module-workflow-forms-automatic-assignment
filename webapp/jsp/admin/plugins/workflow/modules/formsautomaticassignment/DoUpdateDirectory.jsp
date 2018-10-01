<%@ page errorPage="../../../../ErrorPage.jsp" %>
<jsp:useBean id="workflowAutomaticAssignment" scope="session" class="fr.paris.lutece.plugins.workflow.modules.formsautomaticassignment.web.AutomaticAssignmentJspBean" />
<%
    workflowAutomaticAssignment.init( request, fr.paris.lutece.plugins.workflow.web.WorkflowJspBean.RIGHT_MANAGE_WORKFLOW); 
	response.sendRedirect( workflowAutomaticAssignment.doUpdateForm(request) );
%>
