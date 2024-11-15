[#-------------- ASSIGNMENTS --------------]
[#include "/rudi/templates/macros/localized.ftl"/]
[#include "/rudi/templates/macros/normalized.ftl"/]
[#assign lang = ctx.getParameter('lang')!"fr"]
[#assign projectvalueContent = ctx.projectvalueContent!]

[#-------------- RENDERING --------------]
[#if projectvalueContent?has_content]

    [#assign projectvalueNode = cmsfn.asJCRNode(projectvalueContent)!]

    [#if projectvalueNode?has_content]
        [#assign id = projectvalueContent["jcr:uuid"]!]
        [#assign urlTitle = normalized(localized(projectvalueContent,"title",lang))!]
    [/#if]

    <div class="projectvalue-container">

        <a class="card-click-redirection"
           href="@self/projectvalues/${id!}/rudi-project-values@one-projectvalue-detailed/${urlTitle!}">
            ${urlTitle!}
        </a>
    </div>
[/#if]