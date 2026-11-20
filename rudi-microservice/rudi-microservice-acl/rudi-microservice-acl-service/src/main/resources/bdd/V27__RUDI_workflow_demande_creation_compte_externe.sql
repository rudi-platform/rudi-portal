CREATE TABLE IF NOT EXISTS acl_data.external_account_request
(
    id                            bigserial     NOT NULL,
    uuid                          uuid          NOT NULL,
    assignee                      varchar(100),
    creation_date                 timestamp     NOT NULL,
    data                          varchar(255),
    description                   varchar(1024) NOT NULL,
    functional_status             varchar(50)   NOT NULL,
    initiator                     varchar(100)  NOT NULL,
    process_definition_key        varchar(150)  NOT NULL,
    process_definition_version    int4,
    status                        varchar(20)   NOT NULL,
    updated_date                  timestamp,
    updator                      varchar(100),
    lastname                      varchar(30),
    firstname                     varchar(30),
    email                         varchar(255)  NOT NULL,
    organization_uuid             uuid,
    organization_name             varchar(255),
    message                       varchar(1000),
    external_account_request_status varchar(20) NOT NULL,
    PRIMARY KEY (id)
);

ALTER TABLE IF EXISTS acl_data.external_account_request
    ADD CONSTRAINT uk_external_account_request_uuid UNIQUE (uuid);

