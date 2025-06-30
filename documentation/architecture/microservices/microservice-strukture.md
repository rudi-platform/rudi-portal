# Microservice Strukture

## Présentation générale

Le microservice Strukture est utilisé pour la gestion des producteurs et fournisseurs de données, des organisations utilisatrices de RUDI.

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

- **rudi-microservice-strukture-facade**: Points d'entrée REST et contrôleurs
- **rudi-microservice-strukture-service**: Logique métier et services
- **rudi-microservice-strukture-storage**: Persistence des données et DAO
- **rudi-microservice-strukture-core**: Modèles et objets partagés

## Diagramme de classes
![Diagramme de classes](../../../rudi-microservice/rudi-microservice-strukture/readme/rudi-microservice-strukture-storage-entities.png)

## Configuration

### Exemple de configuration

Un exemple de fichier de configuration est disponible [ici](../../../rudi-microservice/rudi-microservice-strukture/rudi-microservice-strukture-facade/src/main/resources/strukture-exemple.properties).

