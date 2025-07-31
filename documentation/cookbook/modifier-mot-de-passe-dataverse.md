# Comment modifier les mots de passe et token de Dataverse ?

## Comment modifier le mot de passe de la BDD ?

* Se connecter à la base de données et utiliser la commande

```sql
ALTER USER dataverse WITH PASSWORD 'nouveau mot de passe';
```

* Renseigner le mot de passe dans la variable ``POSTGRES_PASSWORD`` du service ``dataverse-database`` dans docker-compose
* Renseigner le mot de passe dans la variable ``DATAVERSE_DB_PASSWORD`` du service ``dataverse`` dans docker-compose
* Redémarrer dataverse

## Comment modifier ou renouveller le token de connexion à Dataverse ?

* Renouveller le token côté Dataverse :
    * Se connecter sur l'IHM d'administration dataverse
    * Se rendre sur le menu ``Dataverse Admin`` puis ``API Token``
    * Cliquer sur Recreate Token
    * Copier le nouveau token
* Mettre à jour la configuration côté Rudi
    * Remplacer la valeur de la propriété ``dataverse.api.token`` partout ou elle est présente
    * Redémarrer les services concernés

## Comment modifier le mot de passe d'un utilisateur de Dataverse ?

* Se connecter sur l'IHM de dataverse et se connecter avec le compte de l'utilisateur
* Se rendre sur le menu ``Dataverse Admin`` puis ``Account Information``
* Cliquer sur ``Edit Account`` puis ``Password``
* Modifier le mot de passe comme indiqué dans la page


