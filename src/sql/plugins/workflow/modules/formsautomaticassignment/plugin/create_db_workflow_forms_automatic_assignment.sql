DROP TABLE IF EXISTS workflow_forms_auto_assignment;
DROP TABLE IF EXISTS workflow_forms_auto_assignment_cf;
DROP TABLE IF EXISTS workflow_forms_auto_assignment_ef;

CREATE TABLE  workflow_forms_auto_assignment (
  id_task INT  NOT NULL ,
  id_question INT  NOT NULL ,
  value INT  NOT NULL ,
  workgroup_key varchar(255) NOT NULL,
  PRIMARY KEY  (id_task,id_question,value,workgroup_key)
);

CREATE TABLE  workflow_forms_auto_assignment_cf (
  id_task INT  NOT NULL ,
  id_form INT  NOT NULL ,
  title VARCHAR(255) DEFAULT NULL, 
  is_notify SMALLINT DEFAULT 0,
  sender_name VARCHAR(255) DEFAULT NULL, 
  message LONG VARCHAR DEFAULT NULL,
  subject VARCHAR(255) DEFAULT NULL,
  is_view_form_response SMALLINT DEFAULT 0 NOT NULL,
  label_link_view_form_response VARCHAR(255) DEFAULT NULL,
  recipients_cc VARCHAR(255) DEFAULT '' NOT NULL,
  recipients_bcc VARCHAR(255) DEFAULT '' NOT NULL,
  PRIMARY KEY  (id_task)
);

CREATE TABLE workflow_forms_auto_assignment_ef(
  id_task INT NOT NULL,
  position_form_question_file INT NOT NULL,
  PRIMARY KEY (id_task, position_form_question_file)
);
