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
package fr.paris.lutece.plugins.workflow.modules.formsautomaticassignment.business;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * AutomaticAssignmentDAO
 *
 */
@ApplicationScoped
@Named( AutomaticAssignmentDAO.BEAN_NAME )
public class AutomaticAssignmentDAO implements IAutomaticAssignmentDAO
{
    public static final String BEAN_NAME = "workflow-formsautomaticassignment.automaticAssignmentDAO";
    private static final String SQL_QUERY_FIND_BY_TASK_BY_QUESTION = "SELECT value, workgroup_key "
            + " FROM workflow_forms_auto_assignment WHERE id_task = ? AND id_question = ?";
    private static final String SQL_QUERY_FIND_BY_TASK = "SELECT value, workgroup_key, id_question " + " FROM workflow_forms_auto_assignment WHERE id_task = ?";
    private static final String SQL_QUERY_INSERT = "INSERT INTO  workflow_forms_auto_assignment  "
            + "(id_task,id_question, value, workgroup_key)VALUES(?,?,?,?)";
    private static final String SQL_QUERY_CHECK_EXIST = "SELECT  COUNT(*) AS result_nb FROM workflow_forms_auto_assignment  "
            + "WHERE id_task = ? AND id_question = ? AND value = ? AND workgroup_key = ?";
    private static final String SQL_QUERY_FIND_ID_QUESTION_BY_TASK = "SELECT DISTINCT id_question FROM workflow_forms_auto_assignment " + " WHERE id_task = ? ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM workflow_forms_auto_assignment  "
            + "WHERE id_task = ? AND id_question = ? AND value = ? AND workgroup_key = ?";
    private static final String SQL_QUERY_DELETE_BY_TASK = "DELETE FROM workflow_forms_auto_assignment  " + "WHERE id_task = ?";
    private static final String CONSTANT_VALUE = "value";
    private static final String CONSTANT_WORKGROUP = "workgroup_key";
    private static final String CONSTANT_ID_QUESTION = "id_question";

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkExist( AutomaticAssignment assign, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_CHECK_EXIST, plugin ) )
        {
            daoUtil.setInt( 1, assign.getIdTask( ) );
            daoUtil.setInt( 2, assign.getIdQuestion( ) );
            daoUtil.setInt( 3, assign.getIdField( ) );
            daoUtil.setString( 4, assign.getWorkgroupKey( ) );

            daoUtil.executeQuery( );

            if ( daoUtil.next( ) && daoUtil.getInt( 1 ) > 0 )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete( AutomaticAssignment assign, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin ) )
        {
            daoUtil.setInt( 1, assign.getIdTask( ) );
            daoUtil.setInt( 2, assign.getIdQuestion( ) );
            daoUtil.setInt( 3, assign.getIdField( ) );
            daoUtil.setString( 4, assign.getWorkgroupKey( ) );

            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteByTask( int nIdTask, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_BY_TASK, plugin ) )
        {
            daoUtil.setInt( 1, nIdTask );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void insert( AutomaticAssignment assign, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin ) )
        {
            int nPos = 0;

            daoUtil.setInt( ++nPos, assign.getIdTask( ) );
            daoUtil.setInt( ++nPos, assign.getIdQuestion( ) );
            daoUtil.setInt( ++nPos, assign.getIdField( ) );
            daoUtil.setString( ++nPos, assign.getWorkgroupKey( ) );

            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AutomaticAssignment> loadByTaskByQuestion( int nIdTask, int nIdQuestion, Plugin plugin )
    {
        List<AutomaticAssignment> assignmentList = new ArrayList<>( );
        AutomaticAssignment assignment = null;

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_FIND_BY_TASK_BY_QUESTION, plugin ) )
        {
            daoUtil.setInt( 1, nIdTask );
            daoUtil.setInt( 2, nIdQuestion );

            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                assignment = new AutomaticAssignment( );
                assignment.setIdTask( nIdTask );
                assignment.setIdQuestion( nIdQuestion );
                assignment.setIdField( daoUtil.getInt( CONSTANT_VALUE ) );
                assignment.setWorkgroupKey( daoUtil.getString( CONSTANT_WORKGROUP ) );
                assignmentList.add( assignment );
            }
        }
        return assignmentList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AutomaticAssignment> loadByTask( int nIdTask, Plugin plugin )
    {
        List<AutomaticAssignment> assignmentList = new ArrayList<>( );
        AutomaticAssignment assignment = null;

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_FIND_BY_TASK, plugin ) )
        {
            daoUtil.setInt( 1, nIdTask );

            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                assignment = new AutomaticAssignment( );
                assignment.setIdTask( nIdTask );
                assignment.setIdQuestion( daoUtil.getInt( CONSTANT_ID_QUESTION ) );
                assignment.setIdField( daoUtil.getInt( CONSTANT_VALUE ) );
                assignment.setWorkgroupKey( daoUtil.getString( CONSTANT_WORKGROUP ) );
                assignmentList.add( assignment );
            }
        }
        return assignmentList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> getIdQuestionsListByTask( int nIdTask, Plugin plugin )
    {
        List<Integer> idEntriesList = new ArrayList<>( );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_FIND_ID_QUESTION_BY_TASK, plugin ) )
        {
            daoUtil.setInt( 1, nIdTask );

            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                idEntriesList.add( daoUtil.getInt( CONSTANT_ID_QUESTION ) );
            }
        }
        return idEntriesList;
    }
}
