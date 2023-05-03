<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "title" || section = "header">
        ${msg("sms-auth.title")}
    <#elseif section = "form">
    <#--
		Hack-alert: Keycloak doesn't provide per-field error messages here,
		so we check global message for need to display validation error styling
	-->
        <#if message?has_content && message.type = "error">
            <#assign errorClass = "govuk-form-group--error" >
        </#if>
		<div class="govuk-grid-row">
			<div class="govuk-grid-column-full">
				<p class="sms-instruction">${msg("sms-auth.instruction")}</p>
			</div>
			<form id="kc-totp-login-form" action="${url.loginAction}" method="post">
				<div class="form-group">
					<div class='inputfield govuk-grid-column-full ${errorClass!""}'>
						<input id="totp" name="smsCode" type="number"
							   class='notice'
							   placeholder='${msg("sms-auth.code")}'
							   autocomplete="false"
						       autofocus>
						<label for="totp" class='floating-label floating-space-left'>${msg("sms-auth.code")}</label>
					</div>
				</div>
				<div id="kc-logins" class="govuk-grid-column-full">
					<button class='go-next' type="submit">
						<img src="${url.resourcesPath}/img/right-arrow-white.svg" style="margin-right: 10px; height: 19px; width: auto; vertical-align: middle; margin-bottom: 1px;">
						<span>${msg("doSubmit")}</span>
						<img src="${url.resourcesPath}/img/right-arrow-white.svg"
							 style="margin-left: 17px;height:19px;width:auto;vertical-align:middle;margin-bottom: 1px;">
					</button>
				</div>

                <#if client?? && client.baseUrl?has_content>
				<div id="kc-return" class="govuk-grid-column-full">
					<button class='go-back col-md-4' type="button" name="goBack" value="goBack" onclick="location.href='${client.baseUrl}';">
						<img src="${url.resourcesPath}/img/go-back.svg"
							 style="margin-right:13px;height:19px;width:auto;vertical-align:middle;margin-bottom: 1px;">
						<span>${msg("backToApplication")}</span>
					</button>
				</div>
                </#if>
			</form>


		</div>


    </#if>
</@layout.registrationLayout>
