# Commandes frontend

Les informations ci-dessous sont des informations complémentaires au [Guide de démarrage du développeur](demarrage-developpeur.md).

## Processus de développement

Pour démarrer le développement sur ce projet front-office Angular, il est nécessaire de générer les services et modèles et de construire l'application.

Ces étapes sont effectuées lorsque l'application complète est buildée (backend+frontend) par la commande 

```bash 
cd rudi-portal
mvn clean install -DskipTests
```
 
   > Concernant le frontend, cette commande déclenche les étapes 
   > 1. ``npm run generate:all`` pour **générer les clients API** à partir des spécifications OpenAPI
   > 1. ``npm ci`` pour **installer les dépendances**
 
## Démarrer le serveur de développement

Pour un environnement de développement standard, utilisez 

```bash
npm start
``` 

L'application sera accessible à l'adresse `http://localhost:4200/`.

Remplacer par ``npm run start-https`` pour un environnement sécurisé.

## Commandes Angular

Si vous souhaitez utiliser les commandes en dehors de maven, voici une documentation des commandes utiles :

| Commande | Description |
|-|-|
| npm start | Démarre un serveur de développement avec configuration de proxy HTTP |
| npm run start-https | Démarre un serveur de développement avec configuration de proxy HTTPS |
| npm run start-prod-https | Démarre un serveur avec la configuration de production et proxy HTTPS |
| npm run build-prod | Construit l'application en mode production |
| npm run build-dev | Construit l'application en mode développement |

[Voir plus d'informations sur le fonctionnement du proxy.](../architecture/frontend/proxy.md)

### Commandes de génération OpenAPI

#### Commandes de génération globales

| Commande | Description |
|-|-|
| npm run generate:all | Génère tous les clients API et modèles (facets et microservices) |
| npm run generate:all-facets | Génère tous les clients API des facets |
| npm run generate:all-microservices | Génère tous les clients API des microservices |

####  Génération de clients API pour les facets

| Commande | Description |
|-|-|
| npm run generate:bpmn | Génère le client API pour la facet BPMN |
| npm run generate:kaccess | Génère le client API pour la facet KAccess |
| npm run generate:kmedia | Génère le client API pour la facet KMedia |
| npm run generate:rva | Génère le client API pour la facet RVA |
| npm run generate:cms | Génère le client API pour la facet CMS |

#### Génération de clients API pour les microservices

| Commande | Description |
|-|-|
| npm run generate:strukture | Génère les clients API pour le microservice Strukture (modèle et API) |
| npm run generate:acl | Génère les clients API pour le microservice ACL (modèle et API) |
| npm run generate:konsult | Génère les clients API pour le microservice Konsult (modèle et API) |
| npm run generate:kos | Génère les clients API pour le microservice KOS (modèle et API) |
| npm run generate:projekt | Génère les clients API pour le microservice Projekt (modèle et API) |
| npm run generate:selfdata | Génère les clients API pour le microservice Selfdata (modèle et API) |
| npm run generate:konsent | Génère les clients API pour le microservice Konsent (modèle et API) |

