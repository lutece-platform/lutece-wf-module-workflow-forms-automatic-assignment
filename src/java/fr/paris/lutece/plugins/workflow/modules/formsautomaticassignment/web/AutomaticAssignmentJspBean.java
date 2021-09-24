/*
 * Copyright (c) 2002-2021, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.workflow.modules.formsautomaticassignment.web;

import fr.paris.lutece.plugins.forms.business.Question;
import fr.paris.lutece.plugins.forms.business.QuestionHome;
import fr.paris.lutece.plugins.genericattributes.business.Field;
import fr.paris.lutece.plugins.genericattributes.business.FieldHome;
import fr.paris.lutece.plugins.workflow.modules.formsautomaticassignment.business.AutomaticAssignment;
import fr.paris.lutece.plugins.workflow.modules.formsautomaticassignment.business.TaskAutomaticAssignmentConfig;
import fr.paris.lutece.plugins.workflow.modules.formsautomaticassignment.service.AutomaticAssignmentPlugin;
import fr.paris.lutece.plugins.workflow.modules.formsautomaticassignment.service.AutomaticAssignmentService;
import fr.paris.lutece.plugins.workflow.modules.formsautomaticassignment.service.IAutomaticAssignmentService;
import fr.paris.lutece.plugins.workflow.modules.formsautomaticassignment.service.TaskAutomaticAssignmentConfigService;
import fr.paris.lutece.plugins.workflowcore.service.config.ITaskConfigService;
import fr.paris.lutece.portal.business.workgroup.AdminWorkgroup;
import fr.paris.lutece.portal.business.workgroup.AdminWorkgroupHome;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.web.admin.PluginAdminPageJspBean;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.url.UrlItem;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * AutomaticAssignmentJspBean
 *
 */
public class AutomaticAssignmentJspBean extends PluginAdminPageJspBean
{
    private static final long serialVersionUID = -8429549377694294789L;
    private static final String TEMPLATE_MODIFY_QUESTION_ASSIGNMENT = "admin/plugins/workflow/modules/formsautomaticassignment/modify_question_assignment.html";
    private static final String JSP_MODIFY_TASK = "jsp/admin/plugins/workflow/ModifyTask.jsp";
    private static final String JSP_MODIFY_QUESTION_ASSIGNMENT = "jsp/admin/plugins/workflow/modules/formsautomaticassignment/ModifyQuestionAssignment.jsp";
    private static final String PROPERTY_MODIFY_TASK_PAGE_TITLE = "workflow.modify_workflow.page_title";
    private static final String PARAMETER_ID_TASK = "id_task";
    private static final String PARAMETER_ID_QUESTION = "id_question";
    private static final String PARAMETER_QUESTION = "question";
    private static final String PARAMETER_URL = "url";
    private static final String PARAMETER_WORKGROUP_LIST = "workgroup_list";
    private static final String PARAMETER_WORKGROUP = "workgroup";
    private static final String PARAMETER_VALUE = "value";
    private static final String PARAMETER_ASSIGNMENT_LIST = "assignment_list";
    private static final String PARAMETER_ID_FORM = "id_form";
    private static final String MESSAGE_ALREADY_EXIST = "module.workflow.formsautomaticassignment.message.modify_entry_assignment.already_exist";
    private static final String MESSAGE_ERROR_MISSING_FIELD = "module.workflow.formsautomaticassignment.message.missing_field";

    // SERVICES
    private IAutomaticAssignmentService _automaticAssignmentService = SpringContextService.getBean( AutomaticAssignmentService.BEAN_SERVICE );
    private ITaskConfigService _taskAutomaticAssignmentConfigService = SpringContextService.getBean( TaskAutomaticAssignmentConfigService.BEAN_SERVICE );

    /**
     * Get the modify entry assignment page which allow the user to assign a workgroup to a field
     * 
     * @param request
     *            the request
     * @return the page
     */
    public String getModifyQuestionAssignments( HttpServletRequest request )
    {
        String strIdTask = request.getParameter( PARAMETER_ID_TASK );
        String strIdQuestion = request.getParameter( PARAMETER_ID_QUESTION );
        List<AutomaticAssignment> assignmentList;
        Plugin autoAssignPlugin = PluginService.getPlugin( AutomaticAssignmentPlugin.PLUGIN_NAME );

        HashMap<String, Object> model = new HashMap<>( );
        int nIdTask = -1;
        int nIdQuestion = -1;

        if ( strIdTask != null )
        {
            nIdTask = Integer.parseInt( strIdTask );
        }

        if ( strIdQuestion != null )
        {
            nIdQuestion = Integer.parseInt( strIdQuestion );
        }

        assignmentList = _automaticAssignmentService.findByTaskByQuestion( nIdTask, nIdQuestion, autoAssignPlugin );
        setAssignmentValues( assignmentList );

        Question question = QuestionHome.findByPrimaryKey( nIdQuestion );

        model.put( PARAMETER_QUESTION, question );

        UrlItem url = new UrlItem( JSP_MODIFY_TASK );
        url.addParameter( PARAMETER_ID_TASK, nIdTask );
        setPageTitleProperty( PROPERTY_MODIFY_TASK_PAGE_TITLE );
        model.put( PARAMETER_URL, url.getUrl( ) );
        model.put( PARAMETER_ID_TASK, nIdTask );
        model.put( PARAMETER_WORKGROUP_LIST, AdminWorkgroupHome.findAll( ) );
        model.put( PARAMETER_ASSIGNMENT_LIST, assignmentList );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MODIFY_QUESTION_ASSIGNMENT, getLocale( ), model );

        return getAdminPage( template.getHtml( ) );
    }

    /**
     * Fill a list of assignment with workgroupdescription and field value for displaying purpose
     * 
     * @param assignmentList
     *            a list of AutomaticAssignment
     * @param directoryPlugin
     *            the directory plugin
     */
    private void setAssignmentValues( List<AutomaticAssignment> assignmentList )
    {
        for ( AutomaticAssignment assignment : assignmentList )
        {
            AdminWorkgroup workgroup = AdminWorkgroupHome.findByPrimaryKey( assignment.getWorkgroupKey( ) );

            if ( workgroup != null )
            {
                assignment.setWorkgroupDescription( workgroup.getDescription( ) );
            }

            Field field = FieldHome.findByPrimaryKey( assignment.getIdField( ) );
            assignment.setFieldValue( field.getTitle( ) );
        }
    }

    /**
     * Assign the selected workgroup to the field
     * 
     * @param request
     *            the request
     * @return the modify assignment page url
     */
    public String doAddAssignment( HttpServletRequest request )
    {
        String strIdTask = request.getParameter( PARAMETER_ID_TASK );
        String strIdQuestion = request.getParameter( PARAMETER_ID_QUESTION );
        String strWorkgroup = request.getParameter( PARAMETER_WORKGROUP );
        String strValue = request.getParameter( PARAMETER_VALUE );
        Plugin autoAssignPlugin = PluginService.getPlugin( AutomaticAssignmentPlugin.PLUGIN_NAME );

        int nIdTask = -1;
        int nIdQuestion = -1;
        int nIdField = -1;

        if ( strIdTask != null )
        {
            nIdTask = Integer.parseInt( strIdTask );
        }

        if ( strIdQuestion != null )
        {
            nIdQuestion = Integer.parseInt( strIdQuestion );
        }

        if ( strValue != null )
        {
            nIdField = Integer.parseInt( strValue );
        }

        if ( ( strValue == null ) || ( strWorkgroup == null ) )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_ERROR_MISSING_FIELD, AdminMessage.TYPE_STOP );
        }

        AutomaticAssignment assign = new AutomaticAssignment( );
        assign.setIdQuestion( nIdQuestion );
        assign.setIdTask( nIdTask );
        assign.setIdField( nIdField );
        assign.setWorkgroupKey( strWorkgroup );

        if ( !_automaticAssignmentService.checkExist( assign, autoAssignPlugin ) )
        {
            _automaticAssignmentService.create( assign, autoAssignPlugin );
        }
        else
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_ALREADY_EXIST, AdminMessage.TYPE_STOP );
        }

        UrlItem url = new UrlItem( AppPathService.getBaseUrl( request ) + JSP_MODIFY_QUESTION_ASSIGNMENT );

        url.addParameter( PARAMETER_ID_TASK, nIdTask );
        url.addParameter( PARAMETER_ID_QUESTION, nIdQuestion );

        return url.getUrl( );
    }

    /**
     * Delete an assignment
     * 
     * @param request
     *            the request
     * @return the modify assignment page url
     */
    public String doDeleteAssignment( HttpServletRequest request )
    {
        Plugin autoAssignPlugin = PluginService.getPlugin( AutomaticAssignmentPlugin.PLUGIN_NAME );
        AutomaticAssignment assignment = getAutomaticAssignment( request );
        _automaticAssignmentService.remove( assignment, autoAssignPlugin );

        UrlItem url = new UrlItem( AppPathService.getBaseUrl( request ) + JSP_MODIFY_QUESTION_ASSIGNMENT );

        url.addParameter( PARAMETER_ID_TASK, assignment.getIdTask( ) );
        url.addParameter( PARAMETER_ID_QUESTION, assignment.getIdQuestion( ) );

        return url.getUrl( );
    }

    /**
     * Use request parameters to create a AutomaticAssignment object
     * 
     * @param request
     *            the request
     * @return the AutomaticAssignment created
     */
    private AutomaticAssignment getAutomaticAssignment( HttpServletRequest request )
    {
        String strIdTask = request.getParameter( PARAMETER_ID_TASK );
        String strIdQuestion = request.getParameter( PARAMETER_ID_QUESTION );
        String strWorkgroup = request.getParameter( PARAMETER_WORKGROUP );
        String strValue = request.getParameter( PARAMETER_VALUE );

        int nIdTask = -1;
        int nIdQuestion = -1;
        int nIdField = -1;

        if ( strIdTask != null )
        {
            nIdTask = Integer.parseInt( strIdTask );
        }

        if ( strIdQuestion != null )
        {
            nIdQuestion = Integer.parseInt( strIdQuestion );
        }

        if ( strValue != null )
        {
            nIdField = Integer.parseInt( strValue );
        }

        AutomaticAssignment assign = new AutomaticAssignment( );

        assign.setIdQuestion( nIdQuestion );
        assign.setIdTask( nIdTask );
        assign.setIdField( nIdField );
        assign.setWorkgroupKey( strWorkgroup );

        return assign;
    }

    /**
     * Update the directory selected and reload the page
     * 
     * @param request
     *            the request
     * @return the modify autoassignment config page
     */
    public String doUpdateForm( HttpServletRequest request )
    {
        String strIdForm = request.getParameter( PARAMETER_ID_FORM );
        String strIdTask = request.getParameter( PARAMETER_ID_TASK );
        int nIdForm = -1;
        int nIdTask = -1;
        Plugin autoAssignPlugin = PluginService.getPlugin( AutomaticAssignmentPlugin.PLUGIN_NAME );

        if ( strIdForm != null )
        {
            nIdForm = Integer.parseInt( strIdForm );
        }

        if ( strIdTask != null )
        {
            nIdTask = Integer.parseInt( strIdTask );
        }

        TaskAutomaticAssignmentConfig config = _taskAutomaticAssignmentConfigService.findByPrimaryKey( nIdTask );
        config.setIdForm( nIdForm );

        if ( nIdForm != -1 )
        {
            _taskAutomaticAssignmentConfigService.update( config );
            _automaticAssignmentService.removeByTask( nIdTask, autoAssignPlugin );
        }

        UrlItem url = new UrlItem( AppPathService.getBaseUrl( request ) + JSP_MODIFY_TASK );
        url.addParameter( PARAMETER_ID_TASK, nIdTask );

        return url.getUrl( );
    }
}
