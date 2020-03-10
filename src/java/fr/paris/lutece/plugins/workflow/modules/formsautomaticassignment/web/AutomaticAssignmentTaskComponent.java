/*
 * Copyright (c) 2002-2020, City of Paris
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import fr.paris.lutece.plugins.forms.business.FormHome;
import fr.paris.lutece.plugins.forms.business.Question;
import fr.paris.lutece.plugins.workflow.modules.assignment.business.AssignmentHistory;
import fr.paris.lutece.plugins.workflow.modules.assignment.business.WorkgroupConfig;
import fr.paris.lutece.plugins.workflow.modules.assignment.service.IAssignmentHistoryService;
import fr.paris.lutece.plugins.workflow.modules.formsautomaticassignment.business.TaskAutomaticAssignmentConfig;
import fr.paris.lutece.plugins.workflow.modules.formsautomaticassignment.service.IAutomaticAssignmentService;
import fr.paris.lutece.plugins.workflow.modules.formsautomaticassignment.service.TaskAutomaticAssignmentConfigService;
import fr.paris.lutece.plugins.workflow.utils.WorkflowUtils;
import fr.paris.lutece.plugins.workflow.web.task.NoFormTaskComponent;
import fr.paris.lutece.plugins.workflowcore.service.config.ITaskConfigService;
import fr.paris.lutece.plugins.workflowcore.service.task.ITask;
import fr.paris.lutece.portal.business.mailinglist.MailingList;
import fr.paris.lutece.portal.business.mailinglist.MailingListHome;
import fr.paris.lutece.portal.business.workgroup.AdminWorkgroup;
import fr.paris.lutece.portal.business.workgroup.AdminWorkgroupHome;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.mailinglist.AdminMailingListService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.service.workgroup.AdminWorkgroupService;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.url.UrlItem;
import fr.paris.lutece.util.xml.XmlUtil;

/**
 *
 * AutomaticAssignmentTaskComponent
 *
 */
public class AutomaticAssignmentTaskComponent extends NoFormTaskComponent
{
    // Templates
    private static final String TEMPLATE_TASK_AUTO_ASSIGNMENT_CONFIG = "admin/plugins/workflow/modules/formsautomaticassignment/task_config.html";
    private static final String TEMPLATE_TASK_AUTO_ASSIGNMENT_INFORMATION = "admin/plugins/workflow/modules/formsautomaticassignment/task_assignment_information.html";

    // Markers
    private static final String MARK_CONFIG = "config";
    private static final String MARK_WORKGROUP_LIST = "workgroup_list";
    private static final String MARK_ITEM = "item";
    private static final String MARK_MAILING_LIST = "mailing_list";
    private static final String MARK_FORM_LIST = "form_list";
    private static final String MARK_ALL_QUESTION_LIST = "full_question_list";
    private static final String MARK_AUTHORIZED_QUESTION_LIST = "authorized_question_list";
    private static final String MARK_QUESTION_LIST_TYPES_FILE = "question_types_file_list";

    private static final String MARK_WEBAPP_URL = "webapp_url";
    private static final String MARK_LOCALE = "locale";

    // Parameters
    private static final String PARAMETER_TITLE = "title";
    private static final String PARAMETER_WORKGROUPS = "workgroups";
    private static final String PARAMETER_ID_MAILING_LIST = "id_mailing_list";
    private static final String PARAMETER_MESSAGE = "message";
    private static final String PARAMETER_IS_NOTIFICATION = "is_notify";
    private static final String PARAMETER_SUBJECT = "subject";
    private static final String PARAMETER_SENDER_NAME = "sender_name";
    private static final String PARAMETER_FORM = "form";
    private static final String PARAMETER_ID_FORM = "id_form";
    private static final String PARAMETER_ID_TASK = "id_task";
    private static final String PARAMETER_view_form_response = "view_form_response";
    private static final String PARAMETER_LABEL_LINK_view_form_response = "label_link_view_form_response";
    private static final String PARAMETER_RECIPIENTS_CC = "recipients_cc";
    private static final String PARAMETER_RECIPIENTS_BCC = "recipients_bcc";
    private static final String PARAMETER_LIST_POSITION_QUESTION_FILE_CHECKED = "list_position_question_file_checked";

    // Properties
    private static final String FIELD_TITLE = "module.workflow.formsautomaticassignment.task_config.label_title";
    private static final String FIELD_MAILINGLIST_SUBJECT = "module.workflow.formsautomaticassignment.task_config.label_mailinglist_subject";
    private static final String FIELD_MAILINGLIST_MESSAGE = "module.workflow.formsautomaticassignment.task_config.label_mailinglist_message";
    private static final String FIELD_MAILINGLIST_SENDER_NAME = "module.workflow.formsautomaticassignment.task_config.label_mailinglist_sender_name";
    private static final String FIELD_LABEL_LINK_view_form_response = "module.workflow.formsautomaticassignment.task_config.label_label_link_view_form_response";

    private static final String PROPERTY_SELECT_EMPTY_CHOICE = "module.workflow.assignment.task_assignment_config.label_empty_choice";

    // Messages
    private static final String MESSAGE_MANDATORY_FIELD = "module.workflow.formsautomaticassignment.message.mandatory.field";
    private static final String MESSAGE_NO_MAILINGLIST_FOR_WORKGROUP = "module.workflow.assignment.task_assignment_config.message.no_mailinglist_for_workgroup";
    private static final String MESSAGE_CONFIRM_FORM_UPDATE = "module.workflow.formsautomaticassignment.message.task_config.confirm_form_update";

    // JSP
    private static final String JSP_DO_UPDATE_DIRECTORY = "jsp/admin/plugins/workflow/modules/formsautomaticassignment/DoUpdateDirectory.jsp";

    // XML
    private static final String TAG_ASSIGNMENT = "assignment";
    private static final String TAG_LIST_WORKGROUP = "list-workgroup";
    private static final String TAG_WORKGROUP = "workgroup";

    // SERVICES
    @Inject
    @Named( TaskAutomaticAssignmentConfigService.BEAN_SERVICE )
    private ITaskConfigService _taskAutomaticAssignmentConfigService;
    @Inject
    private IAutomaticAssignmentService _automaticAssignmentService;
    @Inject
    private IAssignmentHistoryService _assignmentHistoryService;

    /**
     * {@inheritDoc}
     */
    @Override
    public String doSaveConfig( HttpServletRequest request, Locale locale, ITask task )
    {
        String strError = WorkflowUtils.EMPTY_STRING;

        String strTitle = request.getParameter( PARAMETER_TITLE );
        String strIsNotification = request.getParameter( PARAMETER_IS_NOTIFICATION );
        String strSenderName = request.getParameter( PARAMETER_SENDER_NAME );
        String strMessage = request.getParameter( PARAMETER_MESSAGE );
        String strSubject = request.getParameter( PARAMETER_SUBJECT );
        String strIdForm = request.getParameter( PARAMETER_ID_FORM );
        String [ ] tabWorkgroups = request.getParameterValues( PARAMETER_WORKGROUPS );
        String strViewRecord = request.getParameter( PARAMETER_view_form_response );
        String strLabelLinkViewRecord = request.getParameter( PARAMETER_LABEL_LINK_view_form_response );
        String strRecipientsCc = request.getParameter( PARAMETER_RECIPIENTS_CC );
        String strRecipientsBcc = request.getParameter( PARAMETER_RECIPIENTS_BCC );
        String [ ] tabSelectedPositionsEntryFile = request.getParameterValues( PARAMETER_LIST_POSITION_QUESTION_FILE_CHECKED );
        int nIdForm = -1;

        if ( ( strTitle == null ) || strTitle.trim( ).equals( WorkflowUtils.EMPTY_STRING ) )
        {
            strError = FIELD_TITLE;
        }

        if ( ( strIsNotification != null ) && ( ( strSubject == null ) || strSubject.equals( "" ) ) )
        {
            strError = FIELD_MAILINGLIST_SUBJECT;
        }

        if ( ( strIsNotification != null ) && ( ( strMessage == null ) || strMessage.equals( "" ) ) )
        {
            strError = FIELD_MAILINGLIST_MESSAGE;
        }

        if ( ( strIsNotification != null ) && ( ( strSenderName == null ) || strSenderName.equals( "" ) ) )
        {
            strError = FIELD_MAILINGLIST_SENDER_NAME;
        }

        if ( StringUtils.isNotBlank( strViewRecord ) && StringUtils.isBlank( strLabelLinkViewRecord ) )
        {
            strError = FIELD_LABEL_LINK_view_form_response;
        }

        if ( !strError.equals( WorkflowUtils.EMPTY_STRING ) )
        {
            Object [ ] tabRequiredFields = {
                    I18nService.getLocalizedString( strError, locale )
            };

            return AdminMessageService.getMessageUrl( request, MESSAGE_MANDATORY_FIELD, tabRequiredFields, AdminMessage.TYPE_STOP );
        }

        if ( strIdForm != null )
        {
            nIdForm = Integer.parseInt( strIdForm );
        }

        TaskAutomaticAssignmentConfig config = _taskAutomaticAssignmentConfigService.findByPrimaryKey( task.getId( ) );
        Boolean bCreate = false;

        if ( config == null )
        {
            config = new TaskAutomaticAssignmentConfig( );
            config.setIdTask( task.getId( ) );
            bCreate = true;
        }

        // Add workgroups
        List<WorkgroupConfig> listWorkgroupConfig = new ArrayList<WorkgroupConfig>( );
        WorkgroupConfig workgroupConfig;

        if ( tabWorkgroups != null )
        {
            for ( int i = 0; i < tabWorkgroups.length; i++ )
            {
                workgroupConfig = new WorkgroupConfig( );
                workgroupConfig.setIdTask( task.getId( ) );
                workgroupConfig.setWorkgroupKey( tabWorkgroups [i] );

                if ( strIsNotification != null )
                {
                    if ( WorkflowUtils.convertStringToInt( request.getParameter( PARAMETER_ID_MAILING_LIST + "_" + tabWorkgroups [i] ) ) != -1 )
                    {
                        workgroupConfig.setIdMailingList(
                                WorkflowUtils.convertStringToInt( request.getParameter( PARAMETER_ID_MAILING_LIST + "_" + tabWorkgroups [i] ) ) );
                    }
                    else
                    {
                        return AdminMessageService.getMessageUrl( request, MESSAGE_NO_MAILINGLIST_FOR_WORKGROUP, AdminMessage.TYPE_STOP );
                    }
                }

                listWorkgroupConfig.add( workgroupConfig );
            }
        }

        config.setWorkgroups( listWorkgroupConfig );
        config.setTitle( strTitle );
        config.setNotify( strIsNotification != null );

        config.setMessage( strMessage );
        config.setSubject( strSubject );
        config.setSenderName( strSenderName );
        config.setViewFormResponse( strViewRecord != null );
        config.setLabelLinkViewRecord( strLabelLinkViewRecord );
        config.setRecipientsCc( StringUtils.isNotEmpty( strRecipientsCc ) ? strRecipientsCc : StringUtils.EMPTY );
        config.setRecipientsBcc( StringUtils.isNotEmpty( strRecipientsBcc ) ? strRecipientsBcc : StringUtils.EMPTY );

        if ( config.getIdForm( ) != nIdForm )
        {
            config.setIdForm( nIdForm );

            if ( !bCreate )
            {
                UrlItem url = new UrlItem( JSP_DO_UPDATE_DIRECTORY );
                url.addParameter( PARAMETER_ID_FORM, nIdForm );
                url.addParameter( PARAMETER_ID_TASK, task.getId( ) );

                return AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_FORM_UPDATE, url.getUrl( ), AdminMessage.TYPE_CONFIRMATION );
            }
        }

        if ( ( tabSelectedPositionsEntryFile != null ) && ( tabSelectedPositionsEntryFile.length > 0 ) )
        {
            List<Integer> listSelectedPositionEntryFile = new ArrayList<Integer>( );

            for ( int i = 0; i < tabSelectedPositionsEntryFile.length; i++ )
            {
                listSelectedPositionEntryFile.add( WorkflowUtils.convertStringToInt( tabSelectedPositionsEntryFile [i] ) );
            }

            config.setListPositionsQuestionFile( listSelectedPositionEntryFile );
        }
        else
        {
            config.setListPositionsQuestionFile( null );
        }

        if ( bCreate )
        {
            _taskAutomaticAssignmentConfigService.create( config );
        }
        else
        {
            _taskAutomaticAssignmentConfigService.update( config );
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayConfigForm( HttpServletRequest request, Locale locale, ITask task )
    {

        Map<String, Object> model = new HashMap<String, Object>( );
        ReferenceList formsRefList = FormHome.getFormsReferenceList( );
        List<Map<String, Object>> listWorkgroups = new ArrayList<Map<String, Object>>( );
        String strNothing = StringUtils.EMPTY;

        TaskAutomaticAssignmentConfig config = _taskAutomaticAssignmentConfigService.findByPrimaryKey( task.getId( ) );

        if ( config == null )
        {
            config = new TaskAutomaticAssignmentConfig( );
            config.setIdTask( task.getId( ) );
        }

        for ( AdminWorkgroup workgroup : AdminWorkgroupHome.findAll( ) )
        {
            Map<String, Object> workgroupsItem = new HashMap<String, Object>( );
            workgroupsItem.put( MARK_ITEM, workgroup );

            if ( ( config != null ) && ( config.getWorkgroups( ) != null ) )
            {
                for ( WorkgroupConfig workgroupSelected : config.getWorkgroups( ) )
                {
                    if ( workgroup.getKey( ).equals( workgroupSelected.getWorkgroupKey( ) ) )
                    {
                        workgroupsItem.put( MARK_CONFIG, workgroupSelected );

                        break;
                    }
                }
            }

            listWorkgroups.add( workgroupsItem );
        }

        // customizable questions list
        List<Question> authorizedQuestions = _automaticAssignmentService.getAuthorizedQuestions( task.getId( ) );

        // All questions list
        List<Question> allQuestions = _automaticAssignmentService.getAllQuestions( task.getId( ) );

        // questions types file
        List<Question> questionsTypesFile = _automaticAssignmentService.getQuestionsTypesFile( task.getId( ) );

        ReferenceList refMailingList = new ReferenceList( );
        refMailingList.addItem( WorkflowUtils.CONSTANT_ID_NULL, strNothing );

        ReferenceList refMailList = new ReferenceList( );

        for ( MailingList mailingList : MailingListHome.findAll( ) )
        {
            refMailList.addItem( mailingList.getId( ), mailingList.getName( ) );
        }

        refMailingList.addAll( refMailList );

        model.put( MARK_WORKGROUP_LIST, listWorkgroups );
        model.put( MARK_FORM_LIST, formsRefList );
        model.put( MARK_CONFIG, config );
        model.put( MARK_ALL_QUESTION_LIST, allQuestions );
        model.put( MARK_AUTHORIZED_QUESTION_LIST, authorizedQuestions );
        model.put( MARK_QUESTION_LIST_TYPES_FILE, questionsTypesFile );
        model.put( MARK_MAILING_LIST, refMailingList );
        model.put( MARK_WEBAPP_URL, AppPathService.getBaseUrl( request ) );
        model.put( MARK_LOCALE, locale );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_TASK_AUTO_ASSIGNMENT_CONFIG, locale, model );

        return template.getHtml( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayTaskInformation( int nIdHistory, HttpServletRequest request, Locale locale, ITask task )
    {
        Map<String, Object> model = new HashMap<String, Object>( );
        String strNothing = I18nService.getLocalizedString( PROPERTY_SELECT_EMPTY_CHOICE, locale );
        TaskAutomaticAssignmentConfig config = _taskAutomaticAssignmentConfigService.findByPrimaryKey( task.getId( ) );
        List<AssignmentHistory> listAssignmentHistory = _assignmentHistoryService.getListByHistory( nIdHistory, task.getId( ), WorkflowUtils.getPlugin( ) );
        ReferenceList refWorkgroups = AdminWorkgroupService.getUserWorkgroups( AdminUserService.getAdminUser( request ), locale );

        List<HashMap<String, Object>> listWorkgroups = new ArrayList<HashMap<String, Object>>( );

        for ( ReferenceItem referenceItem : refWorkgroups )
        {
            if ( !referenceItem.getCode( ).equals( AdminWorkgroupService.ALL_GROUPS ) )
            {
                HashMap<String, Object> workgroupsItem = new HashMap<String, Object>( );
                workgroupsItem.put( MARK_ITEM, referenceItem );

                if ( ( config != null ) && ( config.getWorkgroups( ) != null ) )
                {
                    for ( AssignmentHistory assignmentHistory : listAssignmentHistory )
                    {
                        if ( referenceItem.getCode( ).equals( assignmentHistory.getWorkgroup( ) ) )
                        {
                            workgroupsItem.put( MARK_CONFIG, referenceItem );

                            break;
                        }
                    }
                }

                listWorkgroups.add( workgroupsItem );
            }
        }

        ReferenceList refMailingList = new ReferenceList( );
        refMailingList.addItem( WorkflowUtils.CONSTANT_ID_NULL, strNothing );
        refMailingList.addAll( AdminMailingListService.getMailingLists( AdminUserService.getAdminUser( request ) ) );

        model.put( MARK_WORKGROUP_LIST, listWorkgroups );
        model.put( MARK_CONFIG, config );
        model.put( MARK_MAILING_LIST, refMailingList );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_TASK_AUTO_ASSIGNMENT_INFORMATION, locale, model );

        return template.getHtml( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTaskInformationXml( int nIdHistory, HttpServletRequest request, Locale locale, ITask task )
    {
        List<AssignmentHistory> listAssignmentHistory = _assignmentHistoryService.getListByHistory( nIdHistory, task.getId( ), WorkflowUtils.getPlugin( ) );

        StringBuffer strXml = new StringBuffer( );

        XmlUtil.beginElement( strXml, TAG_ASSIGNMENT );
        XmlUtil.beginElement( strXml, TAG_LIST_WORKGROUP );

        for ( ReferenceItem referenceItem : AdminWorkgroupService.getUserWorkgroups( AdminUserService.getAdminUser( request ), locale ) )
        {
            if ( !referenceItem.getCode( ).equals( AdminWorkgroupService.ALL_GROUPS ) )
            {
                if ( listAssignmentHistory != null )
                {
                    for ( AssignmentHistory assignmentHistory : listAssignmentHistory )
                    {
                        if ( referenceItem.getCode( ).equals( assignmentHistory.getWorkgroup( ) ) )
                        {
                            XmlUtil.addElementHtml( strXml, TAG_WORKGROUP, referenceItem.getName( ) );
                        }
                    }
                }
            }
        }

        XmlUtil.endElement( strXml, TAG_LIST_WORKGROUP );
        XmlUtil.endElement( strXml, TAG_ASSIGNMENT );

        return strXml.toString( );
    }
}
