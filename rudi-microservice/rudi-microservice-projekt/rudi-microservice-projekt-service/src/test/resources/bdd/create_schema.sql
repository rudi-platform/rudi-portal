CREATE SCHEMA if not exists projekt_data;


CREATE ALIAS if not exists unaccent AS $$
String unaccent2(String value){ return org.apache.commons.lang3.StringUtils.stripAccents(value);}
$$;