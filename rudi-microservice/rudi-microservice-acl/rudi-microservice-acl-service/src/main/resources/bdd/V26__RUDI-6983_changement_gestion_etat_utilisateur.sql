ALTER TABLE acl_data.user_ ADD account_state varchar(255) NOT NULL DEFAULT 'ACTIVE';

UPDATE acl_data.user_ SET account_state = 'LOCKED' WHERE account_locked = true;

ALTER TABLE acl_data.user_ DROP COLUMN account_locked;
