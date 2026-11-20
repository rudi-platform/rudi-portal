
# Configuration Magnolia pour RUDI

## ContentTypes

#### Généralités

Il existe 3 types de contenu :

* **term** (*contentTypes/term.yaml*) - ce type de contenu permet d'affiche des éléments de type Copyright, CGU etc.
* **news** (*contentTypes/news.yaml*) - ce type de contenu permet d'affiche des actualités
* **projectvalue** (*contentTypes/projectvalue.yaml*) - ce type de contenu permet d'affiche une valeur du projet

#### Tout sur les ContentTypes

* https://docs.magnolia-cms.com/product-docs/6.2/Features/Content-Types.html
* https://docs.magnolia-cms.com/product-docs/6.2/Modules/List-of-modules/Content-Types-module/Content-type-definition/Content-type-Model-definition.html#_submodels
* https://docs.magnolia-cms.com/product-docs/6.2/Developing/Content-Types-tutorial/Part-I-My-first-content-type.html
* https://documentation.magnolia-cms.com/display/DOCS57/Select+field
* https://docs.magnolia-cms.com/product-docs/6.2/Administration/How-to-delete-a-JCR-workspace.html

## Applications

A chaque type de contenu est associé une application (Exemple: *apps/news-app.yaml*).
Cette application permet d'éditer de nouveaux contenus pour chacun des types et de définir les règles d'affichage.

## APIs

4 APIs ont été exposées :

* categories (`http(s)://<host:port>/.rest/delivery/categories/v1`) : permet de lister les catégories
* news (`http(s)://<host:port>/.rest/delivery/news/v1`) : permet de lister les actualités
* projectvalues (`http(s)://<host:port>/.rest/delivery/projectvalues/v1`) : permet de lister les valeurs du projet
* terms (`http(s)://<host:port>/.rest/delivery/terms/v1`) : permet de lister les "terms"

Chacune des APIs peut prendre en paramètre :

* offset
* limit
* order
* `<attribute>=<value>`

Exemple :
`http(s)://<host:port>/.rest/delivery/terms/v1?offset=0&categories=<uuid>&name=CGU`

## Templates

#### Tous sur les templates

* https://docs.magnolia-cms.com/product-docs/6.2/Developing/Templating.html
* https://docs.magnolia-cms.com/product-docs/6.2/Developing/Templating/Renderers.html
* https://docs.magnolia-cms.com/product-docs/6.2/Developing/Templating/Template-definition/Component-definition.html
* https://docs.magnolia-cms.com/product-docs/6.2/Developing/Templating/Template-scripts/Rendering-context-objects.html
* https://docs.magnolia-cms.com/product-docs/6.2/Developing/Templating/Template-scripts/Directives.html
* https://docs.magnolia-cms.com/product-docs/6.2/Developing/Templating/Template-scripts/Templating-functions.html
* https://git.magnolia-cms.com/projects/MODULES/repos/templating-essentials/browse/magnolia-templating-kit-2

##### Gestion des "Terms" (CGU, Copyrights...)

Le template "term" (*templates/pages/term.yaml*) permet de créer une page contenant un composant "termOne".

Le composant "termOne" (*templates/components/termOne.yaml*) permet d'insérer :

* soit un composant termDetail (affichage de l'actualité de manière complète)
* soit un composant termSimple (affichage de l'actualité de manière simplifiée - lien cliquable uniquement)

Il est donc possible - afin d'afficher un objet "Term" en mode simple ou détaillé - d'appeler la page sous la forme :

`http(s)://<host:port>/<nom de la page>?id=<id du term que l'on souhaite afficher>`

Le template "terms" (*templates/pages/terms.yaml*) permet de créer une page contenant (entre autre) un composant "termList".

Le composant "termList" (*templates/components/termList.yaml*) permet d'insérer :

* soit un composant termDetail
* soit un composant termSimple

Il est donc possible - afin d'afficher tous les "Terms" en mode simple ou détaillé - d'appeler la page sous la forme :

`http(s)://<host:port>/<nom de la page>`

##### Gestion des news

Le template "news" (*templates/pages/news.yaml*) permet de créer une page contenant un composant "newsOne".

Le composant "newsOne" (*templates/components/newsOne.yaml*) permet d'insérer :

* soit un composant newDetail (affichage de l'actualité de manière complète)
* soit un composant newSimple (affichage de l'actualité de manière simplifiée)

Il est donc possible - afin d'afficher un objet "News" en mode simple ou détaillé - d'appeler la page sous la forme :

`http(s)://<host:port>/<nom de la page>?id=<id de l'actualité que l'on souhaite afficher>`

Le template "newss" (*templates/pages/newss.yaml*) permet de créer une page contenant (entre autre) un composant "newsList".

Le composant "newsList" (*templates/components/newsList.yaml*) permet d'insérer :

* soit un composant newDetail (affichage de l'actualité de manière complète)
* soit un composant newSimple (affichage de l'actualité de manière simplifiée)

Il est donc possible - afin d'afficher tous les "News" en mode simple ou détaillé - d'appeler la page sous la forme :

`http(s)://<host:port>/<nom de la page>`

##### Bouton "Publier ses données" (catalogue des jeux de données)

Ce bouton (US RUDI-6720) est piloté par le CMS via la description
`cmsDatasetsButtonDescription` de la `customization.json` du microservice konsult :

    "cmsDatasetsButtonDescription": {
        "assetType": "PROJECTVALUES",
        "category": "/rudi/projectvalues/datasets-button",
        "templateSimple": "rudi-project-values@one-button"
    }

Le bouton est un **composant** `projectvalueButton`
(*templates/components/projectvalue/projectvalueButton*) inséré dans l'aire
`projectvalueOne` (au même titre que `projectvalueSimple`). Il rend un bouton
**entièrement stylé côté Magnolia** (style porté par *webresources/css/projectvalues.css*,
classes `.projectvalue-button` / `.projectvalue-button-container`) pointant vers l'article
"valeur projet" catégorisé. Le libellé du bouton est le champ **`shorttitle`** du contenu
(le `title` sert au slug d'URL du détail).

Le front **insère la rédition Magnolia telle quelle** (`[innerHTML]`, cf.
`dataset-list-banner.component`)

Comme konsult ne rend que des pages `.html`, le composant est hébergé par la page de
rendu `one-button` (template de page `rudi:pages/rudi-item` → composant `projectvalueOne`
→ aire `projectvalueOne` → composant `projectvalueButton`).

**MEP — contenu à provisionner dans Magnolia (non fourni par le déploiement applicatif) :**

1. Créer la catégorie `/rudi/projectvalues/datasets-button` (workspace `category`).
2. Créer un contenu "valeur projet" (app *projectvalues*) taggé avec cette catégorie :
   il porte l'URL cible du bouton ; renseigner `shorttitle` = libellé du bouton.
3. Créer la page `one-button` sous `rudi-project-values` (workspace `website`),
   template `rudi:pages/rudi-item`, avec dans l'aire `main` un composant `projectvalueOne`
   contenant lui-même un composant `projectvalueButton` dans son aire `projectvalueOne`.

**Vérification :** le bouton n'apparaît sur le catalogue des jeux de données que si
ces 3 éléments existent (sinon il est masqué, sans erreur). On peut contrôler la
catégorie via `/.rest/delivery/categories/v1` et le rendu via
`/rudi/rudi-project-values/one-button.html?id=<uuid>&lang=fr`.

## Répliquer les données de Magnolia entre 2 environnements

Sur l'environnement source :

* Faire un dump de la base de données (schéma public) (ci-après *magnolia.yyyymmdd.dump*)
* Faire un tgz du répertoire *[...]/data/repository* (ci-après *repository.yyyymmdd.tgz*)

```
 sudo tar zcvf repository.yyyymmdd.tgz data.repository
```

Sur l'environnement cible

* Dans l'interface Magnolia :

  - Saisir dans la barre de recherche *"magnoliaPublic8080"*

  Un item de configuration apparait. [magnoliaPublic8080.png](magnoliaPublic8080.png)
  Copier la valeur de l'attribut *"url"*

  - Saisir dans la barre de recherche *"publicKey"*

  Un item de configuration apparait. [publicKey.png](publicKey.png)
  Copier la valeur de l'attribut *"publicKey"*
* Stopper le container Magnolia (pas la base)
* Faire un dump de sauvegarde de la base de données (schéma public)
* Supprimer le schema public et le récréer
* Restaurer la sauvegarde *magnolia.yyyymmdd.dump* réalisée sur l'environnement *source*
* Faire un tgz de sauvegarde du répertoire *[...]/data/repository*
* Supprimer le contenu du répertoire *[...]/data/repository*
* Détarrer le *repository.yyyymmdd.tgz*

```
 sudo tar zxvf repository.yyyymmdd.tgz
```

* Purger le répertoire *[...]/data/workspaces*
* Rédémarrer le container Magnolia
* Restaurer les valeurs de *magnoliaPublic8080.url" et *"publicKey"*

## Tips

* https://demo.magnolia-cms.com/
* https://docs.magnolia-cms.com/headless/getting-started-with-Magnolia-headless.html
* https://git.magnolia-cms.com/projects/DEMOS
* https://www.magnolia-cms.com/blog/customising-the-richtext-field-using-ckeditor.html (configuration du ckeditor)
* https://docs.magnolia-cms.com/product-docs/6.2/Developing/Templating/Dialog-definition/Field-definition/List-of-fields/CKEditor-customization.html
