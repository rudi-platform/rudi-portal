# Facettes (composants partagés)

## Présentation générale

Les facettes sont des bibliothèques de code partagées entre les différents microservices de la plateforme RUDI. Elles permettent de mutualiser les fonctionnalités communes (par exemple pour l'accès aux API) et d'assurer une cohérence dans l'implémentation des différents services.

## Principes architecturaux

Les facettes suivent plusieurs principes clés :

- **Réutilisabilité** : Une facette peut être utilisée par plusieurs microservices
- **Responsabilité unique** : Chaque facette se concentre sur une préoccupation spécifique
- **Indépendance** : Les facettes ont un minimum de dépendances entre elles
- **Modularité** : Organisation en modules fonctionnels cohérents

## Organisation des facettes

Les facettes sont organisées en modules Maven, généralement structurés comme suit :

```
rudi-facet-{nom}
├── src/main/java
│   └── org/rudi/facet/{nom}
│       ├── bean          # Modèles de données
│       ├── helper        # Classes utilitaires
│       ├── service       # Interfaces de service
│       └── service/impl  # Implémentations de service
└── src/test
```

## Liste des facettes
| Facette | Description |
|---------|-------------|
| core | Éléments fondamentaux et utilitaires |
| [Dataverse](./facet-dataverse.md) (kaccess, kmedia, dataverse et dataset) | Intégration avec Dataverse, gestion des jeux de données et des medias |
| generator-text | Génération de documents text (txt, html, ...) à partir d'un template freemarker |
| generator-docx | Génération de documents docx |
| generator-pdf | Conversion des fichier docx en PDF |
| email | Service d'envoi d'emails |
| bpmn | Service de manipulation des workflows |
| crypto | Fonctions cryptographiques |
| doks | Stockage de documents sous forme de blob |
| bucket-s3 | Stockage de contenu dans un service de type S3 (minio) |
| cms | Accès à l'API de CMS (Magnolia) |
| oauth2 | Support OAuth2 |
| acl | Contrôle d'accès |
| kos | Accès au service de gestion des informations des thèmes SKOS https://skos.um.es/ |
| projekt | Accès aux services de gestion des réutilisations |
| rva | Service de recherche des adresses postales (RVA - Référentiel Voies Adresse de Rennes Métropole, OSM - Service Open Street Map, BAN - Service de l'état |
| selfdata | Accès aux services de gestion des données personnelles |
| apigateway | Accès aux services de la gateway d'accès aux différents contenus de jeux de données |
| strukture-common | Elément commun pour l'accès aux services de gestion des fournisseurs et des organisations |
| providers (strukture) | Accès aux services de gestion des fournisseurs de données |
| organization (strukture) | Accès aux services de gestion de gestion des organisations |


## Utilisation des facettes

| Microservice | Facettes utilisées |
|--------------|-------------------|
| ACL | 	generator-text, email |
| APIGateway | 	kaccess, acl, projekt, selfdata, crypto |
| Kalim | 	kaccess, organization, providers, kos, acl, apigateway |
| Konsent | 	organization, acl, generator-docx, crypto, generator-pdf, bucket-s3, projekt, strukture-common |
| Konsult | 	kaccess, rva, cms, kos, acl, projekt, selfdata, organization |
| Projekt | 	kmedia, bpmn,  strukture-common, organization, generator-text, email, kaccess |
| Selfdata | 	bpmn, kaccess, rva, doks, oauth2, providers, email, generator-text, organization, dataset, apigateway |
| Strukture | 	kmedia, bpmn, doks, acl, projekt, email, generator-text, kaccess |

D'autres facettes peuvent être utilisées par transitivité.

## Comment utiliser les facettes

Pour utiliser une facette dans un microservice, il faut ajouter la dépendance Maven correspondante :

```xml
<dependency>
    <groupId>org.rudi</groupId>
    <artifactId>rudi-facet-{nom}</artifactId>
    <version>${project.version}</version>
</dependency>
```

Activer la facette dans la configuration Spring Boot du microservice :

```java
@SpringBootApplication(scanBasePackages = { 
    "org.rudi.common.service", 
    "org.rudi.facet.{nom}",
    // Autres packages nécessaires
})
```

Il est également nécessaire de compléter la configuration fichier `.properties` du microservice avec la configuration de la facette utilisée.