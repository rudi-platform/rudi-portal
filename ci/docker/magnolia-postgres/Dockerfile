FROM postgres:15.20-bookworm
USER root

#COPY magnolia-dump.sql /docker-entrypoint-initdb.d

EXPOSE 5432

#RUN chmod a+wr /docker-entrypoint-initdb.d/magnolia-dump.sql

RUN echo "Installing and running Postgres for magnolia..."
#CMD ["/docker-entrypoint.sh"]
