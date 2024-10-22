INSERT INTO strukture_data.address_role (uuid, code, label, opening_date, order_, type)
SELECT 'd9d85d7b-7c73-4203-bbc6-9f328f18bffb', 'CONTACT', 'Adresse de contact', '2024-10-14 15:05:33.676000', 0, 'EMAIL'
    WHERE 'CONTACTEMAIL' not in (select code || type from strukture_data.address_role);