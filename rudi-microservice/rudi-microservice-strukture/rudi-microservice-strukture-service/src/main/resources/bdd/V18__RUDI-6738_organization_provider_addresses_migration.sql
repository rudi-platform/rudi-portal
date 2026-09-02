-- ============================================================================
-- RUDI-6738 - Organization/Provider addresses migration to ManyToMany
-- ----------------------------------------------------------------------------
-- Detailed summary aligned with numbered steps below:
-- 1) Create table strukture_data.provider_address.
-- 2) Create table strukture_data.organization_address.
-- 3) Add FK provider_address.provider_fk -> provider.id (ON DELETE CASCADE).
-- 4) Add FK provider_address.address_fk -> abstract_address.id (ON DELETE CASCADE).
-- 5) Add FK organization_address.organization_fk -> organization.id (ON DELETE CASCADE).
-- 6) Add FK organization_address.address_fk -> abstract_address.id (ON DELETE CASCADE).
-- 7) Backfill provider_address from legacy abstract_address.provider_fk.
-- 8) Ensure CONTACT/WEBSITE and CONTACT/PHONE roles exist in address_role.
-- 9) Insert WEBSITE rows into abstract_address from organization.url.
-- 10) Insert corresponding rows into web_site_address.
-- 11) Insert organization_address links for migrated WEBSITE rows.
-- 12) Drop legacy abstract_address.provider_fk relation and column.
-- 13) Drop legacy organization.url column.
--
-- Idempotency notes:
-- - Table creation and drop operations use IF EXISTS / IF NOT EXISTS.
-- - Backfill/link inserts use NOT EXISTS filters.
-- - WEBSITE migration uses deterministic UUIDs based on organization.id.
-- - Constraint ADD operations are expected to run once in migration flow.
-- ============================================================================

-- 1) Create join table for provider <-> address links (ManyToMany)
CREATE TABLE IF NOT EXISTS strukture_data.provider_address
(
    provider_fk int8 NOT NULL,
    address_fk  int8 NOT NULL,
    CONSTRAINT provider_address_pk PRIMARY KEY (provider_fk, address_fk)
);

-- 2) Create join table for organization <-> address links (ManyToMany)
CREATE TABLE IF NOT EXISTS strukture_data.organization_address
(
    organization_fk int8 NOT NULL,
    address_fk      int8 NOT NULL,
    CONSTRAINT organization_address_pk PRIMARY KEY (organization_fk, address_fk)
);

-- 3) Add FK provider_address.provider_fk -> provider.id (cascade delete)
ALTER TABLE IF EXISTS strukture_data.provider_address
    ADD CONSTRAINT provider_address_provider_fk
        FOREIGN KEY (provider_fk) REFERENCES strukture_data.provider (id) ON DELETE CASCADE;

-- 4) Add FK provider_address.address_fk -> abstract_address.id (cascade delete)
ALTER TABLE IF EXISTS strukture_data.provider_address
    ADD CONSTRAINT provider_address_address_fk
        FOREIGN KEY (address_fk) REFERENCES strukture_data.abstract_address (id) ON DELETE CASCADE;

-- 5) Add FK organization_address.organization_fk -> organization.id (cascade delete)
ALTER TABLE IF EXISTS strukture_data.organization_address
    ADD CONSTRAINT organization_address_organization_fk
        FOREIGN KEY (organization_fk) REFERENCES strukture_data.organization (id) ON DELETE CASCADE;

-- 6) Add FK organization_address.address_fk -> abstract_address.id (cascade delete)
ALTER TABLE IF EXISTS strukture_data.organization_address
    ADD CONSTRAINT organization_address_address_fk
        FOREIGN KEY (address_fk) REFERENCES strukture_data.abstract_address (id) ON DELETE CASCADE;

-- 7) Backfill provider_address from legacy abstract_address.provider_fk
--    Only insert missing pairs to keep script rerunnable.
INSERT INTO strukture_data.provider_address (provider_fk, address_fk)
SELECT aa.provider_fk, aa.id
FROM strukture_data.abstract_address aa
WHERE aa.provider_fk IS NOT NULL
  AND NOT EXISTS(
        SELECT 1
        FROM strukture_data.provider_address pa
        WHERE pa.provider_fk = aa.provider_fk
          AND pa.address_fk = aa.id
    );

-- 8) Ensure CONTACT roles exist for WEBSITE and PHONE addresses (idempotent)
INSERT INTO strukture_data.address_role (uuid, code, label, opening_date, order_, type)
SELECT CAST('2b6a90df-5d11-4f40-a3db-b8c0782f6e2a' AS UUID), 'CONTACT', 'Adresse de contact WEBSITE', CURRENT_TIMESTAMP, 0, 'WEBSITE'
WHERE NOT EXISTS (
    SELECT 1
    FROM strukture_data.address_role ar
    WHERE ar.code = 'CONTACT'
      AND ar.type = 'WEBSITE'
);

INSERT INTO strukture_data.address_role (uuid, code, label, opening_date, order_, type)
SELECT CAST('7f5d29d8-2f8c-4f92-9cd9-17a59e8b12af' AS UUID), 'CONTACT', 'Adresse de contact PHONE', CURRENT_TIMESTAMP, 0, 'PHONE'
WHERE NOT EXISTS (
    SELECT 1
    FROM strukture_data.address_role ar
    WHERE ar.code = 'CONTACT'
      AND ar.type = 'PHONE'
);

-- 9) Migrate organization.url into abstract_address(type=WEBSITE)
--    One row per organization URL; UUID is deterministic from organization.id.
--    This avoids duplicate UUID issues and supports reruns.
INSERT INTO strukture_data.abstract_address (uuid, type, address_role_fk)
SELECT CAST('10000000-0000-0000-0000-' || LPAD(CAST(o.id AS VARCHAR), 12, '0') AS UUID),
       'WEBSITE',
       (
           SELECT ar.id
           FROM strukture_data.address_role ar
           WHERE ar.code = 'CONTACT'
             AND ar.type = 'WEBSITE'
           ORDER BY ar.closing_date NULLS FIRST, ar.opening_date DESC
           LIMIT 1
       )
FROM strukture_data.organization o
WHERE o.url IS NOT NULL
  AND btrim(o.url) <> '';

-- 10) Insert payload rows into web_site_address using the same deterministic UUID key
INSERT INTO strukture_data.web_site_address (id, url)
SELECT aa.id, o.url
FROM strukture_data.organization o
         JOIN strukture_data.abstract_address aa
              ON aa.uuid = CAST('10000000-0000-0000-0000-' || LPAD(CAST(o.id AS VARCHAR), 12, '0') AS UUID)
                  AND aa.type = 'WEBSITE'
WHERE o.url IS NOT NULL
  AND btrim(o.url) <> '';

-- 11) Link organization to migrated website addresses (idempotent join insert)
INSERT INTO strukture_data.organization_address (organization_fk, address_fk)
SELECT o.id, aa.id
FROM strukture_data.organization o
         JOIN strukture_data.abstract_address aa
              ON aa.uuid = CAST('10000000-0000-0000-0000-' || LPAD(CAST(o.id AS VARCHAR), 12, '0') AS UUID)
                  AND aa.type = 'WEBSITE'
WHERE o.url IS NOT NULL
  AND btrim(o.url) <> ''
  AND NOT EXISTS(
        SELECT 1
        FROM strukture_data.organization_address oa
        WHERE oa.organization_fk = o.id
          AND oa.address_fk = aa.id
    );

-- 12) Remove legacy provider 1-N relation from abstract_address
ALTER TABLE IF EXISTS strukture_data.abstract_address
    DROP CONSTRAINT IF EXISTS fk14sktnct3mlngr0kuqxqmwgxi;

ALTER TABLE IF EXISTS strukture_data.abstract_address
    DROP COLUMN IF EXISTS provider_fk;

-- 13) Remove legacy organization.url column (data is now in address tables)
ALTER TABLE IF EXISTS strukture_data.organization
    DROP COLUMN IF EXISTS url;
