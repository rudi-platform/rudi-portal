INSERT INTO apigateway_data.throttling (uuid, code, label, opening_date, order_, burst_capacity, rate) 
	SELECT 'a2bd7012-d27e-4dfb-bff2-03f5bda61494', 'BRONZE', 'Bronze', to_date('01/01/2024','DD/MM/YYYY'), 5, 5000, 1000 
	WHERE 'a2bd7012-d27e-4dfb-bff2-03f5bda61494' NOT IN (SELECT uuid from apigateway_data.throttling);
INSERT INTO apigateway_data.throttling (uuid, code, label, opening_date, order_, burst_capacity, rate) 
	SELECT '88e2439a-9bfc-4802-aab0-6c7b88fde6cc', 'SILVER', 'Silver', to_date('01/01/2024','DD/MM/YYYY'), 10, 10000, 5000
	WHERE '88e2439a-9bfc-4802-aab0-6c7b88fde6cc' NOT IN (SELECT uuid from apigateway_data.throttling);
INSERT INTO apigateway_data.throttling (uuid, code, label, opening_date, order_, burst_capacity, rate) 
	SELECT '6ff84e13-ec09-45c4-8155-441094de1aac', 'GOLD', 'Gold', to_date('01/01/2024','DD/MM/YYYY'), 15, 20000, 10000
	WHERE '6ff84e13-ec09-45c4-8155-441094de1aac' NOT IN (SELECT uuid from apigateway_data.throttling);
INSERT INTO apigateway_data.throttling (uuid, code, label, opening_date, order_, burst_capacity, rate) 
	SELECT 'fd5ea082-1747-44e8-86eb-196b011d811f', 'UNLIMITED', 'Unlimited', to_date('01/01/2024','DD/MM/YYYY'), 20, -1, -1 
	WHERE 'fd5ea082-1747-44e8-86eb-196b011d811f' NOT IN (SELECT uuid from apigateway_data.throttling);
