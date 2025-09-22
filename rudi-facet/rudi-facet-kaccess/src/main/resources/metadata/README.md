Doc de Dataverse : <https://guides.dataverse.org/en/6.5/admin/metadatacustomization.html>.

# Ajout de champs Dataverse

1. On génère le fichier [rudi.tsv](rudi.tsv) avec maven
2. On importe le fichier dans Dataverse.
3. On propage les modifications

# Génération du fichier rudi.tsv par maven

Lancer le Run Configuration "Mettre à jour rudi.tsv" qui lance le main de la
classe [RudiTsvUpdator](../../java/org/rudi/facet/kaccess/helper/tsv/RudiTsvUpdator.java).

# Upload du fichier sur Dataverse

Lancer la commande curl :

```
curl -k -v https://<dataverse-host>:<dataverse-port>/api/admin/datasetfield/load -H "Content-type: text/tab-separated-values" -X POST --upload-file rudi.tsv
```

On fait ensuite un RELOAD Solr en appelant ce script sur la machine hébergeant Dataverse (à adapter en fonction de
l'instance) :

Commencer par créer une copie du fichier **schema.xml** : **schema-copy.xml**
Si elle existe déjà, la remplacer.

Puis récupéré la liste des champs depuis Dataverse pour mettre à jour le fichier schema.xml via le script
update-fields.sh fourni par Dataverse :

```
curl "https://<dataverse-host>:<dataverse-port>/api/admin/index/solr/schema" | sudo update-fields.sh <solrSchemaPath>/schema-copy.xml
```

### Si rajout d'une facet dataverse

Il sera nécessaire d'aller dans le dataverse rudi_root dans `Edit > General Information` et de modifier la section
`Browse/Search Facets` pour y rajouter notre nouvelle facet dans la section **Selected**.

# Vérifications

Pour vérifier la configuration d'un champ dans Dataverse, on peut appeler directement son API.

Exemple pour le champ `rudi_media_type` :

```
https://<dataverse-host>:<dataverse-port>/api/admin/datasetfield/rudi_media_type
```

On peut aussi vérifier que le catalogue et le détail s'affichent toujours bien.

# Propagation des modifications pour Ansible/Karbon

L'ajout de champs modifie les fichiers suivants de l'instance Dataverse :

- **schema.xml** : Mettre à jour les <copieField...>
- **schema_dv_mdb_fields.xml** : Mettre à jour les <field...>
- **schema_dv_mdb_copies.xml** : Mettre à jour les <copieField...>

À chaque déploiement Ansible/Karbon, ces fichiers sont écrasés.
Il faut donc récupérer ces fichiers depuis la machine (ou la VM ou le pod) ciblée et écraser les fichiers correspondants
dans [ce dossier](../../../../../../ci/docker-compose/dataverse/solr-data/collection1/conf).

**Attention cependant** : certaines modifications doivent être conservées car Dataverse ne les prend pas en compte dans
le fichier tsv.
Notamment :

- **schema.xml** : n'écraser ce fichier que si les modifications apportées sont réellement voulues
- **schema_dv_mdb_fields.xml** : ne pas écraser le type des champs qui ont le type :
    - `text_fr`
    - `rudi_id`
- **schema_dv_mdb_copies.xml** :

# Annulation de modifications

Certaines modifications ne peuvent pas être effectuées via l'API. Pour cela, il est nécessaire de se connecter
directement sur la base
SQL de Dataverse.

## Passage du type CONTROLLEDVOCABULARY à PRIMITIVE

Exemple avec un champ portant l'id `225` dans la table `datasetfieldtype` :

```postgresql
-- On supprime le contrôle des valeurs pour le champ
UPDATE datasetfieldtype
SET allowcontrolledvocabulary = FALSE
WHERE id = 225;
-- On supprime les valeurs autorisées pour le champ
DELETE
FROM controlledvocabularyvalue
WHERE datasetfieldtype_id = 225;
```

[RudiMetadataField]: ../../../main/java/org/rudi/facet/kaccess/constant/RudiMetadataField.java

# Liens complémentaires

[Documentation dataverse pour le réindexation](https://guides.dataverse.org/en/latest/admin/solr-search-index.html)