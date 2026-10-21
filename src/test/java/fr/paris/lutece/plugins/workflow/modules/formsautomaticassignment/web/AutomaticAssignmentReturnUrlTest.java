/*
 * Copyright (c) 2002-2026, City of Paris
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

import java.util.Locale;

import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.workflow.modules.formsautomaticassignment.service.TaskAutomaticAssignment;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.test.LuteceTestCase;
import fr.paris.lutece.test.mocks.MockHttpServletRequest;
import jakarta.inject.Inject;

/**
 * Error messages of the automatic assignment back office
 */
public class AutomaticAssignmentReturnUrlTest extends LuteceTestCase
{
    @Inject
    private AutomaticAssignmentTaskComponent _taskComponent;

    /**
     * A configuration of the task without title is refused: the button of the message opens the configuration of the task, even in a new tab
     */
    @Test
    public void testSaveConfigWithoutTitleReturnsToTheConfigurationOfTheTask( )
    {
        MockHttpServletRequest request = new MockHttpServletRequest( );
        TaskAutomaticAssignment task = new TaskAutomaticAssignment( );
        task.setId( 42 );

        _taskComponent.doSaveConfig( request, Locale.FRENCH, task );

        String strBackUrl = AdminMessageService.getMessage( request ).getBackUrl( );
        assertTrue( strBackUrl.endsWith( "jsp/admin/plugins/workflow/ModifyTask.jsp?id_task=42" ), strBackUrl );
    }

    /**
     * An assignment without value is refused: the button of the message opens the assignments of the question, even in a new tab
     */
    @Test
    public void testAddAssignmentWithoutValueReturnsToTheAssignmentsOfTheQuestion( )
    {
        MockHttpServletRequest request = new MockHttpServletRequest( );
        request.addParameter( "id_task", "42" );
        request.addParameter( "id_question", "7" );

        new AutomaticAssignmentJspBean( ).doAddAssignment( request );

        String strBackUrl = AdminMessageService.getMessage( request ).getBackUrl( );
        assertTrue( strBackUrl.endsWith( "jsp/admin/plugins/workflow/modules/formsautomaticassignment/ModifyQuestionAssignment.jsp?id_task=42&id_question=7" ),
                strBackUrl );
    }
}
