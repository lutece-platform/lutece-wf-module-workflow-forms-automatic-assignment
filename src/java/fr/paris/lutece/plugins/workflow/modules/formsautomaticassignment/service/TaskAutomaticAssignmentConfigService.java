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

import fr.paris.lutece.plugins.workflow.modules.assignment.business.WorkgroupConfig;
import fr.paris.lutece.plugins.workflow.modules.assignment.service.IWorkgroupConfigService;
import fr.paris.lutece.plugins.workflow.modules.formsautomaticassignment.business.AutomaticAssignment;
import fr.paris.lutece.plugins.workflow.modules.formsautomaticassignment.business.ITaskAutomaticAssignmentConfigDAO;
import fr.paris.lutece.plugins.workflow.modules.formsautomaticassignment.business.TaskAutomaticAssignmentConfig;
import fr.paris.lutece.plugins.workflow.utils.WorkflowUtils;
import fr.paris.lutece.plugins.workflowcore.business.config.ITaskConfig;
import fr.paris.lutece.plugins.workflowcore.business.config.ITaskConfigDAO;
import fr.paris.lutece.plugins.workflowcore.service.config.TaskConfigService;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;

/**
 *
 * TaskAutomaticAssignmentConfigService
 *
 */
@ApplicationScoped
@Named( TaskAutomaticAssignmentConfigService.BEAN_SERVICE )
public class TaskAutomaticAssignmentConfigService extends TaskConfigService
{
    /** The Constant BEAN_SERVICE. */
    public static final String BEAN_SERVICE = "workflow-formsautomaticassignment.taskAutomaticAssignmentConfigService";
    @Inject
    private IWorkgroupConfigService _workgroupConfigService;
    @Inject
    private ITaskAutomaticAssignmentConfigDAO _taskAutomaticAssignmentDAO;
    @Inject
    private IAutomaticAssignmentService _automaticAssignmentService;

    @Inject
    public TaskAutomaticAssignmentConfigService(@Named( "workflow-formsautomaticassignment.taskAutomaticAssignmentConfigDAO" ) ITaskConfigDAO<TaskAutomaticAssignmentConfig> taskAutomaticAssignmentConfigDAO) {
       setTaskConfigDAO( (ITaskConfigDAO) taskAutomaticAssignmentConfigDAO ); 
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void create( ITaskConfig config )
    {
        super.create( config );

        TaskAutomaticAssignmentConfig autoAssignConfig = getConfigBean( config );
        if ( autoAssignConfig == null )
        {
            return;
        }
        List<WorkgroupConfig> listWorkgroups = autoAssignConfig.getWorkgroups( );

        if ( listWorkgroups != null )
        {
            for ( WorkgroupConfig workgroupConfig : listWorkgroups )
            {
                // Workaround in case of task duplication
                workgroupConfig.setIdTask( config.getIdTask( ) );
                _workgroupConfigService.create( workgroupConfig, WorkflowUtils.getPlugin( ) );
            }
        }

        if ( autoAssignConfig.getListPositionsQuestionFile( ) != null )
        {
            for ( Integer nPositionEntryFile : autoAssignConfig.getListPositionsQuestionFile( ) )
            {
                _taskAutomaticAssignmentDAO.insertListPositionsEntryFile( config.getIdTask( ), nPositionEntryFile );
            }
        }

        if ( autoAssignConfig.getListAutomaticAssignments( ) != null )
        {
            for ( AutomaticAssignment autoAssignment : autoAssignConfig.getListAutomaticAssignments( ) )
            {
                autoAssignment.setIdTask( config.getIdTask( ) );

                if ( !_automaticAssignmentService.checkExist( autoAssignment, AutomaticAssignmentPlugin.getPlugin( ) ) )
                {
                    _automaticAssignmentService.create( autoAssignment, AutomaticAssignmentPlugin.getPlugin( ) );
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void update( ITaskConfig config )
    {
        super.update( config );

        // Update workgroups
        _workgroupConfigService.removeByTask( config.getIdTask( ), WorkflowUtils.getPlugin( ) );

        TaskAutomaticAssignmentConfig autoAssignConfig = getConfigBean( config );
        if ( autoAssignConfig == null )
        {
            return;
        }

        List<WorkgroupConfig> listWorkgroups = autoAssignConfig.getWorkgroups( );

        if ( listWorkgroups != null )
        {
            for ( WorkgroupConfig workgroupConfig : listWorkgroups )
            {
                _workgroupConfigService.create( workgroupConfig, WorkflowUtils.getPlugin( ) );
            }
        }

        _taskAutomaticAssignmentDAO.deleteListPositionsEntryFile( config.getIdTask( ) );

        if ( autoAssignConfig.getListPositionsQuestionFile( ) != null )
        {
            for ( Integer nPositionEntryFile : autoAssignConfig.getListPositionsQuestionFile( ) )
            {
                _taskAutomaticAssignmentDAO.insertListPositionsEntryFile( config.getIdTask( ), nPositionEntryFile );
            }
        }

        _automaticAssignmentService.removeByTask( config.getIdTask( ), AutomaticAssignmentPlugin.getPlugin( ) );

        if ( autoAssignConfig.getListAutomaticAssignments( ) != null )
        {
            for ( AutomaticAssignment autoAssignment : autoAssignConfig.getListAutomaticAssignments( ) )
            {
                autoAssignment.setIdTask( config.getIdTask( ) );

                if ( !_automaticAssignmentService.checkExist( autoAssignment, AutomaticAssignmentPlugin.getPlugin( ) ) )
                {
                    _automaticAssignmentService.create( autoAssignment, AutomaticAssignmentPlugin.getPlugin( ) );
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void remove( int nIdTask )
    {
        _workgroupConfigService.removeByTask( nIdTask, WorkflowUtils.getPlugin( ) );
        _taskAutomaticAssignmentDAO.deleteListPositionsEntryFile( nIdTask );
        _automaticAssignmentService.removeByTask( nIdTask, AutomaticAssignmentPlugin.getPlugin( ) );
        super.remove( nIdTask );
    }

    // Finders

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T findByPrimaryKey( int nIdTask )
    {
        TaskAutomaticAssignmentConfig config = super.findByPrimaryKey( nIdTask );

        if ( config != null )
        {
            config.setWorkgroups( _workgroupConfigService.getListByConfig( nIdTask, WorkflowUtils.getPlugin( ) ) );
            config.setListPositionsQuestionFile( _taskAutomaticAssignmentDAO.loadListPositionsEntryFile( nIdTask ) );
            config.setListAutomaticAssignments( _automaticAssignmentService.findByTask( nIdTask, AutomaticAssignmentPlugin.getPlugin( ) ) );
        }

        return (T) config;
    }
}
