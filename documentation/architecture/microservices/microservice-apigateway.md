# Microservice API Gateway

## Présentation générale

Le microservice API Gateway est utilisé pour l'exposition des routes d'accès aux jeux de données.

## Architecture technique

Le microservice suit l'architecture standard des microservices RUDI avec trois couches principales:

```mermaid
graph TD
    A[API/Facade Layer] --> B[Service Layer]
    B --> C[Storage/DAO Layer]
    C --> D[(Database)]
```
## Structure du code

Le microservice est organisé en plusieurs modules:

- **rudi-microservice-apigateway-facade**: Points d'entrée REST et contrôleurs
- **rudi-microservice-apigateway-service**: Logique métier et services
- **rudi-microservice-apigateway-storage**: Persistence des données et DAO
- **rudi-microservice-apigateway-core**: Modèles et objets partagés

## Diagramme de classes
![Diagramme de classes](../../../rudi-microservice/rudi-microservice-apigateway/readme/rudi-microservice-apigateway-storage-entities.png)

## Configuration

### Exemple de configuration

Un exemple de fichier de configuration est disponible [ici](../../../rudi-microservice/rudi-microservice-apigateway/rudi-microservice-apigateway-facade/src/main/resources/apigateway-exemple.properties).

