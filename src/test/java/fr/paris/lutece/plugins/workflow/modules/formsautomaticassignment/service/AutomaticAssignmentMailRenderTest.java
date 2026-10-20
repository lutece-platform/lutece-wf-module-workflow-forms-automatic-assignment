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
package fr.paris.lutece.plugins.workflow.modules.formsautomaticassignment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import fr.paris.lutece.portal.service.template.HtmlMarkup;
import freemarker.cache.StringTemplateLoader;
import freemarker.core.HTMLOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Renders the notification mail of the automatic assignment as AutomaticAssignmentService does, with HTML auto-escaping
 * (LUT-33153): the mail template of plugin-workflow writes the message, then the result is rendered again as a template
 * with the same model. The message (rich text, sanitized) and the link to the form response are markup and keep their
 * tags; the responses of the user (question_&lt;id&gt;) are escaped; the subject is plain text. Only the FreeMarker engine
 * is needed: the message is given already sanitized, as RichTextContentService.getMarkup returns it.
 */
public class AutomaticAssignmentMailRenderTest
{
    /** The mail template of plugin-workflow (admin/plugins/workflow/modules/notification/task_notification_mail.html). */
    private static final String TEMPLATE_MAIL = "task_notification_mail.html";
    private static final String TEMPLATE_MAIL_SOURCE = "<@div>${message}</@div>";
    /** Stand-in of the core macro the mail template calls. */
    private static final String MACROS = "macros.ftl";
    private static final String MACROS_SOURCE = "<#macro div rest...><div><#nested></div></#macro>";

    private static final String RESPONSE = "l'école <b>x</b> & \"co\"";
    private static final String RESPONSE_ESCAPED = "l&#39;école &lt;b&gt;x&lt;/b&gt; &amp; &quot;co&quot;";
    private static final String LINK = "<a href=\"http://localhost/lutece/jsp/admin/plugins/forms/ManageDirectoryFormResponseDetails.jsp?id_form_response=2\">See the response</a>";
    private static final String MESSAGE = "<p>Response: ${question_7!}</p><p>${link_view_form_response!}</p>";

    /**
     * The configuration of AbstractFreeMarkerTemplateService for HTML templates.
     *
     * @return the configuration
     */
    private static Configuration configuration( )
    {
        StringTemplateLoader loader = new StringTemplateLoader( );
        loader.putTemplate( MACROS, MACROS_SOURCE );
        loader.putTemplate( TEMPLATE_MAIL, TEMPLATE_MAIL_SOURCE );
        Configuration cfg = new Configuration( Configuration.VERSION_2_3_0 );
        cfg.setOutputFormat( HTMLOutputFormat.INSTANCE );
        cfg.setAutoEscapingPolicy( Configuration.ENABLE_IF_DEFAULT_AUTO_ESCAPING_POLICY );
        cfg.setTemplateLoader( loader );
        cfg.addAutoInclude( MACROS );

        return cfg;
    }

    /**
     * Renders a template given as a string, as AppTemplateService.getTemplateFromStringFtl does.
     *
     * @param cfg
     *            the configuration
     * @param strTemplate
     *            the template source
     * @param model
     *            the model
     * @return the rendering
     * @throws IOException
     *             if the template cannot be parsed
     * @throws TemplateException
     *             if the rendering fails
     */
    private static String renderString( Configuration cfg, String strTemplate, Map<String, Object> model ) throws IOException, TemplateException
    {
        StringWriter writer = new StringWriter( );
        new Template( "string", strTemplate, cfg ).process( model, writer );

        return writer.toString( );
    }

    /**
     * Renders the mail body as buildMailHtml does: the mail template, then its rendering as a template.
     *
     * @param model
     *            the model
     * @return the mail body
     * @throws IOException
     *             if a template cannot be read
     * @throws TemplateException
     *             if the rendering fails
     */
    private static String renderMail( Map<String, Object> model ) throws IOException, TemplateException
    {
        Configuration cfg = configuration( );
        StringWriter writer = new StringWriter( );
        cfg.getTemplate( TEMPLATE_MAIL ).process( model, writer );

        return renderString( cfg, writer.toString( ), model );
    }

    /**
     * The model buildModel builds: the responses as strings, the link and the message as markup.
     *
     * @param link
     *            the link to the form response
     * @return the model
     */
    private static Map<String, Object> model( Object link )
    {
        Map<String, Object> model = new HashMap<>( );
        model.put( "question_7", RESPONSE );
        model.put( "link_view_form_response", link );
        model.put( "message", HtmlMarkup.of( MESSAGE ) );

        return model;
    }

    /**
     * The mail keeps the tags of the message and of the link, and escapes the response of the user.
     *
     * @throws Exception
     *             if the rendering fails
     */
    @Test
    public void testMailKeepsTheMessageAndTheLinkAndEscapesTheResponses( ) throws Exception
    {
        String strMail = renderMail( model( HtmlMarkup.of( LINK ) ) );

        assertEquals( "<div><p>Response: " + RESPONSE_ESCAPED + "</p><p>" + LINK + "</p></div>", strMail );
    }

    /**
     * A link left as a String is escaped: the reason buildModel marks it.
     *
     * @throws Exception
     *             if the rendering fails
     */
    @Test
    public void testAStringLinkIsEscaped( ) throws Exception
    {
        String strMail = renderMail( model( LINK ) );

        assertFalse( strMail.contains( LINK ), strMail );
        assertTrue( strMail.contains( "&lt;a href=" ), strMail );
    }

    /**
     * The subject is a text: the entities auto-escaping writes for the response are decoded.
     *
     * @throws Exception
     *             if the rendering fails
     */
    @Test
    public void testSubjectIsPlainText( ) throws Exception
    {
        String strRendered = renderString( configuration( ), "Assignment ${question_7!}", model( HtmlMarkup.of( LINK ) ) );

        assertEquals( "Assignment " + RESPONSE_ESCAPED, strRendered );
        assertEquals( "Assignment " + RESPONSE, AutomaticAssignmentService.toPlainText( strRendered ) );
    }
}
