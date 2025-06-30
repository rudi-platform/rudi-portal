# Comment mettre à jour les diagrammes de classe ?

Les diagrammes 📊 de classes sont générés automatiquement par les plugins maven
* *plantuml-generator-maven-plugin* pour la génération des fichiers lisibles par plantuml (fichiers .puml)
* *plantuml-maven-plugin* pour la génération des images correspondant aux fichiers puml

Pour mettre à jour ces diagrammes, utiliser la commande maven

```bash
cd rudi-portal
mvn clean install -DPLANTUML_LIMIT_SIZE=8192 -DskipTests -DgenerateUml
```

Commiter les fichiers modifiés par cette commande ( *.puml, *-storage-entities.png)