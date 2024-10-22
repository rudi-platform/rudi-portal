CREATE SCHEMA IF NOT EXISTS facetbpmn_data;


-- V1 : Initialisation :
CREATE TABLE IF NOT EXISTS test_asset_description
(
    id                         bigserial    NOT NULL,
    uuid                       uuid         NOT NULL,
    process_definition_key     varchar(150) NOT NULL,
    process_definition_version int4,
    status                     varchar(20)  NOT NULL,
    functional_status          varchar(50)  NOT NULL,
    initiator                  varchar(50)  NOT NULL,
    updator                    varchar(50),
    creation_date              timestamp    NOT NULL,
    updated_date               timestamp    NOT NULL,
    description                varchar(1024),
    assignee                   varchar(100),
    data                       text,
    a                          varchar(12),
    PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS section_definition
(
    id         bigserial              NOT NULL,
    uuid       uuid                   NOT NULL,
    definition text                   NOT NULL,
    label      character varying(150) NOT NULL,
    name       character varying(100) NOT NULL,
    CONSTRAINT section_definition_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS form_definition
(
    id   bigserial              NOT NULL,
    uuid uuid                   NOT NULL,
    name character varying(100) NOT NULL,
    CONSTRAINT form_definition_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS form_section_definition
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

CREATE TABLE IF NOT EXISTS process_form_definition
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

-- V2 : RUDI-2353_Selfdata
-- Suppression du suffixe "_form" des formulaires
UPDATE form_definition
SET name = trim(TRAILING '__form' FROM name);

-- Suppression des labels des sections dont on ne souhaite pas voir la bordure apparaître (par défaut)
ALTER TABLE section_definition
    ALTER COLUMN "label" DROP NOT NULL;
-- noinspection SqlWithoutWhere
UPDATE section_definition
SET label = NULL;

-- Ajout du champ "aide" pour une section
ALTER TABLE section_definition
    ADD IF NOT EXISTS help character varying(150);


