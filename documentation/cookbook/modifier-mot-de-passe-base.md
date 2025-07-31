# Comment modifier le mot de passe d'un utilisateur ?

## Obtenir la valeur chiffré d'un mot de passe

Les mots de passe des utilisateurs sont stockés chiffrés en base de données. Si vous souhaitez obtenir directement la valeur chiffrée d'un mot de passe pour la positionner dans la base de données, utilisez le test ``org.rudi.microservice.acl.service.helper.PasswordHelperUT.bcryptPasswordEncoder2``

Le mot de passe chiffré peut être remplacé dans la colonne ``acl_data.user_.password`` pour le user souhaité.

```sql
UPDATE acl_data.user_ SET password='<valeur chiffrée>' where login = '<login du user concerné>';
```

## Modifier le mot de passe d'un utilisateur de type MICROSERVICE

En cas de mise à jour du mot de passe d'un microservice en base (cf ci-dessus), le fichier de configuration du microservice doit également être mis à jour.

```properties
module.oauth2.client-secret=<nouveau mot de passe en clair>
```
