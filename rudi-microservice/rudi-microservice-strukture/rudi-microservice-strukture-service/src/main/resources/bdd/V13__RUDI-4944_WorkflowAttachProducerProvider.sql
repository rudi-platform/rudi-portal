CREATE TABLE IF NOT EXISTS strukture_data.linked_producer
(
    id                         bigserial     NOT NULL,
    uuid                       uuid          NOT NULL,
    assignee                   varchar(100),
    creation_date              timestamp     NOT NULL,
    data                       varchar(255),
    description                varchar(1024) NOT NULL,
    functional_status          varchar(50)   NOT NULL,
    initiator                  varchar(100)  NOT NULL,
    process_definition_key     varchar(150)  NOT NULL,
    process_definition_version int4,
    status                     varchar(20)   NOT NULL,
    updated_date               timestamp,
    updator                    varchar(100),
    linked_producer_status     varchar(255)  NOT NULL,
    organization_fk            int8,
    provider_fk                int8,
    PRIMARY KEY (id)
);

ALTER TABLE IF EXISTS strukture_data.linked_producer
    ADD CONSTRAINT FKhwjpka5vi3cyif19lpcokti4u FOREIGN KEY (organization_fk) REFERENCES strukture_data.organization;
ALTER TABLE IF EXISTS strukture_data.linked_producer
    ADD CONSTRAINT FKgrhq5eeafynfp9lyphfg9hfwy FOREIGN KEY (provider_fk) REFERENCES strukture_data.provider;
