FROM bitnami/openldap:2.5.11
# https://hub.docker.com/r/bitnami/openldap
# https://github.com/bitnami/bitnami-docker-openldap/tree/master/2.5/debian-10
USER root
ENV TZ=Europe/Paris

RUN mkdir -p /schemas
#COPY 00_ldap_schema.ldif /schemas/schema.ldif
RUN chmod -R a+rwx /schemas

RUN mkdir -p /ldifs
COPY 01_ldap_ou.ldif /ldifs/01_ldap_ou.ldif
COPY 02_ldap_users.ldif /ldifs/02_ldap_users.ldif
COPY 03_ldap_groups.ldif /ldifs/04_ldap_groups.ldif
RUN chmod -R a+rwx /ldifs

ENV LDAP_PORT_NUMBER=1389
ENV LDAP_ROOT=dc=rudi,dc=fr
ENV LDAP_ADMIN_USERNAME=admin
ENV LDAP_ADMIN_PASSWORD=rudi@123

#ENV LDAP_CONFIG_ADMIN_ENABLED=yes
#ENV LDAP_CONFIG_ADMIN_USERNAME=admin
#ENV LDAP_CONFIG_ADMIN_PASSWORD: LDAP configuration admin password. Default: configpassword.

#ENV LDAP_USERS: Comma separated list of LDAP users to create in the default LDAP tree. Default: user01,user02
#ENV LDAP_PASSWORDS: Comma separated list of passwords to use for LDAP users. Default: bitnami1,bitnami2
#ENV LDAP_USER_DC: DC for the users' organizational unit. Default: users
#ENV LDAP_GROUP: Group used to group created users. Default: readers

ENV LDAP_ADD_SCHEMAS=yes

# Extra schemas to add, among OpenLDAP's distributed schemas. Default: cosine, inetorgperson, nis
#ENV LDAP_EXTRA_SCHEMAS=cosine, inetorgperson, nis

# Location of a custom internal schema file that could not be added as custom ldif file (i.e. containing some structuralObjectClass). Default is /schema/custom.ldif"
ENV LDAP_CUSTOM_SCHEMA_FILE=/schemas/schema.ldif

ENV LDAP_SKIP_DEFAULT_TREE=yes

# Location of a directory that contains LDIF files that should be used to bootstrap the database. Only files ending in .ldif will be used. Default LDAP tree based on the LDAP_USERS, LDAP_PASSWORDS, LDAP_USER_DC and LDAP_GROUP will be skipped when LDAP_CUSTOM_LDIF_DIR is used. When using this it will override the usage of LDAP_USERS, LDAP_PASSWORDS, LDAP_USER_DC and LDAP_GROUP. You should set LDAP_ROOT to your base to make sure the olcSuffix configured on the database matches the contents imported from the LDIF files. Default: /ldifs
ENV LDAP_CUSTOM_LDIF_DIR=/ldifs

#ENV LDAP_ULIMIT_NOFILES: Maximum number of open file descriptors. Default: 1024.
#ENV LDAP_ALLOW_ANON_BINDING: Allow anonymous bindings to the LDAP server. Default: yes.
#ENV LDAP_LOGLEVEL: Set the loglevel for the OpenLDAP server (see https://www.openldap.org/doc/admin25/slapdconfig.html for possible values). Default: 256.
ENV LDAP_LOGLEVEL=256

USER 1001