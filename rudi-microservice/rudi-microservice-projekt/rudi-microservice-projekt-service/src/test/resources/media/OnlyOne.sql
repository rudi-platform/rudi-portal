Select u.id, r.label, u.type FROM acl_data.user_ u
JOIN strukture_data.organization o on u.login = CAST(o.uuid as varchar)
JOIN acl_data.user_role ur on u.id = ur.user_fk
JOIN acl_data.role r ON r.id = ur.role_fk LIMIT 20;
-- WHERE o.name like '%rydes';
select distinct(type) from acl_data.user_ inner join acl_data.user_role on user_role.user_fk = user_.id where user_role.role_fk = 20