# Installation de Rudi-Portal

## Généralités

Le portail Rudi peut être installé de différentes manières, mais en raison du nombre élevé de microservices, une installation de type container est recommandée (par exemple, avec Kubernetes ou Docker Compose).

L'installation est composée de quatre étapes successives :

1. Installation des prérequis.
2. Installation des microservices RUDI.
3. Installation du catalogue des jeux de données Dataverse.
4. Installation de l'API Manager WSO2.

## Prérequis

Pour installer sur un système Linux Debian, les logiciels suivants doivent être installés :

- Maven
- JDK 11
- Git
- XMLStarlet

## Installation des microservices RUDI

### 1. Clonage du projet

Cloner le projet depuis le repository suivant :

```bash
git clone https://github.com/sigrennesmetropole/rudi_portal](https://github.com/Rudi-pages-WIP/Rudi-Portal
cd rudi_portal
```

### 2. Construction du projet

Construire le projet avec Maven en utilisant la commande suivante :

```bash
mvn package -Pdev-docker,prod -Dmaven.javadoc.skip=true -DskipTests
```

*Remarque :* À l'issue de cette opération, les fichiers JAR des microservices seront copiés dans les répertoires de construction des images (`<root-rudi>/ci/docker`).

### 3. Lancement du script d'installation

Exécuter le script d'installation :

```bash
cd <root-rudi>/ci/docker-compose
sudo buildDockerImage.sh
```

*Remarque :* Après cette exécution, les images des différents microservices Rudi seront construites.

### 4. Configuration des fichiers de propriétés

Éditer les fichiers de propriétés présents dans `<root-rudi>/ci/docker-compose/config/` et mettre à jour les propriétés en conséquence.

### 5. Démarrage des services RUDI

Démarrer les services RUDI à l'aide du fichier `docker-compose.yml` fourni.

*Remarque :*
- Chaque microservice utilise son propre schéma de données.
- Au démarrage, les scripts de création des tables seront exécutés automatiquement par chaque microservice.
- Il est nécessaire de créer les rôles et schémas de chaque microservice dans la base de données.
- Les schémas attendus sont : `acl_data`, `kalim_data`, `konsent_data`, `kos_data`, `projekt_data`, `selfdata_data`, `strukture_data`, `template_data`.

## Installation de Dataverse

L'installation de Dataverse s'appuie sur le repository `https://github.com/IQSS/dataverse-docker` dans la version `v5.5`.

### 1. Clonage du projet

Se positionner dans le répertoire `<root-rudi>/ci/docker-compose/dataverse` :

```bash
git clone https://github.com/IQSS/dataverse-docker.git
cd dataverse-docker
git checkout v5.5
```

### 2. Modification des définitions des métadonnées

Copier les fichiers suivants pour modifier les définitions des métadonnées :

```bash
cp <root-rudi>/ci/docker-compose/solr-data/collection1/conf/schema_dv_mdb_copies.xml <solr_data>/schema_dv_mdb_copies.xml
cp <root-rudi>/ci/docker-compose/solr-data/collection1/conf/schema_dv_mdb_fields.xml <solr_data>/schema_dv_mdb_fields.xml
cp <root-rudi>/ci/docker-compose/solr-data/collection1/conf/schema.xml <solr_data>/schema.xml
cp <root-rudi>/ci/docker-compose/docker-compose.yml dataverse/docker-compose.yml
```

### 3. Exécution du script dataverse

Exécuter le script suivant pour prendre en compte les nouvelles définitions :

```bash
./updateSchemaMDB.sh -d http://localhost:{{dataverse_port}} -t {{solr_data_conf_directory}}
```

## Installation de WSO2

L'installation de WSO2 s'appuie sur le repository `https://github.com/wso2/docker-apim.git` dans la version `3.2.0.1`.

### 1. Clonage du projet

Se positionner dans le répertoire `<root-rudi>/ci/docker-compose/wso2` :

```bash
git clone https://github.com/wso2/docker-apim.git
cd docker-apim
git checkout 3.2.0.1
```

### 2. Création de la structure de configuration

Se positionner dans le répertoire `apim-with-analytics` et créer la structure suivante dans le répertoire `conf` :

```bash
mkdir -p conf/apim/repository/components/dropins
mkdir -p conf/apim/repository/components/lib
mkdir -p conf/apim/repository/conf
mkdir -p conf/apim/repository/deployment/server/userstores
mkdir -p conf/apim/repository/resources/api_templates
```

Copier les fichiers requis :

```bash
cp <path_to_files>/org.rudi.wso2.handler.properties conf/apim
cp <path_to_files>/org.rudi.wso2.userstore.jar conf/apim/repository/components/dropins
cp <path_to_files>/postgresql-42.2.18.jar conf/apim/repository/components/dropins
cp <path_to_files>/org.rudi.wso2.handler.jar conf/apim/repository/components/lib
cp <path_to_files>/rudi-common-core.jar conf/apim/repository/components/lib
cp <path_to_files>/rudi-facet-crypto.jar conf/apim/repository/components/lib
cp <path_to_files>/deployment.toml conf/apim/repository/conf
cp <path_to_files>/deployment.toml.mail conf/apim/repository/conf
cp <path_to_files>/encryption_key.key conf/apim/repository/conf
cp <path_to_files>/log4j2.properties conf/apim/repository/conf
cp <path_to_files>/user-mgt.xml conf/apim/repository/conf
cp <path_to_files>/RUDI.xml conf/apim/repository/deployment/server/userstores
cp <path_to_files>/velocity_template.xml conf/apim/repository/resources/api_templates
```

*Remarque :* Les fichiers XML, toml et properties doivent être édités pour remplacer les propriétés par les valeurs attendues.

### 3. Démarrage de WSO2

Démarrer WSO2 à l'aide du fichier `docker-compose.yml` fourni :

```bash
docker-compose -f <path_to_docker_compose>/docker-compose.yml up
```
