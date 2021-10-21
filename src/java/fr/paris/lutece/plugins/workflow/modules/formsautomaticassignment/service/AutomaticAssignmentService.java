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
package fr.paris.lutece.plugins.workflow.modules.formsautomaticassignment.service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import fr.paris.lutece.plugins.forms.business.FormQuestionResponse;
import fr.paris.lutece.plugins.forms.business.Question;
import fr.paris.lutece.plugins.forms.business.QuestionHome;
import fr.paris.lutece.plugins.genericattributes.business.Response;
import fr.paris.lutece.plugins.genericattributes.service.entrytype.EntryTypeServiceManager;
import fr.paris.lutece.plugins.genericattributes.service.entrytype.IEntryTypeService;
import fr.paris.lutece.plugins.workflow.modules.assignment.business.AssignmentHistory;
import fr.paris.lutece.plugins.workflow.modules.assignment.business.WorkgroupConfig;
import fr.paris.lutece.plugins.workflow.modules.assignment.service.IAssignmentHistoryService;
import fr.paris.lutece.plugins.workflow.modules.assignment.service.IWorkgroupConfigService;
import fr.paris.lutece.plugins.workflow.modules.formsautomaticassignment.business.AutomaticAssignment;
import fr.paris.lutece.plugins.workflow.modules.formsautomaticassignment.business.IAutomaticAssignmentDAO;
import fr.paris.lutece.plugins.workflow.modules.formsautomaticassignment.business.TaskAutomaticAssignmentConfig;
import fr.paris.lutece.plugins.workflow.service.WorkflowPlugin;
import fr.paris.lutece.plugins.workflow.utils.WorkflowUtils;
import fr.paris.lutece.plugins.workflowcore.business.resource.ResourceHistory;
import fr.paris.lutece.plugins.workflowcore.service.config.ITaskConfigService;
import fr.paris.lutece.plugins.workflowcore.service.task.ITask;
import fr.paris.lutece.portal.business.file.File;
import fr.paris.lutece.portal.business.file.FileHome;
import fr.paris.lutece.portal.business.mailinglist.Recipient;
import fr.paris.lutece.portal.business.physicalfile.PhysicalFile;
import fr.paris.lutece.portal.business.physicalfile.PhysicalFileHome;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.mail.MailService;
import fr.paris.lutece.portal.service.mailinglist.AdminMailingListService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.mail.FileAttachment;
import fr.paris.lutece.util.xml.XmlUtil;

/**
 *
 * AutomaticAssignmentService
 *
 */
public final class AutomaticAssignmentService implements IAutomaticAssignmentService
{
    public static final String BEAN_SERVICE = "workflow-formsautomaticassignment.automaticAssignmentService";

    // TEMPLATE
    private static final String TEMPLATE_TASK_NOTIFICATION_MAIL = "admin/plugins/workflow/modules/notification/task_notification_mail.html";

    // MARKS
    private static final String MARK_MESSAGE = "message";
    private static final String MARK_QUESTION_MARKER = "question_";
    private static final String MARK_LINK_VIEW_FORM_RESPONSE = "link_view_form_response";

    // PROPERTIES
    private static final String PROPERTY_LUTECE_ADMIN_PROD_URL = "lutece.admin.prod.url";
    private static final String PROPERTY_LUTECE_BASE_URL = "lutece.base.url";
    private static final String PROPERTY_LUTECE_PROD_URL = "lutece.prod.url";
    private static final String PROPERTY_ENTRIES_TYPE_ALLOWED = "workflow-forms-automatic-assignment.entriesTypeAllowed";
    private static final String PROPERTY_ENTRIES_TYPE_FILES = "workflow-forms-automatic-assignment.entriesTypeFiles";

    // MESSAGES
    private static final String PROPERTY_MAIL_SENDER_NAME = "module.workflow.assignment.task_assignment_config.mailSenderName";

    // JSP
    private static final String PROPERTY_VIEW_RECAP_URL = "workflow-forms-automatic-assignment.viewRecapUrl";
    // TAGS
    private static final String TAG_A = "a";

    // ATTRIBUTES
    private static final String ATTRIBUTE_HREF = "href";

    // CONSTANTS
    private static final String COMMA = ",";

    // CONSTANTS
    private static final String CONSTANT_COMMA = ", ";
    private static final String CONSTANT_SLASH = "/";

    // SERVICES
    @Inject
    private IAutomaticAssignmentDAO _automaticAssignmentDAO;
    @Inject
    @Named( TaskAutomaticAssignmentConfigService.BEAN_SERVICE )
    private ITaskConfigService _taskAutomaticAssignmentConfigService;
    @Inject
    private IAutomaticAssignmentService _automaticAssignmentService;
    @Inject
    private IAssignmentHistoryService _assignmentHistoryService;
    @Inject
    private IWorkgroupConfigService _workgroupConfigService;

    /**
     * Private constructor
     */
    private AutomaticAssignmentService( )
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional( AutomaticAssignmentPlugin.BEAN_TRANSACTION_MANAGER )
    public void create( AutomaticAssignment assign, Plugin plugin )
    {
        _automaticAssignmentDAO.insert( assign, plugin );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional( AutomaticAssignmentPlugin.BEAN_TRANSACTION_MANAGER )
    public void remove( AutomaticAssignment assign, Plugin plugin )
    {
        _automaticAssignmentDAO.delete( assign, plugin );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional( AutomaticAssignmentPlugin.BEAN_TRANSACTION_MANAGER )
    public void removeByTask( int nIdTask, Plugin plugin )
    {
        _automaticAssignmentDAO.deleteByTask( nIdTask, plugin );
    }

    // CHECKS

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkExist( AutomaticAssignment assign, Plugin plugin )
    {
        return _automaticAssignmentDAO.checkExist( assign, plugin );
    }

    // GET

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AutomaticAssignment> findByTaskByQuestion( int nIdTask, int nIdEntry, Plugin plugin )
    {
        return _automaticAssignmentDAO.loadByTaskByQuestion( nIdTask, nIdEntry, plugin );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AutomaticAssignment> findByTask( int nIdTask, Plugin plugin )
    {
        return _automaticAssignmentDAO.loadByTask( nIdTask, plugin );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> findAllIdEntriesByTask( int nIdTask, Plugin plugin )
    {
        return _automaticAssignmentDAO.getIdQuestionsListByTask( nIdTask, plugin );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Question> getAllQuestions( int nIdTask )
    {

        TaskAutomaticAssignmentConfig config = _taskAutomaticAssignmentConfigService.findByPrimaryKey( nIdTask );

        List<Question> listQuestions = new ArrayList<>( );

        if ( config != null )
        {
            listQuestions = QuestionHome.getListQuestionByIdForm( config.getIdForm( ) );

        }

        return listQuestions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Question> getAuthorizedQuestions( int nIdTask )
    {

        List<Integer> listIdTypesAuthorized = fillListEntryTypes( PROPERTY_ENTRIES_TYPE_ALLOWED );

        return getAllQuestions( nIdTask ).stream( ).filter( x -> listIdTypesAuthorized.contains( x.getEntry( ).getEntryType( ).getIdType( ) ) )
                .collect( Collectors.toList( ) );
    }

    @Override
    public List<Question> getQuestionsTypesFile( int nIdTask )
    {

        List<Integer> listIdTypesFiles = fillListEntryTypes( PROPERTY_ENTRIES_TYPE_FILES );

        return getAllQuestions( nIdTask ).stream( ).filter( x -> listIdTypesFiles.contains( x.getEntry( ).getEntryType( ).getIdType( ) ) )
                .collect( Collectors.toList( ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FileAttachment> getFilesAttachment( TaskAutomaticAssignmentConfig config, List<FormQuestionResponse> listFormQuestionResponse )
    {
        List<FileAttachment> listFileAttachment = new ArrayList<>( );
        if ( CollectionUtils.isEmpty( config.getListPositionsQuestionFile( ) ) )
        {
            return listFileAttachment;
        }
        for ( Integer nPositionEntryFile : config.getListPositionsQuestionFile( ) )
        {
            List<File> listFiles = getFiles( nPositionEntryFile, listFormQuestionResponse );

            if ( CollectionUtils.isNotEmpty( listFiles ) )
            {
                for ( File file : listFiles )
                {
                    if ( ( file != null ) && ( file.getPhysicalFile( ) != null ) )
                    {
                        FileAttachment fileAttachment = new FileAttachment( file.getTitle( ), file.getPhysicalFile( ).getValue( ), file.getMimeType( ) );
                        listFileAttachment.add( fileAttachment );
                    }
                }
            }
        }
        return listFileAttachment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notify( List<FormQuestionResponse> listFormQuestionResponse, TaskAutomaticAssignmentConfig config, List<String> listWorkgroup,
            ResourceHistory resourceHistory, HttpServletRequest request, Locale locale, ITask task )
    {
        Plugin workflowPlugin = PluginService.getPlugin( WorkflowPlugin.PLUGIN_NAME );

        String strSenderEmail = MailService.getNoReplyEmail( );
        String strSenderName = config.getSenderName( );

        if ( StringUtils.isBlank( strSenderName ) )
        {
            strSenderName = I18nService.getLocalizedString( PROPERTY_MAIL_SENDER_NAME, locale );
        }

        Map<String, Object> model = buildModel( config, resourceHistory, listFormQuestionResponse, request, locale );
        String strEmailContent = buildMailHtml( model, locale );
        String strSubject = buildSubjectHtml( config, model, locale );

        List<FileAttachment> listFileAttachments = getFilesAttachment( config, listFormQuestionResponse );

        // Notify the mailings list associated to each workgroup
        for ( String workGroup : listWorkgroup )
        {
            // add history
            AssignmentHistory history = new AssignmentHistory( );
            history.setIdResourceHistory( resourceHistory.getId( ) );
            history.setIdTask( task.getId( ) );
            history.setWorkgroup( workGroup );
            _assignmentHistoryService.create( history, workflowPlugin );

            WorkgroupConfig workgroupConfig = _workgroupConfigService.findByPrimaryKey( task.getId( ), workGroup, workflowPlugin );
            if ( workgroupConfig == null || workgroupConfig.getIdMailingList( ) == WorkflowUtils.CONSTANT_ID_NULL )
            {
                continue;
            }

            Collection<Recipient> listRecipients = AdminMailingListService.getRecipients( workgroupConfig.getIdMailingList( ) );
            // Send Mail
            for ( Recipient recipient : listRecipients )
            {
                if ( CollectionUtils.isNotEmpty( listFileAttachments ) )
                {
                    MailService.sendMailMultipartHtml( recipient.getEmail( ), null, null, strSenderName, strSenderEmail, strSubject, strEmailContent, null,
                            listFileAttachments );
                }
                else
                {
                    // Build the mail message
                    MailService.sendMailHtml( recipient.getEmail( ), strSenderName, strSenderEmail, strSubject, strEmailContent );
                }
            }
        }

        // Notify recipients
        boolean bHasRecipients = ( StringUtils.isNotBlank( config.getRecipientsBcc( ) ) || StringUtils.isNotBlank( config.getRecipientsCc( ) ) );

        if ( !bHasRecipients )
        {
            return;
        }

        if ( CollectionUtils.isNotEmpty( listFileAttachments ) )
        {
            MailService.sendMailMultipartHtml( null, config.getRecipientsCc( ), config.getRecipientsBcc( ), strSenderName, strSenderEmail, strSubject,
                    strEmailContent, null, listFileAttachments );
        }
        else
        {
            MailService.sendMailHtml( null, config.getRecipientsCc( ), config.getRecipientsBcc( ), config.getSenderName( ), strSenderEmail, strSubject,
                    strEmailContent );
        }
    }

    // PRIVATE METHODS

    /**
     * Fill the list of entry types
     * 
     * @param strPropertyEntryTypes
     *            the property containing the entry types
     * @return a list of integer
     */
    private static List<Integer> fillListEntryTypes( String strPropertyEntryTypes )
    {
        List<Integer> listEntryTypes = new ArrayList<>( );
        String strEntryTypes = AppPropertiesService.getProperty( strPropertyEntryTypes );

        if ( StringUtils.isNotBlank( strEntryTypes ) )
        {
            String [ ] listAcceptEntryTypesForIdDemand = strEntryTypes.split( COMMA );

            for ( String strAcceptEntryType : listAcceptEntryTypesForIdDemand )
            {
                if ( StringUtils.isNotBlank( strAcceptEntryType ) && StringUtils.isNumeric( strAcceptEntryType ) )
                {
                    int nAcceptedEntryType = Integer.parseInt( strAcceptEntryType );
                    listEntryTypes.add( nAcceptedEntryType );
                }
            }
        }

        return listEntryTypes;
    }

    /**
     * Get the directory files
     * 
     * @param nPosition
     *            the position of the entry
     * @param nIdRecord
     *            the id record
     * @param nIdDirectory
     *            the id directory
     * @return the directory file
     */
    private List<File> getFiles( int nPosition, List<FormQuestionResponse> listFormQuestionResponse )
    {

        List<File> listFiles = new ArrayList<>( );

        for ( FormQuestionResponse formQuestionResponse : listFormQuestionResponse )
        {

            if ( formQuestionResponse.getQuestion( ).getEntry( ).getPosition( ) == nPosition
                    && !CollectionUtils.isEmpty( formQuestionResponse.getEntryResponse( ) ) )
            {
                File file = formQuestionResponse.getEntryResponse( ).get( 0 ).getFile( );

                if ( file != null )
                {
                    file = FileHome.findByPrimaryKey( file.getIdFile( ) );
                    PhysicalFile physicalFile = PhysicalFileHome.findByPrimaryKey( file.getPhysicalFile( ).getIdPhysicalFile( ) );
                    file.setPhysicalFile( physicalFile );
                    listFiles.add( file );
                }
            }

        }
        return listFiles;
    }

    /**
     * Get the base url
     * 
     * @param request
     *            the HTTP request
     * @return the base url
     */
    private String getBaseUrl( HttpServletRequest request )
    {
        String strBaseUrl;

        if ( request != null )
        {
            strBaseUrl = AppPathService.getBaseUrl( request );
        }
        else
        {
            strBaseUrl = AppPropertiesService.getProperty( PROPERTY_LUTECE_ADMIN_PROD_URL );

            if ( StringUtils.isBlank( strBaseUrl ) )
            {
                strBaseUrl = AppPropertiesService.getProperty( PROPERTY_LUTECE_BASE_URL );

                if ( StringUtils.isBlank( strBaseUrl ) )
                {
                    strBaseUrl = AppPropertiesService.getProperty( PROPERTY_LUTECE_PROD_URL );
                }
            }
        }

        return strBaseUrl;
    }

    /**
     * Build the mail Html
     * 
     * @param model
     *            the model
     * @param locale
     *            the {@link Locale}
     * @return the mail HTML
     */
    private String buildMailHtml( Map<String, Object> model, Locale locale )
    {
        HtmlTemplate t = AppTemplateService
                .getTemplateFromStringFtl( AppTemplateService.getTemplate( TEMPLATE_TASK_NOTIFICATION_MAIL, locale, model ).getHtml( ), locale, model );

        return t.getHtml( );
    }

    /**
     * Build the subject Html
     * 
     * @param config
     *            the config
     * @param model
     *            the model
     * @param locale
     *            the {@link Locale}
     * @return the subject
     */
    private String buildSubjectHtml( TaskAutomaticAssignmentConfig config, Map<String, Object> model, Locale locale )
    {
        return AppTemplateService.getTemplateFromStringFtl( config.getSubject( ), locale, model ).getHtml( );
    }

    /**
     * Build the model for the mail content and for the subject
     * 
     * @param config
     *            the config
     * @param resourceHistory
     *            the resource history
     * @param request
     *            the HTTP request
     * @param locale
     *            the {@link Locale}
     * @return the model
     */
    private Map<String, Object> buildModel( TaskAutomaticAssignmentConfig config, ResourceHistory resourceHistory,
            List<FormQuestionResponse> listFormQuestionResponse, HttpServletRequest request, Locale locale )
    {
        Map<String, Object> model = new HashMap<>( );

        List<Question> allQuestions = _automaticAssignmentService.getAllQuestions( config.getIdTask( ) );

        // Get values for markers that can be used in the message

        for ( Question questionAuthorized : allQuestions )
        {
            String strKey = MARK_QUESTION_MARKER + questionAuthorized.getId( );
            for ( FormQuestionResponse formQuestionResponse : listFormQuestionResponse )
            {
                if ( formQuestionResponse.getQuestion( ).getId( ) == questionAuthorized.getId( ) )
                {
                    IEntryTypeService entryTypeService = EntryTypeServiceManager.getEntryTypeService( questionAuthorized.getEntry( ) );
                    for ( Response response : formQuestionResponse.getEntryResponse( ) )
                    {

                        if ( response.getFile( ) != null )
                        {
                            response.setFile( FileHome.findByPrimaryKey( response.getFile( ).getIdFile( ) ) );
                        }

                        String strQuestionResponseValue = entryTypeService.getResponseValueForRecap( formQuestionResponse.getQuestion( ).getEntry( ), request,
                                response, locale );

                        if ( model.containsKey( strKey ) )
                        {
                            model.put( strKey, model.get( strKey ) + CONSTANT_COMMA + strQuestionResponseValue );

                        }
                        else
                        {

                            model.put( strKey, strQuestionResponseValue );

                        }

                    }

                }
            }

        }

        // Link View record
        String strLinkViewRecordHtml = "";

        if ( config.isViewFormResponse( ) )
        {
            StringBuilder sRecapUrl = new StringBuilder( getBaseUrl( request ) );

            if ( ( sRecapUrl.length( ) > 0 ) && !sRecapUrl.toString( ).endsWith( CONSTANT_SLASH ) )
            {
                sRecapUrl.append( CONSTANT_SLASH );
            }

            sRecapUrl.append( AppPropertiesService.getProperty( PROPERTY_VIEW_RECAP_URL ) );

            MessageFormat mrecapUrl = new MessageFormat( sRecapUrl.toString( ) );
            Object [ ] recapParams = {
                    config.getIdForm( ), resourceHistory.getIdResource( )
            };

            String strFinalUrl = mrecapUrl.format( recapParams );

            StringBuffer sbLinkHtml = new StringBuffer( );
            Map<String, String> mapParams = new HashMap<>( );
            mapParams.put( ATTRIBUTE_HREF, strFinalUrl );
            XmlUtil.beginElement( sbLinkHtml, TAG_A, mapParams );
            sbLinkHtml.append( config.getLabelLinkViewRecord( ) );
            XmlUtil.endElement( sbLinkHtml, TAG_A );

            Map<String, Object> modelTmp = new HashMap<>( );
            modelTmp.put( MARK_LINK_VIEW_FORM_RESPONSE, strFinalUrl );
            strLinkViewRecordHtml = AppTemplateService.getTemplateFromStringFtl( sbLinkHtml.toString( ), locale, modelTmp ).getHtml( );
        }

        model.put( MARK_LINK_VIEW_FORM_RESPONSE, strLinkViewRecordHtml );
        model.put( MARK_MESSAGE, config.getMessage( ) );

        return model;
    }

}
