Configuration des Services d'Authentification OAuth2
====================================================

Ce document décrit comment déclarer et configurer des services d'authentification complémentaires dans le système RUDI.

Table des matières
------------------

1.  [Structure du fichier de configuration](#1-structure-du-fichier-de-configuration)
2.  [Configuration pour LemonLDAP::NG](#2-configuration-pour-lemonldapng)
3.  [Configuration pour FranceConnect et FranceConnect+](#3-configuration-pour-franceconnect-et-franceconnect)
4.  [Configuration pour GitHub](#4-configuration-pour-github)

* * *

1\. Structure du fichier de configuration
-----------------------------------------

Le fichier de configuration des authentificateurs OAuth2 est au format JSON. Une implémentation par défaut embarqué dans l'application se trouve dans :

```
rudi-microservice/rudi-microservice-acl/rudi-microservice-acl-facade/src/main/resources/authenticators/oauth2-authenticator.json

```

Le fichier de configuration peut-être surchargé en utilisant la propriété suivante dans le fichier acl.properties :

```
rudi.oauth2.authenticator.configuration=authenticators/oauth2-authenticator.json

```

Le fichier est recherché en priorité dans le répertoire dans lequel se trouve `acl.properties` puis dans le jar.

### Format général

Le fichier contient un tableau JSON d'objets représentant chaque service d'authentification :

```
[
  {
    "name": "nom-unique",
    "label": "Libellé affiché",
    "description": "Description du service",
    "viewSettings": { ... },
    "serverUrl": "https://url-du-serveur",
    "redirectUrl": "https://url-de-redirection",
    "clientId": "identifiant-client",
    "clientSecret": "secret-client",
    "authorizationGrantType": "type-de-grant",
    "clientAuthenticationMethod": "méthode-authentification",
    "scope": "scopes-demandés",
    "requireProofKey": true/false,
    "provider": { ... }
  }
]

```

### Description des attributs principaux

|Attribut	|Type	|Obligatoire	|Description	|
|:----------|----------|----------|----------|
|`name`|String|✅|Identifiant unique du service d'authentification (utilisé en interne)|
|`label`|String|✅|Libellé affiché à l'utilisateur pour ce mode d'authentification|
|`description`|String|✅|Description détaillée du service|
|`serverUrl`|String|✅|URL de base du serveur d'authentification OAuth2|
|`redirectUrl`|String|✅|URL de redirection après authentification (callback)|
|`clientId`|String|✅|Identifiant client fourni par le fournisseur OAuth2|
|`clientSecret`|String|✅|Secret client fourni par le fournisseur OAuth2|
|`authorizationGrantType`|String|✅|Type de grant OAuth2 : `authorization_code`, `client_credentials`, etc.|
|`clientAuthenticationMethod`|String||Méthode d'authentification du client : `client_secret_post`, `client_secret_basic`|
|`scope`|String|✅|Scopes OAuth2 demandés, séparés par des virgules|
|`requireProofKey`|Boolean||Active PKCE (Proof Key for Code Exchange) pour plus de sécurité|
|`viewSettings`|Object||Paramètres d'affichage de l'authentificateur dans l'interface|
|`provider`|Object|✅|Configuration détaillée du fournisseur OAuth2|

### Objet `viewSettings`

Cet objet contrôle l'apparence de l'authentificateur dans l'interface utilisateur :

```
"viewSettings": {
  "isolated": true,
  "cssClass": "nom-classe-css",
  "iconUrl": "classpath:/authenticators/icone.svg",
  "hoverIconUrl": "classpath:/authenticators/icone-hover.svg",
  "links": [
    {
      "url": "https://exemple.com",
      "label": "Texte du lien"
    }
  ]
}

```

|Attribut	|Type	|Description|
|:----------|:----------|:----------|
|`isolated`|Boolean|Si `true`, affiche l'authentificateur séparément des autres|
|`cssClass`|String|Classe CSS personnalisée pour le style|
|`iconUrl`|String|Chemin vers l'icône (utiliser `classpath:` pour les ressources internes)|
|`hoverIconUrl`|String|Icône affichée au survol|
|`links`|Array|Liens informatifs affichés sous le bouton d'authentification|

Remarques:

- pour les attributs `cssClass`, `iconUrl` et `hoverIconUrl` des valeurs sont disponibles pour certains fournisseurs d'identité (LemonLDAP::NG, Github, FranceConnect, FranceConnect+).

### Objet `provider`

Cet objet définit les endpoints et la configuration spécifique du fournisseur OAuth2 :

```
"provider": {
  "authorizationUri": "${serverUrl}/oauth2/authorize",
  "tokenUri": "${serverUrl}/oauth2/token",
  "userInfoUri": "${serverUrl}/oauth2/userinfo",
  "jwkSetUri": "${serverUrl}/oauth2/jwks",
  "issuerUri": "${serverUrl}",
  "logoutUri": "${serverUrl}/logout?post_logout_redirect_uri=...",
  "userNameAttribute": "sub",
  "attributeMappings": [...],
  "roleConvertExpression": "OAUTH2_*(.*)",
  "additionalParameters": {...}
}

```

|Attribut	|Type	|Description	|
|:----------|:----------|:----------|
|`authorizationUri`|String|Endpoint d'autorisation OAuth2|
|`tokenUri`|String|Endpoint pour obtenir le token d'accès|
|`userInfoUri`|String|Endpoint pour obtenir les informations de l'utilisateur|
|`jwkSetUri`|String|Endpoint pour obtenir les clés publiques JWT|
|`issuerUri`|String|URI de l'émetteur du token (utilisé pour la validation)|
|`logoutUri`|String|URL de déconnexion|
|`userNameAttribute`|String|Attribut utilisé comme nom d'utilisateur (souvent `sub`, `login`, `email`)|
|`attributeMappings`|Array|Mappage des attributs OAuth2 vers les attributs utilisateur RUDI|
|`roleConvertExpression`|String|Expression regex pour convertir les rôles OAuth2|
|`additionalParameters`|Object|Paramètres supplémentaires à envoyer lors de l'authentification|

### Mappage des attributs

Le tableau `attributeMappings` permet de mapper les attributs OAuth2 vers les attributs utilisateur RUDI :

```
"attributeMappings": [
  {
    "userAttribute": "EMAIL",
    "oauth2Attribute": "email"
  },
  {
    "userAttribute": "LASTNAME",
    "oauth2Attribute": "family_name"
  },
  {
    "userAttribute": "FIRSTNAME",
    "oauth2Attribute": "given_name"
  }
]

```

Les attributs utilisateur RUDI disponibles sont : `EMAIL`, `LASTNAME`, `FIRSTNAME`.

### Variables dynamiques

Le système supporte les variables dynamiques dans les URLs avec la syntaxe `${nomVariable}` :

*   `${serverUrl}` : remplacé par la valeur de l'attribut `serverUrl`
*   `${localServerUrl}` : URL du serveur local RUDI
*   `${state}` : état OAuth2 généré dynamiquement (notamment requis pour FranceConnect)
*   `${token}` : valeur du token transmis par le serveur d'identité ( notamment requis pour FranceConnect)

* * *

2\. Configuration pour LemonLDAP::NG
------------------------------------

LemonLDAP::NG est un système de Single Sign-On (SSO) et de gestion d'identité Web.

### Exemple de configuration

```
{
  "name": "lemon",
  "label": "OAuth2 LemonLDAP::NG",
  "description": "External OAuth2 authenticator for LemonLDAP::NG",
  "viewSettings": {
    "iconUrl": "classpath:/authenticators/oauth2-lemon.png"
  },
  "serverUrl": "https://auth.votre-domaine.fr",
  "redirectUrl": "https://rudi.votre-domaine.fr/login/oauth2/code/",
  "clientId": "votre-client-id",
  "clientSecret": "votre-client-secret",
  "authorizationGrantType": "authorization_code",
  "clientAuthenticationMethod": "client_secret_post",
  "scope": "openid,profile,email,rudi_profile",
  "requireProofKey": true,
  "provider": {
    "authorizationUri": "${serverUrl}/oauth2/authorize",
    "tokenUri": "${serverUrl}/oauth2/token",
    "userInfoUri": "${serverUrl}/oauth2/userinfo",
    "jwkSetUri": "${serverUrl}/oauth2/jwks",
    "logoutUri": "${serverUrl}/?logout=1&post_logout_redirect_uri=https://rudi.votre-domaine.fr",
    "userNameAttribute": "sub"
  }
}

```

### Points clés pour LemonLDAP::NG

1.  **Grant type** : Utiliser `authorization_code` pour l'authentification utilisateur
2.  **PKCE** : Recommandé d'activer `requireProofKey: true` pour une sécurité renforcée
3.  **Scopes** :

*   `openid` : obligatoire pour OpenID Connect
*   `profile` : accès au profil utilisateur
*   `email` : accès à l'adresse email
*   `rudi_profile` : scope personnalisé pour les attributs spécifiques RUDI

1.  **Client Authentication** : `client_secret_post` envoie les credentials dans le corps de la requête
2.  **Logout** : Utiliser le paramètre `logout=1` et `post_logout_redirect_uri` pour la déconnexion

### Configuration côté LemonLDAP::NG

Côté serveur LemonLDAP::NG, il faut :

1.  Créer une application OAuth2/OIDC
2.  Définir l'URL de redirection autorisée : `https://rudi.votre-domaine.fr/login/oauth2/code`
3.  Configurer les scopes : `openid`, `profile`, `email`, `rudi_profile`
4.  Activer PKCE si `requireProofKey` est à `true`
5.  Choisir la méthode d'authentification client : POST
6.  Mapper les attributs utilisateur vers les claims OpenID Connect

* * *

3\. Configuration pour FranceConnect et FranceConnect+
------------------------------------------------------

FranceConnect est le système d'authentification de l'État français, permettant aux citoyens de s'identifier avec leurs comptes existants (Impots.gouv.fr, Ameli.fr, etc.).

### FranceConnect (niveau eIDAS 1 - substantiel)

Configuration standard pour FranceConnect avec le niveau d'authentification eIDAS 1 :

```
{
  "name": "rudifranceconnect",
  "label": "Se connecter avec FranceConnect",
  "description": "FranceConnect est la solution proposée par l'État, pour sécuriser et simplifier la connexion à vos services en ligne",
  "viewSettings": {
    "isolated": true,
    "cssClass": "franceconnect",
    "iconUrl": "classpath:/authenticators/franceconnect-btn-principal.svg",
    "hoverIconUrl": "classpath:/authenticators/franceconnect-btn-principal-hover.svg",
    "links": [
      {
        "url": "https://franceconnect.gouv.fr/",
        "label": "Qu'est ce que FranceConnect ?"
      }
    ]
  },
  "serverUrl": "https://fcp.integ01.dev-franceconnect.fr/api/v2",
  "redirectUrl": "https://rudi.votre-domaine.fr/login/oauth2/code/",
  "clientId": "votre-client-id",
  "clientSecret": "votre-client-secret",
  "authorizationGrantType": "authorization_code",
  "clientAuthenticationMethod": "client_secret_post",
  "scope": "openid,given_name,family_name,email",
  "provider": {
    "authorizationUri": "${serverUrl}/authorize",
    "tokenUri": "${serverUrl}/token",
    "userInfoUri": "${serverUrl}/userinfo",
    "jwkSetUri": "${serverUrl}/jwks",
    "issuerUri": "${serverUrl}",
    "logoutUri": "${serverUrl}/session/end?post_logout_redirect_uri=https://rudi.votre-domaine.fr&state=${state}&id_token=${token}",
    "userNameAttribute": "sub",
    "attributeMappings": [
      {
        "userAttribute": "EMAIL",
        "oauth2Attribute": "email"
      },
      {
        "userAttribute": "LASTNAME",
        "oauth2Attribute": "family_name"
      },
      {
        "userAttribute": "FIRSTNAME",
        "oauth2Attribute": "given_name"
      }
    ],
    "additionalParameters": {
      "acr_values": "eidas1"
    }
  }
}

```

### FranceConnect+ (niveau eIDAS 2 et 3)

FranceConnect+ offre des niveaux d'authentification supérieurs pour les services nécessitant une sécurité renforcée.

```
{
  "name": "rudifranceconnectplus",
  "label": "Se connecter avec FranceConnect+",
  "description": "FranceConnect+ offre un niveau de sécurité renforcé pour l'accès aux services sensibles",
  "viewSettings": {
    "isolated": true,
    "cssClass": "franceconnectplus",
    "iconUrl": "classpath:/authenticators/FranceConnect+-btn-principal.svg",
    "hoverIconUrl": "classpath:/authenticators/FranceConnect+-btn-principal-hover.svg",
    "links": [
      {
        "url": "https://franceconnect.gouv.fr/franceconnect-plus",
        "label": "Qu'est-ce que FranceConnect+ ?"
      }
    ]
  },
  "serverUrl": "https://fcp.integ01.dev-franceconnect.fr/api/v2",
  "redirectUrl": "https://rudi.votre-domaine.fr/login/oauth2/code/",
  "clientId": "votre-client-id-fcplus",
  "clientSecret": "votre-client-secret-fcplus",
  "authorizationGrantType": "authorization_code",
  "clientAuthenticationMethod": "client_secret_post",
  "scope": "openid,given_name,family_name,email,birthdate,birthplace,birthcountry",
  "provider": {
    "authorizationUri": "${serverUrl}/authorize",
    "tokenUri": "${serverUrl}/token",
    "userInfoUri": "${serverUrl}/userinfo",
    "jwkSetUri": "${serverUrl}/jwks",
    "issuerUri": "${serverUrl}",
    "logoutUri": "${serverUrl}/session/end?post_logout_redirect_uri=https://rudi.votre-domaine.fr&state=${state}&id_token=${token}",",
    "userNameAttribute": "sub",
    "attributeMappings": [
      {
        "userAttribute": "EMAIL",
        "oauth2Attribute": "email"
      },
      {
        "userAttribute": "LASTNAME",
        "oauth2Attribute": "family_name"
      },
      {
        "userAttribute": "FIRSTNAME",
        "oauth2Attribute": "given_name"
      }
    ],
    "additionalParameters": {
      "acr_values": "eidas2"
    }
  }
}

```

### Points clés pour FranceConnect / FranceConnect+

1.  **Environnements** :

*   Intégration : `https://fcp.integ01.dev-franceconnect.fr/api/v2`
*   Production : `https://app.franceconnect.gouv.fr/api/v2`

1.  **Niveaux eIDAS** (via `acr_values`) :

*   `eidas1` : Niveau substantiel (FranceConnect standard)
*   `eidas2` : Niveau élevé (FranceConnect+)
*   `eidas3` : Niveau fort (FranceConnect+ renforcé)

1.  **Scopes obligatoires** :

*   `openid` : requis
*   `given_name` : prénom
*   `family_name` : nom de famille
*   `email` : adresse email

1.  **Scopes supplémentaires pour FranceConnect+** :

*   `birthdate` : date de naissance
*   `birthplace` : lieu de naissance
*   `birthcountry` : pays de naissance

1.  **Client Authentication** : Toujours `client_secret_post` pour FranceConnect
    
2.  **Isolated Display** : Recommandé de mettre `"isolated": true` pour afficher FranceConnect séparément
    
3.  **Session State** : FranceConnect requiert un `state` différent à chaque requête (géré automatiquement par le système)
    
4.  **Déconnexion** : Utiliser l'endpoint `/session/end` avec le paramètre `post_logout_redirect_uri`
    

### Inscription et configuration côté FranceConnect

1.  S'inscrire sur le [Portail Partenaires FranceConnect](https://partenaires.franceconnect.gouv.fr/)
2.  Créer un fournisseur de service
3.  Déclarer les URLs de redirection autorisées
4.  Obtenir le `client_id` et le `client_secret`
5.  Choisir le niveau eIDAS requis (eidas1, eidas2 ou eidas3)
6.  Valider les scopes nécessaires
7.  Tester en intégration avant de passer en production

### Boutons graphiques

Les boutons FranceConnect doivent respecter la charte graphique officielle :

*   Utiliser les SVG fournis par FranceConnect
*   Ne pas modifier les couleurs ou proportions
*   Afficher le bouton de survol (`hoverIconUrl`) au passage de la souris

Ces resources sont intégrés à RUDI (Cf. `/rudi-microservice-acl-facade/src/main/resources/authenticators`

* * *

4\. Configuration pour GitHub
-----------------------------

GitHub peut être utilisé comme fournisseur d'authentification OAuth2, pratique pour les environnements de développement ou les projets open source.

### Exemple de configuration

```
{
  "name": "rudigithub",
  "label": "OAuth2 GitHub",
  "description": "External OAuth2 authenticator for GitHub",
  "viewSettings": {
    "cssClass": "github",
    "iconUrl": "classpath:/authenticators/oauth2-github.png"
  },
  "serverUrl": "https://github.com",
  "redirectUrl": "http://localhost:8085/login/oauth2/code/",
  "clientId": "votre-client-id-github",
  "clientSecret": "votre-client-secret-github",
  "authorizationGrantType": "authorization_code",
  "scope": "read:user,user:email",
  "provider": {
    "authorizationUri": "${serverUrl}/login/oauth/authorize",
    "tokenUri": "${serverUrl}/login/oauth/access_token",
    "userInfoUri": "https://api.github.com/user",
    "jwkSetUri": "${serverUrl}/login/oauth/jwks",
    "userNameAttribute": "login",
    "attributeMappings": [
      {
        "userAttribute": "EMAIL",
        "oauth2Attribute": "email"
      },
      {
        "userAttribute": "LASTNAME",
        "oauth2Attribute": "login"
      },
      {
        "userAttribute": "FIRSTNAME",
        "oauth2Attribute": "name"
      }
    ],
    "roleConvertExpression": "OAUTH2_*(.*)"
  }
}

```

### Points clés pour GitHub

1.  **Endpoints** :

*   Authorization : `https://github.com/login/oauth/authorize`
*   Token : `https://github.com/login/oauth/access_token`
*   User Info : `https://api.github.com/user`

1.  **Scopes GitHub** :

*   `read:user` : lecture des informations de profil public
*   `user:email` : accès aux adresses email (peut nécessiter `user` pour les emails privés)
*   `user` : accès complet au profil utilisateur

1.  **Attributs utilisateur** :

*   `login` : nom d'utilisateur GitHub (utilisé comme identifiant)
*   `name` : nom complet (peut être null si non renseigné)
*   `email` : adresse email principale

1.  **Mapping des attributs** :

*   Le `login` GitHub est mappé sur `LASTNAME` (nom d'utilisateur)
*   Le `name` GitHub est mappé sur `FIRSTNAME` (nom complet)
*   L'`email` GitHub est mappé sur `EMAIL`

1.  **Expression de conversion des rôles** :

*   `"roleConvertExpression": "OAUTH2_*(.*)"` extrait les rôles en retirant le préfixe `OAUTH2_`
*   Utile si des rôles sont transmis via des attributs OAuth2 personnalisés

1.  **Cas particulier de l'email** :

*   Si l'email n'est pas public, le scope `user:email` doit être utilisé
*   L'API peut retourner plusieurs emails ; le système utilisera l'email principal (`primary: true`)

### Configuration côté GitHub

1.  Se connecter à GitHub
2.  Accéder à **Settings** > **Developer settings** > **OAuth Apps**
3.  Créer une **New OAuth App**
4.  Renseigner :

*   **Application name** : Nom de votre application RUDI
*   **Homepage URL** : URL de votre application (ex: `https://rudi.votre-domaine.fr`)
*   **Authorization callback URL** : `https://rudi.votre-domaine.fr/login/oauth2/code/rudigithub`

1.  Obtenir le **Client ID** et générer un **Client Secret**
2.  Copier ces valeurs dans la configuration

Bonnes pratiques
----------------

### Sécurité

1.  **Secrets** :

*   Ne jamais commiter les `clientSecret` en clair dans le code source
*   Utiliser des variables d'environnement ou un gestionnaire de secrets
*   Exemple : `"clientSecret": "${GITHUB_CLIENT_SECRET}"`

1.  **HTTPS** :

*   Toujours utiliser HTTPS en production pour les `redirectUrl`
*   HTTP est acceptable uniquement en développement local

1.  **PKCE** :

*   Activer `"requireProofKey": true` pour les clients publics
*   Recommandé pour LemonLDAP::NG et tout authentificateur moderne

1.  **Scopes minimaux** :

*   Ne demander que les scopes strictement nécessaires
*   Respecter le principe du moindre privilège

### Organisation des fichiers

1.  **Ressources** :

*   Placer les icônes dans le même répertoire que la configuration
*   Utiliser le préfixe `classpath:/authenticators/` pour référencer les ressources


* * *

Dépannage
---------

### Erreurs courantes

1.  **Redirect URI mismatch** :

*   Vérifier que l'URL de redirection dans le fichier correspond exactement à celle déclarée chez le fournisseur
*   Attention à la casse, au protocole (http vs https) et au trailing slash

1.  **Invalid client** :

*   Vérifier le `clientId` et le `clientSecret`
*   S'assurer que le client est activé côté fournisseur

1.  **Scope invalide** :

*   Vérifier que tous les scopes demandés sont autorisés
*   Certains scopes nécessitent une validation manuelle par le fournisseur

1.  **Token expiration** :

*   Vérifier la configuration du refresh token
*   S'assurer que la gestion de session est correcte

1.  **Attribut manquant** :

*   Vérifier que les attributs mappés existent dans la réponse du fournisseur
*   Utiliser les outils de debug OAuth2 pour inspecter les claims retournés

### Logs utiles

Pour diagnostiquer les problèmes d'authentification, activer les logs DEBUG :

```
logging.level.org.rudi.microservice.acl.facade.config.security.oauth2=DEBUG
logging.level.org.springframework.security.oauth2=DEBUG

```

### Outils de test

1.  **OAuth2 Debugger** : https://oauthdebugger.com/
2.  **JWT.io** : https://jwt.io/ (pour décoder les tokens JWT)
3.  **Postman** : Pour tester manuellement les endpoints OAuth2

* * *

Références
----------

*   [Spécification OAuth 2.0](https://oauth.net/2/)
*   [OpenID Connect](https://openid.net/connect/)
*   [Documentation FranceConnect](https://franceconnect.gouv.fr/partenaires)
*   [Documentation GitHub OAuth](https://docs.github.com/en/developers/apps/building-oauth-apps)
*   [LemonLDAP::NG](https://lemonldap-ng.org/)
*   [PKCE RFC 7636](https://tools.ietf.org/html/rfc7636)