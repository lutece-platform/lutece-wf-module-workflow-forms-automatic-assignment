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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.forms.business.Question;
import fr.paris.lutece.plugins.genericattributes.business.Entry;
import fr.paris.lutece.plugins.genericattributes.business.Field;

/**
 * Values proposed by the automatic assignment of a question
 */
public class AutomaticAssignmentJspBeanTest
{
    /**
     * Only the choices of the question are proposed, not the configuration fields of its entry
     */
    @Test
    public void testOnlyChoicesAreProposed( )
    {
        Entry entry = new Entry( );
        entry.setFields( Arrays.asList( newField( "mandatory_error_message", "" ), newField( "answer_choice", "Alpha" ), newField( "css_class", "" ),
                newField( "answer_choice", "Beta" ), newField( "max_choices", "-1" ) ) );
        Question question = new Question( );
        question.setEntry( entry );

        List<Field> listChoices = AutomaticAssignmentJspBean.getChoiceFields( question );

        assertEquals( 2, listChoices.size( ) );
        assertEquals( "Alpha", listChoices.get( 0 ).getTitle( ) );
        assertEquals( "Beta", listChoices.get( 1 ).getTitle( ) );
        assertTrue( AutomaticAssignmentJspBean.getChoiceFields( null ).isEmpty( ) );
    }

    /**
     * Builds a field
     *
     * @param strCode
     *            the code
     * @param strTitle
     *            the title
     * @return the field
     */
    private static Field newField( String strCode, String strTitle )
    {
        Field field = new Field( );
        field.setCode( strCode );
        field.setTitle( strTitle );
        return field;
    }
}
