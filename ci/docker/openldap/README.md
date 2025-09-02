# Ajout de schéma dans l'image

slapadd -F "/bitnami/openldap/slapd.d/" -n 0 -l /schemas/schema.ldif

# Ajout de fichier ldif à l'image

ldapadd -f le_fichier.ldif -H 'ldapi:///' -D "cn=admin,dc=rudi,dc=fr" -w "le_password"

# Recherche d'éléments dans le ldap
ldapsearch -H 'ldapi:///' -D "cn=admin,dc=rudi,dc=fr" -w "le_password" -b "dc=rudi,dc=fr"  "(&(objectclass=person)(uid=test*))"

# Création de l'image (pour test)

sudo docker build -t "rudi/openldap" .

# Lancement du container (pour test)

sudo docker run -p 1389:1389 rudi/openldap
