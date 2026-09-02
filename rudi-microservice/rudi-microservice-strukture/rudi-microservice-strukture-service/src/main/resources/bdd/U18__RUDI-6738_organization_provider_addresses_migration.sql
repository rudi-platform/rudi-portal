-- ============================================================================
-- RUDI-6738 - Rollback migration addresses ManyToMany (U18)
-- ----------------------------------------------------------------------------
-- Detailed summary aligned with numbered steps below:
-- 1) Restore legacy abstract_address.provider_fk column.
-- 2) Recreate legacy FK abstract_address.provider_fk -> provider.id.
-- 3) Rebuild provider_fk values from provider_address links.
-- 4) Restore legacy organization.url column.
-- 5) Rebuild organization.url from WEBSITE CONTACT addresses.
-- 6) Delete WEBSITE addresses created by V18 deterministic UUID strategy.
-- 7) Drop ManyToMany link tables organization_address and provider_address.
-- 8) Cleanup CONTACT roles (PHONE/WEBSITE) if no address still references them.
--
-- Rollback notes:
-- - This script restores legacy schema columns/links used before V18.
-- - V18 WEBSITE rows are identified by UUID prefix "10000000-0000-0000-0000-".
-- - Role cleanup is protected by NOT EXISTS to avoid deleting referenced roles.
-- - This rollback is designed for migration flow compatibility.
-- ============================================================================
-- 1) Restore legacy provider_fk column on abstract_address
ALTER TABLE IF EXISTS strukture_data.abstract_address
    ADD COLUMN IF NOT EXISTS provider_fk int8;

-- 2) Recreate legacy provider FK constraint on abstract_address.provider_fk
ALTER TABLE IF EXISTS strukture_data.abstract_address
    DROP CONSTRAINT IF EXISTS abstract_address_provider_fk;

ALTER TABLE IF EXISTS strukture_data.abstract_address
    ADD CONSTRAINT abstract_address_provider_fk
        FOREIGN KEY (provider_fk) REFERENCES strukture_data.provider (id);

-- 3) Rebuild provider_fk values from provider_address links
--    If multiple providers are linked to one address, keep MIN(provider_fk) deterministically.
UPDATE strukture_data.abstract_address aa
SET provider_fk = src.provider_fk
FROM (
         SELECT pa.address_fk, MIN(pa.provider_fk) AS provider_fk
         FROM strukture_data.provider_address pa
         GROUP BY pa.address_fk
     ) src
WHERE aa.id = src.address_fk;

-- 4) Restore legacy organization.url column
ALTER TABLE IF EXISTS strukture_data.organization
    ADD COLUMN IF NOT EXISTS url VARCHAR(80);

-- 5) Rebuild organization.url from WEBSITE CONTACT addresses before dropping link tables
UPDATE strukture_data.organization o
SET url = src.url
FROM (
         SELECT oa.organization_fk,
                MAX(w.url) AS url
         FROM strukture_data.organization_address oa
                  JOIN strukture_data.abstract_address aa
                       ON aa.id = oa.address_fk AND aa.type = 'WEBSITE'
                  LEFT JOIN strukture_data.address_role ar
                            ON ar.id = aa.address_role_fk
                  JOIN strukture_data.web_site_address w
                       ON w.id = aa.id
         WHERE ar.code = 'CONTACT'
         GROUP BY oa.organization_fk
     ) src
WHERE o.id = src.organization_fk;

-- 6) Delete WEBSITE addresses created by V18 deterministic UUID strategy
--    First delete web_site_address payload rows, then abstract_address base rows.
DELETE FROM strukture_data.web_site_address w
USING strukture_data.abstract_address aa
WHERE w.id = aa.id
  AND aa.type = 'WEBSITE'
  AND CAST(aa.uuid AS VARCHAR) LIKE '10000000-0000-0000-0000-%';

DELETE FROM strukture_data.abstract_address aa
WHERE aa.type = 'WEBSITE'
  AND CAST(aa.uuid AS VARCHAR) LIKE '10000000-0000-0000-0000-%';

-- 7) Drop ManyToMany link tables created/used by V18
DROP TABLE IF EXISTS strukture_data.organization_address;
DROP TABLE IF EXISTS strukture_data.provider_address;

-- 8) Cleanup CONTACT roles created/used by V18, only if no remaining address references them
DELETE FROM strukture_data.address_role ar
WHERE ar.code = 'CONTACT'
  AND ar.type IN ('PHONE', 'WEBSITE')
  AND NOT EXISTS (
      SELECT 1
      FROM strukture_data.abstract_address aa
      WHERE aa.address_role_fk = ar.id
  );
