insert into acl_data.role (uuid, code, label, opening_date, closing_date, order_) values 
 ( '39fe66f3-40d9-4510-8407-f083e7c1a367', 'MODULE_APIGATEWAY' ,'Module API Gateway' , timestamp '2025-01-01 01:00:00' , null, 160);
 
update acl_data.role set order_ = 150 where code = 'MODULE_APIGATEWAY_ADMINISTRATOR';

insert into acl_data.user_role( user_fk, role_fk) values
	( (select id from acl_data.user_ where login = 'apigateway'), (select id from acl_data.role where code = 'MODULE_APIGATEWAY'))
	;
	