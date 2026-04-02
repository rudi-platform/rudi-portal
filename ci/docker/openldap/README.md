# OpenLDAP RUDI

Image OpenLDAP basée sur Debian Trixie pour la plateforme RUDI.

## Structure des fichiers

```
openldap/
├── Dockerfile              # Image Docker basée sur debian:trixie
├── entrypoint.sh           # Script d'initialisation et démarrage
├── 01_ldap_ou.ldif         # Structure de base : DC, OU, groupe SG
├── 02_ldap_users.ldif      # Utilisateurs de test
├── 03_ldap_groups.ldif     # Groupes et membres
└── README.md
```

## Variables d'environnement

| Variable | Valeur par défaut | Description |
|---|---|---|
| `LDAP_PORT_NUMBER` | `1389` | Port d'écoute TCP |
| `LDAP_ROOT` | `dc=rudi,dc=fr` | Base DN du répertoire |
| `LDAP_ADMIN_USERNAME` | `admin` | Nom de l'administrateur |
| `LDAP_ADMIN_PASSWORD` | `le_password` | Mot de passe administrateur |
| `LDAP_ADD_SCHEMAS` | `yes` | Ajouter les schémas standard (cosine, inetorgperson, nis) |
| `LDAP_SKIP_DEFAULT_TREE` | `yes` | Ne pas créer la structure par défaut (utiliser les LDIF) |
| `LDAP_CUSTOM_LDIF_DIR` | `/ldifs` | Répertoire des fichiers LDIF d'initialisation |
| `LDAP_CUSTOM_SCHEMA_FILE` | `/schemas/schema.ldif` | Fichier de schéma personnalisé |
| `LDAP_LOGLEVEL` | `256` | Niveau de log slapd |
| `LDAP_ALLOW_ANON_BINDING` | `yes` | Autoriser les connexions anonymes |
| `LDAP_CONFIG_ADMIN_ENABLED` | `no` | Activer un admin de config séparé |
| `LDAP_CONFIG_ADMIN_USERNAME` | `admin` | Nom de l'admin de config |
| `LDAP_CONFIG_ADMIN_PASSWORD` | `configpassword` | Mot de passe admin de config |
| `LDAP_ULIMIT_NOFILES` | `1024` | Limite de fichiers ouverts |

## Construction et démarrage

```bash
# Construction de l'image
sudo docker build -t "rudi/openldap" .

# Lancement simple
sudo docker run -d --name openldap -p 1389:1389 rudi/openldap

# Lancement avec variables personnalisées
sudo docker run -d --name openldap \
  -p 1389:1389 \
  -e LDAP_ADMIN_PASSWORD="monpassword" \
  rudi/openldap

# Avec volumes pour persister les données
sudo docker run -d --name openldap \
  -p 1389:1389 \
  -v openldap-data:/var/lib/ldap \
  -v openldap-config:/etc/ldap/slapd.d \
  rudi/openldap
```

## Structure du répertoire LDAP

```
dc=rudi,dc=fr
└── ou=internes
    ├── cn=users (groupOfNames)
    ├── uid=test1
    ├── uid=test2
    ├── uid=test3
    └── ou=groups
        ├── cn=administrator
        │   └── member: uid=test1
        └── cn=users
            ├── member: uid=test2
            └── member: uid=test3
```

## Tests

### Connectivité de base
```bash
# Test anonyme
docker exec openldap ldapwhoami -x -H ldap://localhost:1389
# Attendu : anonymous

# Test authentifié (utilisateur test1)
docker exec openldap ldapwhoami -x \
  -H ldap://localhost:1389 \
  -D "uid=test1,cn=users,ou=internes,dc=rudi,dc=fr" \
  -w "le_password"
# Attendu : dn:uid=test1,cn=users,ou=internes,dc=rudi,dc=fr
```

### Recherche d'éléments
```bash
# Lister toute la base
docker exec openldap ldapsearch -x \
  -H ldap://localhost:1389 \
  -b "dc=rudi,dc=fr"

# Rechercher un utilisateur par uid
docker exec openldap ldapsearch -x \
  -H ldap://localhost:1389 \
  -b "cn=users,ou=internes,dc=rudi,dc=fr" \
  "(uid=test1)"

# Rechercher des utilisateurs avec filtre
docker exec openldap ldapsearch -x \
  -H ldap://localhost:1389 \
  -b "dc=rudi,dc=fr" \
  "(&(objectClass=person)(uid=test*))"

# Lister les groupes
docker exec openldap ldapsearch -x \
  -H ldap://localhost:1389 \
  -b "ou=groups,ou=internes,dc=rudi,dc=fr"

# Recherche d'éléments dans le ldap en tant qu'administrateur
docker exec openldap ldapsearch -x \
  -H ldap://localhost:1389 \
  -D "cn=admin,dc=rudi,dc=fr" \
  -w "le_password" \
  -b "dc=rudi,dc=fr" \
  "(&(objectclass=person)(uid=test*))"
```


### Opérations administratives (dans le conteneur)

```bash
# Ajouter un schéma personnalisé (offline)
slapadd -F "/etc/ldap/slapd.d/" -n 0 -l /schemas/schema.ldif

# Ajouter des entrées via ldapadd
ldapadd -f le_fichier.ldif \
  -H ldap://localhost:1389 \
  -D "cn=admin,dc=rudi,dc=fr" \
  -w "le_password"
```

## Notes importantes

- Les données LDIF sont importées via `slapadd` (direct en base) au premier démarrage
- Le fichier `/var/lib/ldap/.initialized` marque que l'initialisation a déjà été effectuée
- Pour forcer une réinitialisation : supprimer ce fichier et redémarrer le conteneur
- slapd s'exécute en tant que l'utilisateur système `openldap`

## commandes utiles
### Ajout de schéma dans l'image

slapadd -F "/etc/ldap/slapd.d" -n 0 -l /schemas/schema.ldif

### Ajout de fichier ldif à l'image

ldapadd -f le_fichier.ldif -H 'ldapi:///' -D "cn=admin,dc=rudi,dc=fr" -w "le_password"

### Recherche d'éléments dans le ldap
ldapsearch -H 'ldapi:///' -D "cn=admin,dc=rudi,dc=fr" -w "le_password" -b "dc=rudi,dc=fr"  "(&(objectclass=person)(uid=test*))"
