#FROM postgres:15.12-bookworm
FROM postgres:10.23-bullseye
USER root

COPY dataverse-dump.sql /docker-entrypoint-initdb.d

EXPOSE 5432

RUN apt-get update && apt-get upgrade -y

RUN chmod a+wr /docker-entrypoint-initdb.d/dataverse-dump.sql

RUN echo "Installing and running Postgres for dataverse..."
#CMD ["/docker-entrypoint.sh"]
