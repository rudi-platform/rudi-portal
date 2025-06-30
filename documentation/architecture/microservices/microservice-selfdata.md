# Microservice Selfdata

## Présentation générale

Le microservice Selfdata est utilisé pour la gestion des données personnelles.

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

- **rudi-microservice-selfdata-facade**: Points d'entrée REST et contrôleurs
- **rudi-microservice-selfdata-service**: Logique métier et services
- **rudi-microservice-selfdata-storage**: Persistence des données et DAO
- **rudi-microservice-selfdata-core**: Modèles et objets partagés

## Diagramme de classes
![Diagramme de classes](../../../rudi-microservice/rudi-microservice-selfdata/readme/rudi-microservice-selfdata-storage-entities.png)

## Configuration

### Exemple de configuration

Un exemple de fichier de configuration est disponible [ici](../../../rudi-microservice/rudi-microservice-selfdata/rudi-microservice-selfdata-facade/src/main/resources/selfdata-exemple.properties).

