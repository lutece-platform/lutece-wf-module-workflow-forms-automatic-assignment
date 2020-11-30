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
package fr.paris.lutece.plugins.workflow.modules.formsautomaticassignment.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import fr.paris.lutece.plugins.forms.business.FormQuestionResponse;
import fr.paris.lutece.plugins.forms.business.FormQuestionResponseHome;
import fr.paris.lutece.plugins.forms.business.FormResponse;
import fr.paris.lutece.plugins.forms.business.FormResponseHome;
import fr.paris.lutece.plugins.genericattributes.business.Response;
import fr.paris.lutece.plugins.workflow.modules.assignment.service.IAssignmentHistoryService;
import fr.paris.lutece.plugins.workflow.modules.assignment.service.IWorkgroupConfigService;
import fr.paris.lutece.plugins.workflow.modules.formsautomaticassignment.business.AutomaticAssignment;
import fr.paris.lutece.plugins.workflow.modules.formsautomaticassignment.business.TaskAutomaticAssignmentConfig;
import fr.paris.lutece.plugins.workflow.service.WorkflowPlugin;
import fr.paris.lutece.plugins.workflowcore.business.resource.ResourceHistory;
import fr.paris.lutece.plugins.workflowcore.business.resource.ResourceWorkflow;
import fr.paris.lutece.plugins.workflowcore.service.config.ITaskConfigService;
import fr.paris.lutece.plugins.workflowcore.service.resource.IResourceHistoryService;
import fr.paris.lutece.plugins.workflowcore.service.resource.IResourceWorkflowService;
import fr.paris.lutece.plugins.workflowcore.service.task.SimpleTask;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;

/**
 *
 * TaskAutomaticAssignment
 *
 */
public class TaskAutomaticAssignment extends SimpleTask
{
    // SERVICES
    @Inject
    @Named( TaskAutomaticAssignmentConfigService.BEAN_SERVICE )
    private ITaskConfigService _taskAutomaticAssignmentConfigService;
    @Inject
    private IAutomaticAssignmentService _automaticAssignmentService;
    @Inject
    private IResourceHistoryService _resourceHistoryService;
    @Inject
    private IResourceWorkflowService _resourceWorkflowService;
    @Inject
    private IAssignmentHistoryService _assignmentHistoryService;
    @Inject
    private IWorkgroupConfigService _workgroupConfigService;

    /**
     * {@inheritDoc}
     */
    @Override
    public void processTask( int nIdResourceHistory, HttpServletRequest request, Locale locale )
    {
        Plugin autoAssignPlugin = PluginService.getPlugin( AutomaticAssignmentPlugin.PLUGIN_NAME );

        ResourceHistory resourceHistory = _resourceHistoryService.findByPrimaryKey( nIdResourceHistory );

        FormResponse formResponse = FormResponseHome.findByPrimaryKey( resourceHistory.getIdResource( ) );
        if ( formResponse == null )
        {
            return;   
        }

        List<String> listWorkgroup = new ArrayList<>( );

        List<AutomaticAssignment> automaticAssignementList = _automaticAssignmentService.findByTask( this.getId( ), autoAssignPlugin );
        List<FormQuestionResponse> listFormQuestionResponse = FormQuestionResponseHome.getFormQuestionResponseListByFormResponse( formResponse.getId( ) );

        for ( FormQuestionResponse formQuestionResponse : listFormQuestionResponse )
        {
            for ( AutomaticAssignment automaticAssignment : automaticAssignementList )
            {
                if ( formQuestionResponse.getQuestion( ).getId( ) == automaticAssignment.getIdQuestion( ) )
                {
                    for ( Response response : formQuestionResponse.getEntryResponse( ) )
                    {
                        if ( response.getField( ) != null && response.getField( ).getIdField( ) == automaticAssignment.getIdField( ) )
                        {
                            listWorkgroup.add( automaticAssignment.getWorkgroupKey( ) );
                        }
                    }
                }
            }

        }

        TaskAutomaticAssignmentConfig config = _taskAutomaticAssignmentConfigService.findByPrimaryKey( this.getId( ) );

        if ( ( config != null ) && config.isNotify( ) )
        {
            _automaticAssignmentService.notify( listFormQuestionResponse, config, listWorkgroup, resourceHistory, request, locale, this );
        }

        // update resource workflow
        ResourceWorkflow resourceWorkflow = _resourceWorkflowService.findByPrimaryKey( resourceHistory.getIdResource( ), resourceHistory.getResourceType( ),
                resourceHistory.getWorkflow( ).getId( ) );
        resourceWorkflow.setWorkgroups( listWorkgroup );
        _resourceWorkflowService.update( resourceWorkflow );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doRemoveConfig( )
    {
        Plugin autoAssignPlugin = PluginService.getPlugin( AutomaticAssignmentPlugin.PLUGIN_NAME );
        Plugin workflowPlugin = PluginService.getPlugin( WorkflowPlugin.PLUGIN_NAME );
        // Remove config
        _taskAutomaticAssignmentConfigService.remove( this.getId( ) );
        _automaticAssignmentService.removeByTask( this.getId( ), autoAssignPlugin );
        // Remove task information
        _assignmentHistoryService.removeByTask( this.getId( ), workflowPlugin );
        _workgroupConfigService.removeByTask( this.getId( ), workflowPlugin );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doRemoveTaskInformation( int nIdHistory )
    {
        Plugin workflowPlugin = PluginService.getPlugin( WorkflowPlugin.PLUGIN_NAME );
        _assignmentHistoryService.removeByHistory( nIdHistory, this.getId( ), workflowPlugin );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle( Locale locale )
    {
        TaskAutomaticAssignmentConfig config = _taskAutomaticAssignmentConfigService.findByPrimaryKey( this.getId( ) );

        if ( config != null )
        {
            return config.getTitle( );
        }

        return StringUtils.EMPTY;
    }
}
