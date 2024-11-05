<br>
<p align="center">
  <a href="https://rudi.rennesmetropole.fr/">
  <img src="https://blog.rudi.bzh/wp-content/uploads/2020/11/logo_bleu_orange.svg" width=100px alt="Rudi logo" />  </a>
</p>

<h2 align="center" >Le Portail</h3>
<p align="center">La partie grand public de la plateforme Rudi.</p>

<p align="center"><a href="https://rudi.rennesmetropole.fr/">🌐 Instance de Rennes Métropole</a> · <a href="doc.rudi.bzh">📚 Documentation</a> ·  <a href="https://blog.rudi.bzh/">📰 Blog</a><p>

Ce dépôt de code de Rudi permet de lancer la partie "grand public" de la plateforme est :

### ✨ Une vitrine pour les données de votre territoire
Une page d'accueil configurable pour avoir les couleurs de votre territoire.
<br>
![screely-1730817183457](https://github.com/user-attachments/assets/f8480d07-25ae-4f59-83cb-bdbc4be99a0e)

### 🔍 Un moteur de recherche pour les jeux de données ouvertes et à accès restreint
Accéder à une vaste collection de jeux de données.
<br>
![screely-1730816747258](https://github.com/user-attachments/assets/7c719f6d-39fc-45de-8a6f-eedbb8105c28)

### 💡 Un catalogue de réalisations pour inspirer le partage de données
Partager et examiner comment les données sont réutilisées.
<br>
![screely-1730817335737](https://github.com/user-attachments/assets/03c92e6b-052e-4088-8f53-1dc8b592f6d2)


## Installation

L'installation se fait en trois étapes principales. Pour une meilleure performance, nous recommandons l'utilisation de conteneurs (Docker ou Kubernetes).

### 1️⃣ Prérequis (sur Debian Linux)

```bash
# Installation des outils nécessaires
# - Maven pour la gestion des dépendances
# - JDK 11 pour l'exécution
# - Git pour le code source
# - XMLStarlet pour le traitement XML
```
### 2️⃣ Installation des microservices RUDI

1. Clonez le dépôt :
```bash
git clone https://github.com/rudi-platform/rudi-portal
```

2. Construisez le projet :
```bash
mvn package -Pdev-docker,prod -Dmaven.javadoc.skip=true -DskipTests
```
_Remarque : A l'issue de cette opération, les jar des microservices sont copiés dans les répertoires de construction des images (/ci/docker)_

3. Lancez le script d'installation :
```bash
cd ci/docker-compose
sudo buildDockerImage.sh
```
_Remarque : A l'issue de cette execution les images des différents µService Rudi sont construites._

4. Configurez votre installation en modifiant les fichiers dans `ci/docker-compose/config/`

5. Démarrez les services avec Docker Compose

> **Note** : Chaque microservice utilise son propre schéma de base de données, créé automatiquement au démarrage. Au démarrage les scripts de création des tables sont joués de manière automatique par chaque µService. En revanche, il est nécessaire de créer role et schema de chaque microservice dans la base de données Les schémas attendus sont acl_data, kalim_data, konsent_data, kos_data, projekt_data, selfdata_data, strukture_data, template_data

### 3️⃣ Installation de Dataverse

RUDI utilise Dataverse v5.5 pour la gestion des données. Pour l'installer :

1. Naviguez vers le répertoire Dataverse :
```bash
cd ci/docker-compose/dataverse
```

2. A l'issue de cette primo-installation, il nécessaire de copier les fichiers suivants afin de modifier la définition des méta-données :

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

## Documentation technique

Les diagrammes 📊 de classe sont générés automatiquement par les plugins maven
* * plantuml-generator-maven-plugin * pour la génération des fichiers lisibles par plantuml (fichiers .puml)
* * plantuml-maven-plugin * pour la génération des images correspondant aux fichiers puml

Pour mettre à jour les diagrammes de classe :
```bash
mvn clean install -DPLANTUML_LIMIT_SIZE=8192 -DskipTests -DgenerateUml
```

Commiter les fichiers modifiés par cette commande ( *.puml, *-storage-entities.png)


## L'écosystème Rudi (les autres dépôts de code)

Le portail Rudi n'est qu'une partie de l'écosystème de la plateforme Rudi. Pour l'utiliser plainement, réferez vous aux autres dépôts de code de l'organisation:

### [RUDI Out of the Box 🎁](https://github.com/rudi-platform/rudi-out-of-the-box)
Lancez une instance RUDI en seulement 4 commandes !

### Nœud Producteur RUDI 

Un ensemble d'outils pour les producteurs de données comprenant :

#### [Node Manager 👀](https://github.com/rudi-platform/rudi-node-manager)
Gérez les accès et les interactions avec vos données.

#### [Node Storage 💽](https://github.com/rudi-platform/rudi-node-storage)
Stockez et organisez vos données en toute sécurité.

#### [Node Catalog 🗂️](https://github.com/rudi-platform/rudi-node-catalog)
Décrivez et indexez vos jeux de données pour les rendre facilement trouvables.




