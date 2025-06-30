# Installation et configuration de Dataverse

> Note: La suite de cette documentation nécessite un minimum de connaissances concernant Docker et docker compose.

## Installation de Dataverse

L'installation de Dataverse comprend 3 composants :
- Dataverse Engine
- Solr
- Postgres

Nous préconisons d'utiliser une image Docker pour chacun de ces composants. 

La suite de cette documentation indique comment construire les images.

### Construction de l'image de Dataverse Engine

Le fichier Dockerfile de Dataverse Engine est fourni dans ``ci/docker/dataverse-engine``.

```bash
cd ci/docker/dataverse-engine/
./build-image.sh
```

### Construction de l'image de Solr

Le fichier Dockerfile de Dataverse Engine est fourni dans ``ci/docker/dataverse-solr``.

```bash
cd ci/docker/dataverse-solr/
./build-image.sh
```

### Construction de l'image de postgres

L'image de postgres utilisée est l'image standard ``postgres:15.12-bookworm``.

## Configuration de Dataverse

Nous préconisons d'utiliser Docker Compose pour la configuration et le démarrage l'ensemble des composants de Dataverse.

Un exemple de configuration Docker Compose est disponible dans ``ci/docker-compose/dataverse/docker-compose.yml``.


Considérons que l'installation de dataverse se fait dans ``<dv_dir>``, ce fichier doit être copié dans ``<dv_dir>``.

Les fichiers de configuration suivants doivent être déposés dans le répertoire ``<dv_dir>/solr-data/collection1/conf``.
- ``ci/docker-compose/dataverse/solr-data/collection1/conf/schema_dv_mdb_copies.xml``
- ``ci/docker-compose/dataverse/solr-data/collection1/conf/schema_dv_mdb_fields.xml``
- ``ci/docker-compose/dataverse/solr-data/collection1/conf/schema.xml``
- ``ci/docker-compose/dataverse/solr-data/collection1/conf/solrconfig.xml``

## Démarrage de Dataverse

Démarrer Dataverse à l'aide de docker compose :

```bash
cd <dv_dir>
docker compose up -d
```

## Configuration des microservices pour l'accès à Dataverse

Dans les fichiers de properties des microservices nécessitant l'accès à Dataverse, modifier les valeurs de 

    dataverse.host
    dataverse.api.token

La valeur du token peut être générée ou retrouvée dans l'IHM d'administration de Dataverse (sur le port 8095 par défaut), menu *Dataverse Admin* / *API Token*
