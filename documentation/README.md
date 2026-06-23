# Documentation Technique RUDI

## À propos de cette documentation

Cette documentation technique s'adresse aux **développeurs** qui souhaitent comprendre, maintenir ou contribuer au projet RUDI (Rennes Urban Data Interface). Elle couvre les aspects techniques de la plateforme, son architecture et ses composants.

Pour démarrer rapidement, une **instance prête à démarrer** de RUDI est disponible : [RUDI Out of the Box 🎁](https://github.com/rudi-platform/rudi-out-of-the-box).

## Sommaire

1. Architecture
   - [Généralités](./architecture/0-generalites.md)
   - [Microservices](./architecture/microservices/0-generalites.md)
   - [Facettes](./architecture/facettes/0-generalites.md)
   - [Frontend](./architecture/frontend/0-generalites.md)
   - [Dataverse](./architecture/dataverse/0-generalites.md)
   - [Magnolia](./architecture/magnolia/0-generalites.md)

1. Guide de mise en place de l'environnement de développement

   - [Installation, configuration et démarrage de l'environnement](./demarrage/demarrage-developpeur.md) - Installation des prérequis, la configuration de l'IDE, la récupération du code, la compilation et l'exécution du projet en local.
   - [Commandes frontend](./demarrage/demarrage-frontend.md) - Commandes utiles de génération de code et le démarrage du frontend Angular.
   - [Installation et configuration de Dataverse](./demarrage/demarrage-dataverse.md)
   - [Installation et configuration de Magnolia](./demarrage/demarrage-magnolia.md)
   

1. Procédures spécifiques

   - [Comment mettre à jour les diagrammes de classe ?](./cookbook/maj-diagrammes-classe.md)
   - [Commandes utiles pour l'administration de RUDI](https://blog.rudi.bzh/yeswiki/?DocumentationSurLAdministrationDuPortailR)
   - [Comment modifier le mot de passe d'un utilisateur RUDI en BDD ?](./cookbook/modifier-mot-de-passe-base.md)
   - [Comment modifier les mots de passe des utilisateurs postgres de Rudi ?](./cookbook/modifier-mot-de-passe-postgres.md)
   - [Comment modifier les mots de passe et token de Dataverse ?](./cookbook/modifier-mot-de-passe-dataverse.md)
   - [Comment modifier les mots de passe et token de Magnolia ?](./cookbook/modifier-mot-de-passe-magnolia.md)
   - [(Hors production) Comment modifier le mot de passe de mailhog ?](./cookbook/modifier-mot-de-passe-mailhog.md)
   - [Configuration du service d'authentification externe](./cookbook/configuration-services-authentification.md)

## Conventions de documentation

Cette documentation utilise les conventions suivantes :

- Les **noms de fichiers et chemins** sont en `police à chasse fixe`
- Les ***termes importants*** sont en italique et en gras
- Les morceaux de code sont dans des blocs dédiés avec coloration syntaxique
- Les diagrammes sont créés avec la syntaxe Mermaid
