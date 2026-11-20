[#-------------- ASSIGNMENTS --------------]
[#include "/rudi/templates/macros/localized.ftl"/]
[#include "/rudi/templates/macros/normalized.ftl"/]
[#assign lang = ctx.getParameter('lang')!"fr"]
[#assign projectvalueContent = ctx.projectvalueContent!]

[#-------------- RENDERING --------------]
[#-- Composant bouton "Publier ses données" : rend un bouton stylé (style porté par
     projectvalues.css) pointant vers l'article "valeur projet" (détail). Le front insère
     cette rédition telle quelle. L'URL est régénérée à chaque rendu (robuste au changement de titre). --]
[#if projectvalueContent?has_content]

    [#assign projectvalueNode = cmsfn.asJCRNode(projectvalueContent)!]

    [#if projectvalueNode?has_content]
        [#assign id = projectvalueContent["jcr:uuid"]!]
        [#assign urlTitle = normalized(localized(projectvalueContent,"title",lang))!]

        <div class="projectvalue-container projectvalue-button-container">
            <link rel="stylesheet" type="text/css"
                  href="${ctx.contextPath}/.resources/rudi/webresources/css/projectvalues.css"/>
            <a class="projectvalue-button"
               href="@self/projectvalues/${id!}/rudi-project-values@one-projectvalue-detailed/${urlTitle!}">
                ${(localized(projectvalueContent,"title",lang))}
            </a>
        </div>
    [/#if]
[/#if]
