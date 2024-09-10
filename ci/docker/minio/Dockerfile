FROM minio/minio:RELEASE.2024-08-17T01-24-54Z.fips
#https://hub.docker.com/r/minio/minio
MAINTAINER smartcnpf@cnpf.fr
ENV TZ=Europe/Paris

CMD ["minio", "server", "--console-address", ":9001", "/data"]

