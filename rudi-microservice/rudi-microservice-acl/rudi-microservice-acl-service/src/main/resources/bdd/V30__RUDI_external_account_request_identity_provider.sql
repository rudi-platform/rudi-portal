ALTER TABLE IF EXISTS acl_data.external_account_request
    ADD COLUMN IF NOT EXISTS identity_provider character varying(100);
