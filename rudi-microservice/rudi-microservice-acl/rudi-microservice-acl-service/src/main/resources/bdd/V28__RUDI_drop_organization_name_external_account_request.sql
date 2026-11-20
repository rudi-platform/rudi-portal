-- L'UUID de l'organisation (organization_uuid) suffit à référencer l'organisation côté strukture ;
-- le nom dénormalisé n'est jamais renseigné par le front (seul organization_uuid est transmis).
ALTER TABLE IF EXISTS acl_data.external_account_request
    DROP COLUMN IF EXISTS organization_name;

