# Interface utilisateur (Frontend Angular)

## Présentation générale

Le frontend de la plateforme RUDI est une application Angular qui constitue l'interface utilisateur du portail grand public. Cette interface permet aux utilisateurs de découvrir, rechercher et accéder aux jeux de données disponibles dans le catalogue.


Le frontend RUDI offre les fonctionnalités suivantes :

- **Page d'accueil** personnalisable et moderne
- **Moteur de recherche** avancé pour les jeux de données
- **Visualisation des métadonnées** des jeux de données
- **Téléchargement** des données ouvertes
- **Demande d'accès** pour les données restreintes
- **Catalogue de réalisations** basées sur les données
- **Authentification** et gestion du profil utilisateur
- **Espace personnel** pour suivre ses demandes et projets

## Architecture technique

L'application est développée avec :

- **Angular 17** : Framework frontend
- **TypeScript** : Langage de programmation
- **SCSS** : Préprocesseur CSS
- **NgRx** : Gestion d'état (pattern Redux)
- **RxJS** : Programmation réactive

## Structure de l'application

L'application suit une architecture modulaire organisée comme suit :

```
angular-project/
├── src/
│   ├── app/
│   │   ├── core/           # Services et composants fondamentaux
│   │   ├── features/       # Modules fonctionnels
│   │   │   ├── cms/        # Gestion de contenu
│   │   │   ├── data-set/   # Consultation des jeux de données
│   │   │   ├── home/       # Page d'accueil
│   │   │   ├── login/      # Authentification
│   │   │   ├── organization/ # Organisations
│   │   │   ├── personal-space/ # Espace personnel
│   │   │   └── project/    # Projets de réutilisation
│   │   ├── shared/         # Composants et services partagés
│   │   └── styles/         # Styles globaux
│   ├── assets/             # Images, icônes, etc.
│   ├── environments/       # Configuration des environnements
│   └── theme/              # Thème de l'application
└── ...
```

## Communication avec le backend

Le frontend communique avec les microservices backend à travers des services Angular qui encapsulent les appels API REST.

Les services et DTO d'accès aux microservices sont générés à partir des API des microservices dans ``rudi-portal/rudi-application/rudi-application-front-office/angular-project/micro_service_modules/<nom du microservice>`` à l'aide de [openapi-generator](https://openapi-generator.tech/). Les [commandes de construction du front-end](../../demarrage/demarrage-frontend.md) disponibles dans [package.json](../../../rudi-application/rudi-application-front-office/angular-project/package.json) sont détaillées dans le guide de démarrage.

Selon l'environnement utilisé, le frontend offre un [proxy d'accès aux différents microservices](proxy.md).

## Responsive Design

L'interface utilisateur est entièrement responsive, s'adaptant à différentes tailles d'écran :

- **Desktop** : Expérience complète
- **Tablette** : Adaptation de la mise en page
- **Mobile** : Version optimisée pour petits écrans
