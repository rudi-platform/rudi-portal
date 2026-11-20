FROM pgsty/silo:RELEASE.2026-10-26T00-00-00Z
#https://hub.docker.com/r/minio/minio
ENV TZ=Europe/Paris

CMD ["minio", "server", "--console-address", ":9001", "/data"]

