[#-------------- ASSIGNMENTS --------------]
[#include "/rudi/templates/macros/localized.ftl"/]
[#include "/rudi/templates/macros/normalized.ftl"/]
[#assign lang = ctx.getParameter('lang')!"fr"]
[#assign newsContent = ctx.newsContent!]

[#-------------- RENDERING ----------------]
[#if newsContent?has_content]
    <div class="news-container">
        [#assign newsContentNode = cmsfn.asJCRNode(newsContent)!]
        [#if newsContentNode?has_content]
            [#assign id = newsContent["jcr:uuid"]!]
            [#assign urlTitle = normalized(localized(newsContent,"title1",lang))!]
        [/#if]
        <a href="@self/news/${id!}/rudi-news@one-news-detailed/${urlTitle}">
            ${urlTitle!}
        </a>
    </div>
[/#if]