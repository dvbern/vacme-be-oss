<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "title">
        ${msg("loginTitle",realm.name)}
    <#elseif section = "form">
        <div class="govuk-grid-row">
            <p style="color: red;font-size: 1.3em;border: 2px solid red;padding: 8px;">${msg("rateLimiterHit")}</p>
        </div>
        <#if client?? && client.baseUrl?has_content>
            <div id="kc-return" style="margin-top: 24px;" class="${properties.kcFormButtonsClass!}">
                <a href="${client.baseUrl}">${msg("backToApplication")}</a>
            </div>
        </#if>
    </#if>
</@layout.registrationLayout>
