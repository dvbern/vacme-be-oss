<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "title">
        ${msg("loginTitle",realm.name)}
    <#elseif section = "form">
		<div class="govuk-grid-row">
			<form id="kc-totp-login-form" class="${properties.kcFormClass!} govuk-grid-column-two-thirds" action="${url.loginAction}" method="post">
				<div class="govuk-form-group">
					<div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
						<div class="${properties.kcFormOptionsWrapperClass!}">
						</div>
					</div>
				</div>
			</form>
		</div>
        <#if client?? && client.baseUrl?has_content>
			<div id="kc-return" class="${properties.kcFormButtonsClass!}">
				<button class='go-back col-md-4' type="button" name="goBack" value="goBack" onclick="location.href='${client.baseUrl}';">
					<img src="${url.resourcesPath}/img/go-back.svg" style="margin-right:13px;height:19px;width:auto;vertical-align:middle;margin-bottom: 1px;">
					<span>${msg("backToApplication")}</span>
					<img src="${url.resourcesPath}/img/go-back.svg"
						 style="margin-left: 17px;height:19px;width:auto;vertical-align:middle;margin-bottom: 1px;">
				</button>
			</div>
        </#if>
    </#if>
</@layout.registrationLayout>
