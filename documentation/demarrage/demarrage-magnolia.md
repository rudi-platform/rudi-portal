# Installation et configuration de Magnolia

> Note: La suite de cette documentation nécessite un minimum de connaissances concernant Docker et docker compose.

## Installation de Magnolia

Récupérer le war de Magnolia sur le [Nexus de **Magnolia CMS**](https://nexus.magnolia-cms.com/repository/magnolia.public.releases/info/magnolia/bundle/magnolia-community-webapp/6.2.48/magnolia-community-webapp-6.2.48.war). 

La version utilisée sur RUDI est indiquée dans le fichier [Dockerfile](/ci/docker/magnolia/Dockerfile).

Le placer dans le répertoire ``ci/docker/magnolia/`` sous le nom ``ROOT-6.2.48.tgz`` puis construire l'image :

```bash
mv magnolia-community-webapp-6.2.48.war <rudi-portal>/ci/docker/magnolia/ROOT-6.2.48.tgz
cd <rudi-portal>/ci/docker/magnolia/
docker image build -t magnolia 
```

Construire l'image magnolia-postgres
```bash
cd <rudi-portal>/ci/docker/magnolia-postgres/
docker image build -t magnolia-postgres
```

## Configuration de Magnolia

Nous préconisons d'utiliser Docker Compose pour la configuration et le démarrage l'ensemble des composants de Magnolia.

Un exemple de configuration Docker Compose est disponible dans ``ci/docker-compose/magnolia/docker-compose-magnolia.yml``.

Le fichier de docker-compose.yml doit être déposé dans le répertoire ``<magnolia_dir>``. Dans l'exemple fourni, ``<magnolia_dir>`` vaut ``/opt/magnolia``. 

Copier l'ensemble des fichiers et dossiers du répertoire ``ci/docker-compose/magnolia/config`` dans le répertoire ``<magnolia_dir>/config``

Les fichiers de configuration spécifiques à RUDI situés dans le répertoire ``/rudi-tools/rudi-tools-cms-magnolia/target/rudi-cms-magnolia.zip`` doivent être déposés dans ``<magnolia_dir>/modules/rudi``

Ce répertoire contient désormais les templates qui pourront être utilisés dans les pages de Magnolia.

# Démarrage de Magnolia 

Démarrer Magnolia à l'aide de docker compose :

```bash
cd <magnolia_dir>
docker compose up -d
```

## Création des catégories, pages...

_documentation à venir_


## Configuration des microservices pour l'accès à Magnolia

Dans les fichiers de properties des microservices nécessitant l'accès à Magnolia (Microservice Konsult), modifier la valeur de 

    cms.url
