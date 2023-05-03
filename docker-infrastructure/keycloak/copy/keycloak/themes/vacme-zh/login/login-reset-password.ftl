<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "header">
        ${msg("emailForgotTitle")}
    <#elseif section = "form">
		<form id="kc-reset-password-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
			<div class='inputfield'>

<#--				<div class="${properties.kcInputWrapperClass!}">-->
                    <#if auth?has_content && auth.showUsername()>
						<input type="text" id="username" name="username" class='notice' autofocus value="${auth.attemptedUsername}"/>
                    <#else>
						<input type="text" id="username" name="username" class='notice' autofocus/>
                    </#if>
<#--				</div>-->
<#--				<div class="${properties.kcLabelWrapperClass!}">-->
					<label for="username" class='floating-label'>
                        <#if !realm.loginWithEmailAllowed>${msg("username")}
                        <#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}
                        <#else>${msg("email")}
                        </#if>
					</label>
<#--				</div>				-->
			</div>
			<div class="hint marginBottom10">
				<p>${msg("passwordResetInputfieldInstruction")}</p>
			</div>
			<div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!}">
				<div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
					<div class="${properties.kcFormOptionsWrapperClass!}">
						<span><a href="${url.loginUrl}">${kcSanitize(msg("backToLogin"))?no_esc}</a></span>
					</div>
				</div>

				<div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
					<button class='go-next' type="submit">
						<img src="${url.resourcesPath}/img/right-arrow-white.svg" style="margin-right: 10px; height: 19px; width: auto; vertical-align: middle; margin-bottom: 1px;">
						<span>${msg("doSubmit")}</span>
						<img src="${url.resourcesPath}/img/right-arrow-white.svg"
							 style="margin-left: 17px;height:19px;width:auto;vertical-align:middle;margin-bottom: 1px;">
					</button>
				</div>
			</div>
		</form>
    <#elseif section = "info" >
        ${msg("emailInstruction")}
    </#if>
</@layout.registrationLayout>
