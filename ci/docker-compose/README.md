# Installation

## Généralités

Le portail rudi peut-être installée de différentes manières mais étant donné le nombre élevé de microservices, une installation de type container est à privilégier (k8s ou docker-compose par exemple).

L'installation est constituée de 3 étapes successives :

  - Installation des pre-requis,
  - Installation des *microservices RUDI*,
  - Installation du catalogue des jeux de données *Dataverse*
  
### Prérequis

Installation prévue sur linux debian :

- Installer Maven
- Installer jdk11
- Installer git
- Installer xmlstarlet

### Installation microservices RUDI

  - Cloner le projet *https://github.com/rudi-platform/rudi-portal*
  - Construire le projet à l'aide de la commande suivante :

```
mvn package -Pdev-docker,prod -Dmaven.javadoc.skip=true -DskipTests
```

*Remarque :*
_A l'issue de cette opération, les jar des microservices sont copiés dans les répertoires de construction des images (<root-rudi>/ci/docker)_

  - Lancer le script d'installation 
  
```
cd <root-rudi>/ci/docker-compose
sudo buildDockerImage.sh
```

*Remarque :*
_A l'issue de cette execution les images des différents µService Rudi sont construites._

  - Editer les fichiers de propriétés présents dans *<root-rudi>/ci/docker-compose/config/* et mettre à jour les propriétés en conséquence
 
  - Démarrer les services RUDI à l'aide du docker-compose.yml fourni
   
*Remarque :*
_Chaque µService utilise son propre schéma de données._<br>
_Au démarrage les scripts de création des tables sont joués de manière automatique par chaque µService._<br>
_En revanche, il est nécessaire de créer role et schema de chaque microservice dans la base de données_
_Les schémas attendus sont acl_data, kalim_data, konsent_data, kos_data, projekt_data, selfdata_data, strukture_data, template_data_

### Installation Dataverse

L'installation de Dataverse s'appuie sur *https://github.com/IQSS/dataverse-docker* dans la version v5.5.

  - Se positionner dans le répertoire <root-rudi>/ci/docker-compose/dataverse
  - Cloner le projet et se positionner sur la branche correspondante

A l'issue de cette primo-installation, il nécessaire de copier les fichiers suivants afin de modifier la définition des méta-données :

```
  - <root-rudi>/ci/docker-compose/solr-data/collection1/conf/schema_dv_mdb_copies.xml dans le volume <solr_data>/schema_dv_mdb_copies.xml
  - <root-rudi>/ci/docker-compose/solr-data/collection1/conf/schema_dv_mdb_fields.xml dans le volume <solr_data>/schema_dv_mdb_fields.xml
  - <root-rudi>/ci/docker-compose/solr-data/collection1/conf/schema.xml dans le volume <solr_data>/schema.xml
  - <root-rudi>/ci/docker-compose/docker-compose.yml dans dataverse
```

  - Exécuter le script dataverse suivant afin de prendre en compte ces nouvelles définitions :

```
./updateSchemaMDB.sh -d http://localhost:{{dataverse_port}} -t {{solr_data_conf_directory}}
```
