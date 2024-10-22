# Portail Rudi

## Présentation

Ce répertoire regroupe le code du portail Rudi.
Une instance du portail Rudi est accessible à l'adresse : [https://rudi.rennesmetropole.fr](https://rudi.rennesmetropole.fr)

La documentation générale sur le projet Rudi est disponible à cette
adresse : [https://rudi.fr/yeswiki](https://rudi.fr/yeswiki/?PagePrincipale).

La documentation sur l'utilisation du portail est disponible
ici : [https://rudi-platform.github.io/rudi-documentation/](https://rudi-platform.github.io/rudi-documentation/)

## Installation

Les étapes d'installation et un certain nombre de composants se trouvent dans [ci/docker-compose/README.md](ci/docker-compose/README.md)

# Visualisation des diagrammes de classe

Les diagrammes de classe sont générés automatiquement par les plugins maven
* * plantuml-generator-maven-plugin * pour la génération des fichiers lisibles par plantuml (fichiers .puml)
* * plantuml-maven-plugin * pour la génération des images correspondant aux fichiers puml

Pour mettre à jour ces diagrammes, utiliser la commande maven

```  
mvn clean install -DPLANTUML_LIMIT_SIZE=8192 -DskipTests -DgenerateUml
```

Commiter les fichiers modifiés par cette commande ( *.puml, *-storage-entities.png)

