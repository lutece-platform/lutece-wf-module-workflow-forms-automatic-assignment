-- liquibase formatted sql
-- changeset workflow-formsautomaticassignment:update_db_workflow_forms_automatic_assignment-3.0.0-3.0.1.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- LUT-33153 (FreeMarker HTML auto-escaping): the mail message, the mail subject and the label of the link to the form
-- response of workflow_forms_auto_assignment_cf are FreeMarker templates (with the question_<id> markers of the user's
-- responses) rendered with HTML auto-escaping. Hand escaping (?html, ?xhtml, ?xml, ?rtf, ?web_safe) no longer parses: the
-- notification would fail at run time. It is removed; auto-escaping escapes the marker values of the message, and the
-- subject is written unescaped.
-- This script rewrites site data (table workflow_forms_auto_assignment_cf, the 3 columns below). Idempotent: a value
-- without these built-ins is not updated.
-- Not handled, to review by hand: <#escape>/<#noescape> directives, and a built-in inside a <script> of a message (it
-- needs ?js_string there). To list them before the upgrade:
--   SELECT id_task FROM workflow_forms_auto_assignment_cf WHERE message LIKE '%<#escape%' OR message LIKE '%<script%';

UPDATE workflow_forms_auto_assignment_cf SET message = REPLACE( REPLACE( REPLACE( REPLACE( REPLACE( message, '?html', '' ), '?xhtml', '' ), '?xml', '' ), '?rtf', '' ), '?web_safe', '' )
  WHERE message LIKE '%?html%' OR message LIKE '%?xhtml%' OR message LIKE '%?xml%' OR message LIKE '%?rtf%' OR message LIKE '%?web_safe%';
UPDATE workflow_forms_auto_assignment_cf SET subject = REPLACE( REPLACE( REPLACE( REPLACE( REPLACE( subject, '?html', '' ), '?xhtml', '' ), '?xml', '' ), '?rtf', '' ), '?web_safe', '' )
  WHERE subject LIKE '%?html%' OR subject LIKE '%?xhtml%' OR subject LIKE '%?xml%' OR subject LIKE '%?rtf%' OR subject LIKE '%?web_safe%';
UPDATE workflow_forms_auto_assignment_cf SET label_link_view_form_response = REPLACE( REPLACE( REPLACE( REPLACE( REPLACE( label_link_view_form_response, '?html', '' ), '?xhtml', '' ), '?xml', '' ), '?rtf', '' ), '?web_safe', '' )
  WHERE label_link_view_form_response LIKE '%?html%' OR label_link_view_form_response LIKE '%?xhtml%' OR label_link_view_form_response LIKE '%?xml%' OR label_link_view_form_response LIKE '%?rtf%' OR label_link_view_form_response LIKE '%?web_safe%';

-- changeset workflow-formsautomaticassignment:update_db_workflow_forms_automatic_assignment-3.0.0-3.0.1.sql-decode-entities
-- preconditions onFail:MARK_RAN onError:WARN
-- comment decodes, once, the HTML entities the removed input filter of lutece-core 8 (sanitizeFilterMode, LUT-34109) wrote into the plain text values: the title, the sender, the subject, the recipients and the label of the link to the form response of the task configuration. The mail message, typed with the rich text editor, is left as it is.
-- This changeset rewrites the data of the site. Back up the database before the upgrade. It must run ONCE only: run
-- again, it would decode a second time a text that really holds an entity (a typed &lt; stored as &amp;lt; becomes &lt;
-- once, then < the second time). Only the values matching LIKE '%&%;%' are concerned; to list them before the upgrade,
-- run the WHERE clauses below in a SELECT.
UPDATE workflow_forms_auto_assignment_cf SET title = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(title, '&#39;', ''''), '&#x27;', ''''), '&#34;', '"'), '&quot;', '"'), '&#96;', '`'), '&#61;', '='), '&#64;', '@'), '&#43;', '+'), '&lt;', '<'), '&gt;', '>'), '&amp;', '&') WHERE title LIKE '%&%;%';
UPDATE workflow_forms_auto_assignment_cf SET sender_name = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(sender_name, '&#39;', ''''), '&#x27;', ''''), '&#34;', '"'), '&quot;', '"'), '&#96;', '`'), '&#61;', '='), '&#64;', '@'), '&#43;', '+'), '&lt;', '<'), '&gt;', '>'), '&amp;', '&') WHERE sender_name LIKE '%&%;%';
UPDATE workflow_forms_auto_assignment_cf SET subject = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(subject, '&#39;', ''''), '&#x27;', ''''), '&#34;', '"'), '&quot;', '"'), '&#96;', '`'), '&#61;', '='), '&#64;', '@'), '&#43;', '+'), '&lt;', '<'), '&gt;', '>'), '&amp;', '&') WHERE subject LIKE '%&%;%';
UPDATE workflow_forms_auto_assignment_cf SET label_link_view_form_response = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(label_link_view_form_response, '&#39;', ''''), '&#x27;', ''''), '&#34;', '"'), '&quot;', '"'), '&#96;', '`'), '&#61;', '='), '&#64;', '@'), '&#43;', '+'), '&lt;', '<'), '&gt;', '>'), '&amp;', '&') WHERE label_link_view_form_response LIKE '%&%;%';
UPDATE workflow_forms_auto_assignment_cf SET recipients_cc = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(recipients_cc, '&#39;', ''''), '&#x27;', ''''), '&#34;', '"'), '&quot;', '"'), '&#96;', '`'), '&#61;', '='), '&#64;', '@'), '&#43;', '+'), '&lt;', '<'), '&gt;', '>'), '&amp;', '&') WHERE recipients_cc LIKE '%&%;%';
UPDATE workflow_forms_auto_assignment_cf SET recipients_bcc = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(recipients_bcc, '&#39;', ''''), '&#x27;', ''''), '&#34;', '"'), '&quot;', '"'), '&#96;', '`'), '&#61;', '='), '&#64;', '@'), '&#43;', '+'), '&lt;', '<'), '&gt;', '>'), '&amp;', '&') WHERE recipients_bcc LIKE '%&%;%';
