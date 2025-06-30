# Microservice Gateway

## Présentation générale

Le microservice Gateway permet la mise en œuvre du load-balancing entre les microservices RUDI. Il agit comme point d'entrée unique pour toutes les requêtes vers l'écosystème RUDI et les route vers les microservices appropriés.

- Routage des requêtes vers les microservices appropriés
- Load-balancing du trafic entre les instances de microservices
- Filtrage et validation des requêtes entrantes
- Gestion des règles de sécurité et d'accès
- Monitoring et logging des requêtes

## Architecture technique

Contrairement aux autres microservices de RUDI, le microservice Gateway ne dispose que d'une couche `Facade` :

```mermaid
graph TD
    A[API/Facade Layer]
```

## Structure du code

Le microservice est organisé en un module principal:

- **rudi-microservice-gateway-facade**: Points d'entrée REST, contrôleurs et configuration du routage

Contrairement aux autres microservices RUDI, le Gateway est principalement basé sur la configuration et ne nécessite pas de couches service et storage, car il n'a pas besoin de persister des données.

## Configuration

### Exemple de configuration

Un exemple de fichier de configuration est disponible [ici](../../../rudi-microservice/rudi-microservice-gateway/rudi-microservice-gateway-facade/src/main/resources/gateway-exemple.properties).
