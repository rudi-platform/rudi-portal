FROM coudot/lemonldap-ng:2.0.14
# https://hub.docker.com/r/coudot/lemonldap-ng/tags
# https://github.com/LemonLDAPNG/lemonldap-ng-docker
ENV TZ=Europe/Paris

ENV SSODOMAIN=rennes-metropole-rudi.karbon.open.global
ENV LOGLEVEL=debug
ENV FASTCGI_LISTEN_PORT=9000

RUN printf "#!/bin/sh\nexit 0" > /usr/sbin/policy-rc.d
RUN chmod +x /usr/sbin/policy-rc.d

RUN cat /etc/apt/sources.list
RUN sed -i "s:stable:bullseye:g" /etc/apt/sources.list
RUN cat /etc/apt/sources.list

RUN apt-get update \
      && apt-get install -y liblasso-perl \
      && apt-get install -y slapd ldap-utils \
      && apt-get install -y certbot

COPY lmConf-1.json /var/lib/lemonldap-ng/conf
#COPY lemonldap-ng.ini /etc/lemonldap-ng/lemonldap-ng.ini
COPY portal-nginx.conf /etc/nginx/sites-available/portal-nginx.conf
COPY manager-nginx.conf /etc/nginx/sites-available/manager-nginx.conf
COPY api-nginx.conf /etc/nginx/sites-available/api-nginx.conf
COPY test-nginx.conf /etc/nginx/sites-available/test-nginx.conf
COPY rudi-nginx.conf /etc/nginx/sites-available/rudi-nginx.conf  

RUN sed -i "s/# server_names_hash_bucket_size 64;/ server_names_hash_bucket_size 128;/g" /etc/nginx/nginx.conf

EXPOSE 80
EXPOSE 9000
