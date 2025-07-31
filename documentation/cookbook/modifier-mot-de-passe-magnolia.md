# Comment modifier les mots de passe et token de Magnolia ?

## Comment modifier le mot de passe de la BDD ?

* Se connecter à la base de données et utiliser la commande

```sql
ALTER USER magnolia WITH PASSWORD 'nouveau mot de passe';
```

* Renseigner le mot de passe dans la variable ``POSTGRES_PASSWORD`` du service ``magnolia-database`` dans docker-compose
* Renseigner le mot de passe dans la variable ``MAGNOLIA_BDD_PASSWORD`` du service ``magnolia`` dans docker-compose
* Redémarrer les 2 services

## Comment modifier le mot de passe d'un utilisateur de Magnolia ?

* Se connnecter à l'IHM d'administration de Magnolia avec le compte d'un administrateur (par exemple ``superuser``)
* Se rendre dans la section ``Sécurité`` 
* Identifier l'utilisateur concerné (soit dans l'onglet ``Utilisateurs``, soit dans l'onglet ``Utilisateurs du système``)
* Sélectionner l'utilisateur, cliquer sur ``Modifier l'utilisateur``
* Renseigner le nouveau mot de passe, puis sauvegarder les changements
* Publier les changements

## Comment modifier les clés de publication de Magnolia ?

[Voir la documentation de Magnolia pour plus de documentation sur les clés de publication de Magnolia](https://docs.magnolia-cms.com/product-docs/6.3/administration/security/activation-security/activation-keys/#_when_and_how_to_create_new_keys)
