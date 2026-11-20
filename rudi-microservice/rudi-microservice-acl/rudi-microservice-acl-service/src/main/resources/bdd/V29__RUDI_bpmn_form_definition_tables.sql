-- Inspiré de la V12 du micro service Strukture
CREATE TABLE IF NOT EXISTS acl_data.section_definition
(
    id         bigserial              NOT NULL,
    uuid       uuid                   NOT NULL,
    definition text                   NOT NULL,
    label      character varying(150),
    name       character varying(100) NOT NULL,
    help       character varying(300),
    CONSTRAINT section_definition_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS acl_data.form_definition
(
    id   bigserial              NOT NULL,
    uuid uuid                   NOT NULL,
    name character varying(100) NOT NULL,
    CONSTRAINT form_definition_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS acl_data.form_section_definition
(
    id                    bigserial NOT NULL,
    uuid                  uuid      NOT NULL,
    order_                integer   NOT NULL,
    read_only             boolean   NOT NULL,
    section_definition_fk bigint,
    form_definition_fk    bigint,
    CONSTRAINT form_section_definition_pkey PRIMARY KEY (id),
    CONSTRAINT fk_form_section_definition_section_definition FOREIGN KEY (section_definition_fk)
        REFERENCES acl_data.section_definition (id),
    CONSTRAINT fk_form_section_definition_form_definition FOREIGN KEY (form_definition_fk)
        REFERENCES acl_data.form_definition (id)
);

CREATE TABLE IF NOT EXISTS acl_data.process_form_definition
(
    id                    bigserial             NOT NULL,
    uuid                  uuid                  NOT NULL,
    process_definition_id character varying(64) NOT NULL,
    revision              integer,
    user_task_id          character varying(64),
    action_name           character varying(64),
    form_definition_fk    bigint,
    CONSTRAINT process_form_definition_pkey PRIMARY KEY (id),
    CONSTRAINT fk_process_form_definition_form_definition FOREIGN KEY (form_definition_fk)
        REFERENCES acl_data.form_definition (id)
);

-- Pour éviter les 'functional_status' vides, également inspiré de la V12 de Stukture
ALTER TABLE acl_data.external_account_request
    ALTER COLUMN functional_status SET DEFAULT 'Demande créée'::character varying;

UPDATE acl_data.external_account_request
SET functional_status = 'Demande créée'
WHERE functional_status = '';
