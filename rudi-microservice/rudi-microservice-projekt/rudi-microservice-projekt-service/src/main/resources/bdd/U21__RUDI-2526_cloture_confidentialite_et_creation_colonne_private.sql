-- Annuler la mise à jour de closing_date pour le code 'CONFIDENTIAL'
UPDATE projekt_data.confidentiality SET closing_date = NULL WHERE code = 'CONFIDENTIAL';

-- Mise à jour du libelle de OPEN
UPDATE projekt_data.confidentiality SET label = 'Open' WHERE code = 'OPEN';

-- Supprimer la colonne private_access :
ALTER TABLE projekt_data.confidentiality DROP COLUMN private_access;

-- Création d'une fausse confidentialité expirée pour la reprise de donnée
INSERT INTO projekt_data.confidentiality (id, uuid, code, label, closing_date, opening_date, order_)
VALUES (0, 'ad9e3b79-eba7-4c91-bff2-621a5553c2c4', 'NULL', 'NULL', '1900-01-01 00:00:00', '2024-07-02 13:51:02.181381', 1000);

-- Reprise de donnée pour éviter les erreurs
UPDATE projekt_data.project SET confidentiality_fk = 0 WHERE confidentiality_fk IN (
    SELECT id
    FROM projekt_data.confidentiality
    WHERE uuid = 'ed5e9378-afa5-45b5-9fcf-07ebddc7ca36'
);

-- Supprimer l'entrée insérée :
DELETE FROM projekt_data.confidentiality WHERE uuid = 'ed5e9378-afa5-45b5-9fcf-07ebddc7ca36';



-- Supprimer la colonne private_access :
ALTER TABLE projekt_data.confidentiality DROP COLUMN description;

