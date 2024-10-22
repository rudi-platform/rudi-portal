-- Ajouter les nouvelles colonnes
ALTER TABLE strukture_data.organization
    ADD COLUMN IF NOT EXISTS assignee varchar(100);
ALTER TABLE strukture_data.organization
    ADD COLUMN IF NOT EXISTS creation_date timestamp NOT NULL DEFAULT now();
ALTER TABLE strukture_data.organization
    ADD COLUMN IF NOT EXISTS data varchar(255);
ALTER TABLE strukture_data.organization
    ADD COLUMN IF NOT EXISTS functional_status varchar(50) NOT NULL DEFAULT 'Acceptée';
ALTER TABLE strukture_data.organization
    ADD COLUMN IF NOT EXISTS organization_status varchar(50) NOT NULL DEFAULT 'VALIDATED';
ALTER TABLE strukture_data.organization
    ADD COLUMN IF NOT EXISTS initiator varchar(100) NOT NULL DEFAULT '';
ALTER TABLE strukture_data.organization
    ADD COLUMN IF NOT EXISTS process_definition_key varchar(150) NOT NULL DEFAULT '';
ALTER TABLE strukture_data.organization
    ADD COLUMN IF NOT EXISTS process_definition_version int4;
ALTER TABLE strukture_data.organization
    ADD COLUMN IF NOT EXISTS status varchar(20) NOT NULL DEFAULT 'COMPLETED';
ALTER TABLE strukture_data.organization
    ADD COLUMN IF NOT EXISTS updated_date timestamp;
ALTER TABLE strukture_data.organization
    ADD COLUMN IF NOT EXISTS updator varchar(100);
ALTER TABLE strukture_data.organization
    ADD COLUMN IF NOT EXISTS address varchar(255);
ALTER TABLE strukture_data.organization
    ADD COLUMN IF NOT EXISTS position ${geometrypointtype};


UPDATE strukture_data.organization
SET description = ''
WHERE description IS NULL;

-- Modifier les colonnes existantes
ALTER TABLE strukture_data.organization
    ALTER COLUMN description TYPE varchar(1024);
ALTER TABLE strukture_data.organization
    ALTER COLUMN description SET NOT NULL;

CREATE TABLE IF NOT EXISTS strukture_data.section_definition
(
    id         bigserial              NOT NULL,
    uuid       uuid                   NOT NULL,
    definition text                   NOT NULL,
    label      character varying(150),
    name       character varying(100) NOT NULL,
    help       character varying(300),
    CONSTRAINT section_definition_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS strukture_data.form_definition
(
    id   bigserial              NOT NULL,
    uuid uuid                   NOT NULL,
    name character varying(100) NOT NULL,
    CONSTRAINT form_definition_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS strukture_data.form_section_definition
(
    id                    bigserial NOT NULL,
    uuid                  uuid      NOT NULL,
    order_                integer   NOT NULL,
    read_only             boolean   NOT NULL,
    section_definition_fk bigint,
    form_definition_fk    bigint,
    CONSTRAINT form_section_definition_pkey PRIMARY KEY (id),
    CONSTRAINT fkjpx0jm21mae9injyxgeaavfs FOREIGN KEY (section_definition_fk)
        REFERENCES section_definition (id),
    CONSTRAINT fkjvk89pj3kxxr22ir9gc4dc4b3 FOREIGN KEY (form_definition_fk)
        REFERENCES form_definition (id)
);

CREATE TABLE IF NOT EXISTS strukture_data.process_form_definition
(
    id                    bigserial             NOT NULL,
    uuid                  uuid                  NOT NULL,
    process_definition_id character varying(64) NOT NULL,
    revision              integer,
    user_task_id          character varying(64),
    action_name           character varying(64),
    form_definition_fk    bigint,
    CONSTRAINT process_form_definition_pkey PRIMARY KEY (id),
    CONSTRAINT fka46uc2xkhmviwxnbbgudfi3n3 FOREIGN KEY (form_definition_fk)
        REFERENCES form_definition (id)
);