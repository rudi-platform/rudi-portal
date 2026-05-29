#!/bin/bash

# Script pour générer une paire de clés RSA et les formater pour le JSON
# Usage: ./generate-keys.sh [private_key_name] [public_key_name]

PRIVATE_KEY="${1:-private_key.pem}"
PUBLIC_KEY="${2:-public_key.pem}"
KEYSIZE="${3:-2048}"

echo "Génération de la paire de clés RSA..."
echo "======================================"

# Générer la clé privée
openssl genrsa -traditional -out "$PRIVATE_KEY" "$KEYSIZE"

if [ $? -ne 0 ]; then
    echo "Erreur lors de la génération de la clé privée"
    exit 1
fi

# Extraire la clé publique
openssl rsa -in "$PRIVATE_KEY" -pubout -out "$PUBLIC_KEY"

if [ $? -ne 0 ]; then
    echo "Erreur lors de l'extraction de la clé publique"
    exit 1
fi

echo ""
echo "Clés générées avec succès !"
echo "- Clé privée: $PRIVATE_KEY"
echo "- Clé publique: $PUBLIC_KEY"
echo ""
echo "======================================"
echo "Format JSON pour lmConf-1.json:"
echo "======================================"
echo ""

# Formater les clés pour JSON : chaque ligne est suivie de \n littéral
PRIVATE_FORMATTED=$(awk '{printf "%s\\n", $0}' "$PRIVATE_KEY")
PUBLIC_FORMATTED=$(awk '{printf "%s\\n", $0}' "$PUBLIC_KEY")

# Afficher au format JSON
cat << EOF
   "oidcServicePrivateKeySig" : "$PRIVATE_FORMATTED",
   "oidcServicePublicKeySig" : "$PUBLIC_FORMATTED",
EOF

echo ""

# Générer 2 UUID pour l'OIDC client
CLIENT_ID=$(powershell -Command "[guid]::NewGuid().ToString()" || uuidgen)
CLIENT_SECRET=$(powershell -Command "[guid]::NewGuid().ToString()" || uuidgen)

echo "======================================"
echo "UUIDs OIDC générés:"
echo "======================================"
echo ""
cat << EOF
         "oidcRPMetaDataOptionsClientID" : "$CLIENT_ID",
         "oidcRPMetaDataOptionsClientSecret" : "$CLIENT_SECRET",
EOF
echo ""

echo "======================================"
echo "Pour remplacer le host:"
echo "======================================"
echo ""
echo sed -i 's/example.com/myhost/g' lmConf-1.json

