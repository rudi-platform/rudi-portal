# Installation locale

Pour faire fonctionner le NodeStub sur un poste de dev local, il faut ajuster certains points.

## URL du NodeProvider

Dans le schéma `strukture_data`, ouvrir la table `provider` et noter l'UUID du fournisseur portant le code `'NODE_STUB'`
.

Ouvrir la table `node_provider` et se placer sur la ligne portant cet UUID. Remplacer alors son `url` par :

```
http://localhost:28001/nodestub
```

## Création des dossiers locaux

Pour créer ses rapports d'intégration, le NodeStub a besoin de pouvoir écrire dans le dossier défini par la propriété 

```
rudi.appdata=D:/Projets/workspace-rm-rudi/rudi-volume/data
```

S'il n'est pas possible de créer ce dossier sur son poste, alors on peut changer le répertoire en surchargeant le
paramètre JVM au lancement de l'application NodeStub. Exemple :

```
-Drudi.nodestub.reports-directory=<chemin>/nodestub/reports
-Drudi.nodestub.resources-directory=<chemin>/nodestub/resources
```

