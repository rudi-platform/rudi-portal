# Configuration de LemonLDAP::NG

Ce répertoire contient des fichiers de configuration d'exemple pour LemonLDAP::NG.

## Fichier `lmConf-1.json`

Le fichier `lmConf-1.json` est un exemple de configuration de LemonLDAP pour Rudi. 

Il doit être placé dans le répertoire `/var/lib/lemonldap-ng/conf` du conteneur Docker de LemonLDAP::NG.

Parmi les informations à mettre à jour, vous devez notamment :
- Modifier les valeurs de `example.com` pour correspondre à votre domaine.
- Corriger les informations LDAP si nécessaire
- Modifier les identifiants de connexion (`oidcRPMetaDataOptionsClientID` et `oidcRPMetaDataOptionsClientSecret`) pour correspondre à ceux de votre configuration OIDC.
- Modifier les clés de chiffrement (`oidcServicePrivateKeySig` et `oidcServicePrivateKeyEnc`)

Le script `generate-conf.sh` peut être utilisé pour vous aider à générer des clés et identifiants.



## Fichier `rudi-nginx.conf`

Le fichier `rudi-nginx.conf` est un exemple de configuration NgINX pour Rudi. 

Il doit être placé dans le répertoire `/etc/nginx/sites-enabled` du conteneur Docker de LemonLDAP::NG.