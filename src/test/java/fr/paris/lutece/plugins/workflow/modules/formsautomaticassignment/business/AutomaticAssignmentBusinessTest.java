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

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.workflow.service.WorkflowPlugin;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.test.LuteceTestCase;
import jakarta.inject.Inject;

public class AutomaticAssignmentBusinessTest extends LuteceTestCase
{

	@Inject
    private IAutomaticAssignmentDAO _dao;
    private Plugin _plugin;

    @BeforeEach
    protected void setUp( ) throws Exception
    {
        super.setUp( );
        _plugin = PluginService.getPlugin( WorkflowPlugin.PLUGIN_NAME );
    }

    @Test
    public void testCRUD( )
    {
        AutomaticAssignment assignment = new AutomaticAssignment( );
        assignment.setIdField( 1 );
        assignment.setIdQuestion( 2 );
        assignment.setIdTask( 3 );
        assignment.setFieldValue( "strFieldValue" );
        assignment.setWorkgroupKey( "strWorkgroupKey" );

        _dao.insert( assignment, _plugin );

        assertTrue( _dao.checkExist( assignment, _plugin ) );

        List<AutomaticAssignment> list = _dao.loadByTask( 3, _plugin );
        assertEquals( 1, list.size( ) );
        AutomaticAssignment loaded = list.get( 0 );
        assertEquals( assignment.getIdField( ), loaded.getIdField( ) );
        assertEquals( assignment.getIdQuestion( ), loaded.getIdQuestion( ) );
        assertEquals( assignment.getIdField( ), loaded.getIdField( ) );
        assertEquals( assignment.getWorkgroupKey( ), loaded.getWorkgroupKey( ) );

        _dao.delete( assignment, _plugin );
        list = _dao.loadByTask( 3, _plugin );
        assertEquals( 0, list.size( ) );
    }
}
