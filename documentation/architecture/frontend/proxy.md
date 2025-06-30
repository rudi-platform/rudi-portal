# Utilisation du proxy 

## Description
Les fichiers suivants définissent les règles de proxy pour le serveur de développement Angular. 
Ils permettent de rediriger les requêtes HTTP émises depuis l'application front-end vers les différents backends pendant le développement local.

- ``rudi-portal/rudi-application/rudi-application-front-office/angular-project/proxy.conf.json``  pour les requêtes en HTTP.
- ``rudi-application/rudi-application-front-office/angular-project/proxys.conf.json`` pour les requêtes en HTTPS.

## Rôle dans le projet
Dans le cadre du projet RUDI (application front-office), ce fichier est essentiel pour le développement local car il :
- Contourne les problèmes de CORS (Cross-Origin Resource Sharing)
- Permet d'accéder aux API backend sans avoir à configurer CORS sur les serveurs de développement
- Facilite le développement en redirigeant les requêtes vers les bonnes destinations (microservices appropriés)

## Utilisation
Pour utiliser ce proxy lors du développement local :
1. Assurez-vous que le fichier est bien configuré avec les bons chemins et destinations
2. Lancez Angular avec la commande `npm start` qui prend en compte ce fichier.

## Configuration
Le fichier contient généralement des entrées du type :
```json
{
    "/api": {
        "target": "http://localhost:8080",
        "secure": false,
        "changeOrigin": true
    }
}
```

Chaque entrée définit :
- Un chemin à intercepter (ex: `/api`)
- L'URL cible vers laquelle rediriger les requêtes
- Des options de configuration supplémentaires