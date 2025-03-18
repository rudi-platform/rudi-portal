FROM solr:9.8.1
USER root

RUN apt-get update && apt-get upgrade -y

EXPOSE 8983

# Owner
RUN chown -R solr:solr /tmp

# Ready !
USER solr
RUN echo "Installing and running Solr for dataverse..."
