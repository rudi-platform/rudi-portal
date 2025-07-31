# Comment modifier les mots de passe des utilisateurs postgres de RUDI ?

## Modification du mot de passe de l'utilisateur principal de BDD

* Se connecter à la base de données et utiliser la commande

```sql
ALTER USER rudi WITH PASSWORD 'nouveau mot de passe';
```

* Renseigner le mot de passe dans la variable POSTGRES_PASSWORD du service ``database`` dans docker-compose


## Modification du mot de passe de connexion à la BDD pour un microservice

* Se connecter à la base de données et utiliser la commande

```sql
ALTER USER <user> WITH PASSWORD 'nouveau mot de passe';
```

* Renseigner le mot de passe dans la propriété ``spring.datasource.password`` du microservice concerné.