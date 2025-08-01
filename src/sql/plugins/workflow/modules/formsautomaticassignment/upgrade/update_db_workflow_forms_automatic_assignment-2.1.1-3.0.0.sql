-- liquibase formatted sql
-- changeset workflow-formsautomaticassignment:update_db_workflow_forms_automatic_assignment-2.1.1-3.0.0.sql
-- preconditions onFail:MARK_RAN onError:WARN
DROP TABLE IF EXISTS workflow_forms_auto_assignment_ef;

CREATE TABLE workflow_forms_auto_assignment_ef(
  id_task INT NOT NULL,
  code_form_question_file varchar(100) NOT NULL,
  PRIMARY KEY (id_task, code_form_question_file)
);