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
