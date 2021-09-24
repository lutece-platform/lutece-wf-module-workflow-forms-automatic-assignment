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

import fr.paris.lutece.plugins.workflow.modules.formsautomaticassignment.service.AutomaticAssignmentPlugin;
import fr.paris.lutece.util.sql.DAOUtil;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * TaskAutomaticAssignmentConfigDAO
 *
 */
public class TaskAutomaticAssignmentConfigDAO implements ITaskAutomaticAssignmentConfigDAO
{
    private static final String SQL_QUERY_FIND_BY_PRIMARY_KEY = "SELECT id_task, id_form, title,is_notify,message,subject,sender_name,is_view_form_response,label_link_view_form_response,recipients_cc,recipients_bcc "
            + " FROM workflow_forms_auto_assignment_cf WHERE id_task=?";
    private static final String SQL_QUERY_INSERT = "INSERT INTO  workflow_forms_auto_assignment_cf  "
            + "(id_task,id_form, title, is_notify,message,subject,sender_name,is_view_form_response,label_link_view_form_response,recipients_cc,recipients_bcc)VALUES(?,?,?,?,?,?,?,?,?,?,?)";
    private static final String SQL_QUERY_UPDATE = "UPDATE workflow_forms_auto_assignment_cf "
            + "SET id_task=?,id_form=?, title=?, is_notify=?, message = ?, subject = ?, sender_name = ?, is_view_form_response = ?, label_link_view_form_response = ?, recipients_cc = ?, recipients_bcc = ? "
            + " WHERE id_task=? ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM workflow_forms_auto_assignment_cf  WHERE id_task=? ";
    private static final String SQL_QUERY_DELETE_POSITION_ENTRY_FILE = " DELETE FROM workflow_forms_auto_assignment_ef where id_task= ? ";
    private static final String SQL_QUERY_INSERT_POSITION_ENTRY_FILE = " INSERT INTO workflow_forms_auto_assignment_ef( "
            + " id_task,position_form_question_file) VALUES ( ?,? ) ";
    private static final String SQL_QUERY_FIND_LIST_POSITION_ENTRY_FILE = "SELECT position_form_question_file "
            + " FROM workflow_forms_auto_assignment_ef WHERE id_task = ? ";

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void insert( TaskAutomaticAssignmentConfig config )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, AutomaticAssignmentPlugin.getPlugin( ) ) )
        {
            int nPos = 0;

            daoUtil.setInt( ++nPos, config.getIdTask( ) );
            daoUtil.setInt( ++nPos, config.getIdForm( ) );
            daoUtil.setString( ++nPos, config.getTitle( ) );
            daoUtil.setBoolean( ++nPos, config.isNotify( ) );
            daoUtil.setString( ++nPos, config.getMessage( ) );
            daoUtil.setString( ++nPos, config.getSubject( ) );
            daoUtil.setString( ++nPos, config.getSenderName( ) );
            daoUtil.setBoolean( ++nPos, config.isViewFormResponse( ) );
            daoUtil.setString( ++nPos, config.getLabelLinkViewRecord( ) );
            daoUtil.setString( ++nPos, config.getRecipientsCc( ) );
            daoUtil.setString( ++nPos, config.getRecipientsBcc( ) );

            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void store( TaskAutomaticAssignmentConfig config )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, AutomaticAssignmentPlugin.getPlugin( ) ) )
        {
            int nPos = 0;

            daoUtil.setInt( ++nPos, config.getIdTask( ) );
            daoUtil.setInt( ++nPos, config.getIdForm( ) );
            daoUtil.setString( ++nPos, config.getTitle( ) );
            daoUtil.setBoolean( ++nPos, config.isNotify( ) );
            daoUtil.setString( ++nPos, config.getMessage( ) );
            daoUtil.setString( ++nPos, config.getSubject( ) );
            daoUtil.setString( ++nPos, config.getSenderName( ) );
            daoUtil.setBoolean( ++nPos, config.isViewFormResponse( ) );
            daoUtil.setString( ++nPos, config.getLabelLinkViewRecord( ) );
            daoUtil.setString( ++nPos, config.getRecipientsCc( ) );
            daoUtil.setString( ++nPos, config.getRecipientsBcc( ) );

            daoUtil.setInt( ++nPos, config.getIdTask( ) );

            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TaskAutomaticAssignmentConfig load( int nIdTask )
    {
        TaskAutomaticAssignmentConfig config = null;
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_FIND_BY_PRIMARY_KEY, AutomaticAssignmentPlugin.getPlugin( ) ) )
        {
            daoUtil.setInt( 1, nIdTask );
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                int nPos = 0;
                config = new TaskAutomaticAssignmentConfig( );
                config.setIdTask( daoUtil.getInt( ++nPos ) );
                config.setIdForm( daoUtil.getInt( ++nPos ) );
                config.setTitle( daoUtil.getString( ++nPos ) );
                config.setNotify( daoUtil.getBoolean( ++nPos ) );
                config.setMessage( daoUtil.getString( ++nPos ) );
                config.setSubject( daoUtil.getString( ++nPos ) );
                config.setSenderName( daoUtil.getString( ++nPos ) );
                config.setViewFormResponse( daoUtil.getBoolean( ++nPos ) );
                config.setLabelLinkViewRecord( daoUtil.getString( ++nPos ) );
                config.setRecipientsCc( daoUtil.getString( ++nPos ) );
                config.setRecipientsBcc( daoUtil.getString( ++nPos ) );
            }
        }
        return config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete( int nIdTask )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, AutomaticAssignmentPlugin.getPlugin( ) ) )
        {
            daoUtil.setInt( 1, nIdTask );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> loadListPositionsEntryFile( int nIdTask )
    {
        List<Integer> listIntegerPositionEntryFile = new ArrayList<>( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_FIND_LIST_POSITION_ENTRY_FILE, AutomaticAssignmentPlugin.getPlugin( ) ) )
        {
            daoUtil.setInt( 1, nIdTask );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                listIntegerPositionEntryFile.add( daoUtil.getInt( 1 ) );
            }
        }
        return listIntegerPositionEntryFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteListPositionsEntryFile( int nIdTask )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_POSITION_ENTRY_FILE, AutomaticAssignmentPlugin.getPlugin( ) ) )
        {
            daoUtil.setInt( 1, nIdTask );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertListPositionsEntryFile( int nIdTask, Integer nPositionEntryFile )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT_POSITION_ENTRY_FILE, AutomaticAssignmentPlugin.getPlugin( ) ) )
        {
            daoUtil.setInt( 1, nIdTask );
            daoUtil.setInt( 2, nPositionEntryFile );
            daoUtil.executeUpdate( );
        }
    }
}
