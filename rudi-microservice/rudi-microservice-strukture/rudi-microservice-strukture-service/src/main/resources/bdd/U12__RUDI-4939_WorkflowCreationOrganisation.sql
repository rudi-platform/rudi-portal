-- Revenir aux anciennes contraintes et types de colonnes
ALTER TABLE strukture_data.organization
    ALTER COLUMN description TYPE varchar(800),
    ALTER COLUMN description DROP NOT NULL;

-- Supprimer les colonnes ajoutées lors de la migration
ALTER TABLE strukture_data.organization
    DROP COLUMN IF EXISTS assignee,
    DROP COLUMN IF EXISTS creation_date,
    DROP COLUMN IF EXISTS data,
    DROP COLUMN IF EXISTS functional_status,
    DROP COLUMN IF EXISTS organization_status,
    DROP COLUMN IF EXISTS initiator,
    DROP COLUMN IF EXISTS process_definition_key,
    DROP COLUMN IF EXISTS process_definition_version,
    DROP COLUMN IF EXISTS status,
    DROP COLUMN IF EXISTS updated_date,
    DROP COLUMN IF EXISTS adresse,
    DROP COLUMN IF EXISTS coordonnees,
    DROP COLUMN IF EXISTS updator;

DROP TABLE IF EXISTS strukture_data.form_section_definition;
DROP TABLE IF EXISTS strukture_data.process_form_definition;
DROP TABLE IF EXISTS strukture_data.section_definition;
DROP TABLE IF EXISTS strukture_data.form_definition;
