# Guide de démarrage pour les développeurs

Ce guide est destiné aux nouveaux développeurs qui souhaitent configurer leur environnement de développement et commencer à contribuer au projet RUDI. Il couvre l'installation des prérequis, la configuration de l'IDE, la récupération du code, la compilation et l'exécution du projet en local.

## Prérequis techniques

### Logiciels nécessaires

Les logiciels suivants doivent être installés avant de démarrer :

- **JDK 17** - Environment Java Development Kit
- **Maven 3.9+** - Gestionnaire de dépendances et outil de build
- **Git** - Gestionnaire de versions
- **PostgreSQL** - Base de données relationnelle pour les microservices
- **Node.js 20+ et npm 10+** - Pour les développements FrontEnd
- **Docker** - Pour l'installation de Dataverse et Magnolia

### IDE recommandés

RUDI peut être développé avec n'importe quel IDE Java moderne, mais nous recommandons en particulier :

- **IntelliJ IDEA** (Ultimate de préférence pour le support complet de Spring Boot et Angular)
- **Eclipse** 

### Composants externes

Pour le bon fonctionnement de RUDI, les 2 composants suivants doivent être installés selon les documentations indiquées.

- **Dataverse** - Utilisé pour le catalogue de jeux de données - [Documentation d'installation et de configuration](./demarrage-dataverse.md)
- **Magnolia** - CMS Headless utilisé pour l'administration de contenus - [Documentation d'installation et de configuration](./demarrage-magnolia.md)

## Mise en place de l'environnement de développement

### Configuration de Git

```bash
git config --global user.name "Votre Nom"
git config --global user.email "votre.email@exemple.com"
```

### Récupération du code source

```bash
# Cloner le dépôt principal
git clone https://github.com/rudi-platform/rudi-portal.git
cd rudi-portal
```

## Construction du projet

Le projet RUDI est basé sur Maven et structuré en modules. Voici les étapes pour compiler la totalité du projet (y compris le frontend) :

```bash
# À la racine du projet
mvn clean install
```

Si vous souhaitez omettre les tests, ajoutez le flag `-DskipTests`. 

## Configuration de l'IDE

### IntelliJ IDEA

1. Ouvrir IntelliJ IDEA
2. Sélectionner "Open" (ou "File" > "Open")
3. Naviguer vers le répertoire du projet cloné et l'ouvrir
4. IntelliJ détectera automatiquement le projet Maven
5. Configuration supplémentaire :
   - Installer le plugin Lombok si ce n'est pas déjà fait : Settings > Plugins > "Lombok"
   - Activer le traitement des annotations Lombok : Settings > Build, Execution, Deployment > Compiler > Annotation Processors > "Enable annotation processing"
   - Configurer l'utilisation de JDK 17 : File > Project Structure > Project Settings > Project > SDK
   - Recommandé : installer un plugin Angular/TypeScript (pour le développement frontend)

### Eclipse

1. Ouvrir Eclipse
2. Sélectionner "File" > "Import" > "Maven" > "Existing Maven Projects"
3. Sélectionner le répertoire racine du projet
4. Eclipse importera tous les modules Maven du projet
5. Configuration supplémentaire 
   - Installer les plugins "Help" > "Eclipse Marketplace" puis rechercher et installer les plugins
      - Maven Integration
      - Lombok
      - Eclipse BPMN2 Modeler 1.5.0
      - Recommandé : installer un plugin Angular/TypeScript (pour le développement frontend)
   - Vérifier que le JDK 17 est configuré : Window > Preferences > Java > Installed JREs

## Exécution locale - partie Backend

### Configuration de la base de données locale

Pour exécuter les services, vous aurez besoin d'une base de données PostgreSQL :

1. Créer un utilisateur et une base de données pour RUDI :
   ```sql
   CREATE USER rudi WITH PASSWORD 'rudi';
   CREATE DATABASE rudi OWNER rudi;
   GRANT ALL PRIVILEGES ON DATABASE rudi TO rudi;
   ```

1. Créer les schémas et utilisateurs pour chaque microservice :

   ```sql
   -- Créer l'utilisateur pour le microservice
   CREATE USER <ms_name> WITH
   LOGIN
   NOSUPERUSER
   INHERIT
   NOCREATEDB
   NOCREATEROLE
   NOREPLICATION
   PASSWORD '<ms_name>';

   -- Créer le schéma pour le microservice
   CREATE SCHEMA <ms_name>_data AUTHORIZATION rudi;

   -- Gérer les droits d'accès
   GRANT ALL ON SCHEMA <ms_name>_data TO <ms_name>;
   ALTER USER <ms_name> SET search_path TO <ms_name>_data, public;

   GRANT ALL PRIVILEGES ON SCHEMA <ms_name>_data TO rudi;
   ```

   où _<ms_name>_ vaut `acl`, `apigateway`, `kalim`, `konsent`, `kos`, `projekt`, `selfdata`, `strukture`.

1. Configurer les propriétés de connexion dans les fichiers de configuration de chaque microservice.
   Les fichiers de configuration sont situés dans le répertoire `rudi-microservice/rudi-microservice-<ms-name>/rudi-microservice-<ms-name>-facade/src/main/resources` de chaque microservice.

   RUDI utilise le système de [datasource de SpringBoot](https://docs.spring.io/spring-boot/how-to/data-access.html).

   Pensez à adapter les valeurs de `spring.datasource.password` avec les valeurs que vous avez choisi pour vos utilisateurs.

### Configurer et exécuter les microservices

1. Créer un nouveau [profil Spring Boot pour la configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.files.profile-specific) de chaque microservice.

   - Créer un fichier `<nom-du-microservice>-<mon-profil>.properties` au même endroit que le fichier `<nom-du-microservice>.properties` de la façade du microservice.

   - Pour vous y aider, un fichier `<nom-du-microservice>-exemple.properties` est présent.

   > Exemple pour le microservice ACL : 
   > 
   > ```bash
   > cd rudi-portal
   > cd rudi-microservice/rudi-microservice-acl/rudi-microservice-acl-facade/src/main/resources
   > cp acl-exemple.properties acl-monprofil.properties
   > # adapter les valeurs dans le fichier acl-monprofil.properties
   > ```


   Faire de même pour tous les microservices (acl, apigateway, gateway, kalim, konsent, konsult, kos, projekt, registry, selfdata, strukture).

   > **Note** : Pour les microservices nécessitant les accès à  Dataverse et Magnolia, il est nécessaire de terminer la configuration et le démarrage de ces composants pour compléter la configuration des microservices.
   > - [Dataverse](./demarrage-dataverse.md)
   > - [Magnolia](./demarrage-magnolia.md)

1. Exécuter chaque microservice via la classe principale `AppFacadeApplication` avec le profil souhaité.

   > Exemple pour ACL :
   > 
   > ```java -Dspring.profiles.active=monprofil org.rudi.microservice.acl.facade.AppFacadeApplication```

1. Chaque microservice sera accessible via son port configuré dans la propriété `server.port`

## Exécution locale - partie Frontend

Les informations sur le fonctionnement local et le démarrage du frontend sont détaillées [ici](./demarrage-frontend.md).

En synthèse, pour lancer le serveur de développement :
```bash
npm start
```

L'application sera accessible à l'adresse `http://localhost:4200/`.

## (Alternative) Construction et utilisation des images Docker

1. Construisez le projet :
```bash
mvn package -Pdev-docker,prod -Dmaven.javadoc.skip=true -DskipTests
```
_Remarque : A l'issue de cette opération, les jar des microservices sont copiés dans les répertoires de construction des images (/ci/docker)_

1. Lancez le script d'installation :
```bash
cd ci/docker-compose
sudo buildDockerImage.sh
```
_Remarque : A l'issue de cette execution les images des différents microservice Rudi sont construites._

1. Configurez votre installation en modifiant les fichiers dans `ci/docker-compose/config/`

1. Démarrez les services avec Docker Compose

> **Note** : Chaque microservice utilise son propre schéma de base de données, créé automatiquement au démarrage. Au démarrage les scripts de création des tables sont joués de manière automatique par chaque microservice. En revanche, il est nécessaire de créer role et schema de chaque microservice dans la base de données comme indiqué [plus haut](#configuration-de-la-base-de-données-locale).

## Composants externes

**[Dataverse](../architecture/dataverse/0-generalites.md)** et **[Magnolia](../architecture/magnolia/0-generalites.md)** sont 2 composants qui peuvent être installés et déployés indépendamment de RUDI.

Ils doivent cependant être configurés de manière spécifique pour RUDI.

- [Installation et configuration de Dataverse](demarrage-dataverse.md)
- [Installation et configuration de Magnolia](demarrage-magnolia.md)

## Ressources supplémentaires

- [Documentation officielle de Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Documentation officielle d'Angular](https://angular.io/docs)
- [Wiki du projet](https://rudi.fr/yeswiki)
