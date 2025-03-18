CREATE SEQUENCE IF NOT EXISTS public.license_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1;

ALTER SEQUENCE public.license_id_seq OWNER TO dataverse;

CREATE TABLE IF NOT EXISTS public.license
(
    id integer NOT NULL DEFAULT nextval('public.license_id_seq'::regclass),
    active boolean NOT NULL,
    iconurl text COLLATE pg_catalog."default",
    isdefault boolean NOT NULL,
    name text COLLATE pg_catalog."default",
    shortdescription text COLLATE pg_catalog."default",
    sortorder bigint NOT NULL DEFAULT 0,
    uri text COLLATE pg_catalog."default",
    CONSTRAINT license_pkey PRIMARY KEY (id),
    CONSTRAINT unq_license_0 UNIQUE (name),
    CONSTRAINT unq_license_1 UNIQUE (uri)
    );

ALTER TABLE IF EXISTS public.license OWNER to dataverse;
ALTER SEQUENCE public.license_id_seq OWNED BY public.license.id;

CREATE INDEX IF NOT EXISTS license_sortorder_id ON public.license USING btree (sortorder ASC NULLS LAST, id ASC NULLS LAST);

CREATE SEQUENCE IF NOT EXISTS public.embargo_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1;

ALTER SEQUENCE public.embargo_id_seq OWNER TO dataverse;

CREATE TABLE IF NOT EXISTS public.embargo
(
    id integer NOT NULL DEFAULT nextval('public.embargo_id_seq'::regclass),
    dateavailable date NOT NULL,
    reason text COLLATE pg_catalog."default",
    CONSTRAINT embargo_pkey PRIMARY KEY (id)
    );

ALTER TABLE IF EXISTS public.embargo OWNER to dataverse;
ALTER SEQUENCE public.embargo_id_seq OWNED BY public.embargo.id;