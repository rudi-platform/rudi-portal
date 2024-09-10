-- Ajout de la colonne 'private_access'
ALTER TABLE projekt_data.confidentiality ADD COLUMN IF NOT EXISTS private_access BOOLEAN DEFAULT TRUE NOT NULL;

-- Ajout de la colonne 'description'
ALTER TABLE projekt_data.confidentiality ADD COLUMN IF NOT EXISTS description varchar(1024);

-- Insertion d'une nouvelle entrée
INSERT INTO projekt_data.confidentiality (uuid, code, label, description, closing_date, opening_date, order_, private_access)
VALUES ('ed5e9378-afa5-45b5-9fcf-07ebddc7ca36', 'PRIVATE', 'Privé', 'Si vous cochez cette case, votre réutilisation ne sera pas visible dans le catalogue Rudi. Seuls vous, l''équipe Rudi, ainsi que les producteurs de jeu de données mobilisés dans la réutilisation pourront consulter son détail ainsi que les contacts renseignés, et les jeux de données mobilisés.', NULL, NOW(), 3, TRUE);

-- Mise à jour de private_access pour le code 'OPEN'
UPDATE projekt_data.confidentiality SET private_access = FALSE WHERE code = 'OPEN';

-- Mise à jour du libelle de OPEN
UPDATE projekt_data.confidentiality SET label = 'Visible en ligne' WHERE code = 'OPEN';

-- Mise à jour de la description de OPEN
UPDATE projekt_data.confidentiality SET description = 'Si vous cochez cette case, votre réutilisation sera visible dans le catalogue Rudi. Tous les visiteurs de Rudi pourront consulter : votre réutilisation, les contacts renseignés ainsi que les jeux de données mobilisés.' WHERE code = 'OPEN';

-- Mise à jour de closing_date pour le code 'CONFIDENTIAL'
UPDATE projekt_data.confidentiality SET closing_date = NOW() WHERE code = 'CONFIDENTIAL';