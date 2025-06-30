# Microservice Registry

## Présentation générale

Le microservice Registry est utilisé afin de constituer un registre unique auprès duquel tous les autres microservices s'inscrivent.
Le microservice Gateway utilise ce registre afin de mettre en oeuvre le loadbalancing si plusieurs microservices d'un même type sont présents.

## Architecture technique

Contrairement aux autres microservices de RUDI, le microservice Registry ne dispose que d'une couche `Facade` :

```mermaid
graph TD
    A[API/Facade Layer]
```

## Structure du code

Le microservice est organisé en un module principal:

- **rudi-microservice-registry-facade**: Points d'entrée REST, contrôleurs et configuration du routage

Contrairement aux autres microservices RUDI, le Registry est principalement basé sur la configuration et ne nécessite pas de couches service et storage, car il n'a pas besoin de persister des données.

## Configuration

### Exemple de configuration

Un exemple de fichier de configuration est disponible [ici](../../../rudi-microservice/rudi-microservice-registry/rudi-microservice-registry-facade/src/main/resources/registry-exemple.properties).

