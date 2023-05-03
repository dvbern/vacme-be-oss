<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=social.displayInfo displayWide=(realm.password && social.providers??); section>
    <#if section = "header">
        <#if realm.name != 'vacme' >
			<img src="${url.resourcesPath}/img/syringe.svg"
				 style="margin-right: 10px; height: 28px; width: auto; vertical-align: middle; margin-bottom: 1px;">
        </#if>
		<span>${msg("doLogIn")}</span>
    <#elseif section = "form">
		<div id="kc-form" <#if realm.password && social.providers??>class="${properties.kcContentWrapperClass!}"</#if>>
			<div id="kc-form-wrapper"
                 <#if realm.password && social.providers??>class="${properties.kcFormSocialAccountContentClass!} ${properties.kcFormSocialAccountClass!}"</#if>>
                <#if realm.password>
					<form id="kc-form-login"
						  onsubmit="login.disabled = true; return true;"
						  action="${url.loginAction}"
						  method="post">


						<div class='inputfield'>

                            <#if usernameEditDisabled??>
								<input tabindex="1"
									   id="username"
									   class="notice"
									   name="username"
									   value="${(login.username!'')}"
									   type="text"
									   disabled/>
                            <#else>
								<input tabindex="1"
									   id="username"
									   class="notice"
									   name="username"
									   value="${(login.username!'')}"
									   type="text"
									   autofocus
									   autocomplete="off"/>
                            </#if>

							<label for="username" class='floating-label'>
                                <#if !realm.loginWithEmailAllowed>${msg("username")}
                                <#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}
                                <#else>${msg("email")}
                                </#if>
							</label>
						</div>

						<div class='inputfield'>
							<input tabindex="1" type="password" id="password" name="password" autocomplete="off"
								   class='notice ' placeholder='${msg("password")}'>
							<label for="password" class='floating-label'>${msg("password")}</label>
						</div>


						<div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!}">
							<div id="kc-form-options">
                                <#if realm.rememberMe && !usernameEditDisabled??>
									<div class="checkbox">
										<label>
                                            <#if login.rememberMe??>
												<input tabindex="0"
													   id="rememberMe"
													   name="rememberMe"
													   type="checkbox"
													   checked> ${msg("rememberMe")}
                                            <#else>
												<input tabindex="0"
													   id="rememberMe"
													   name="rememberMe"
													   type="checkbox"> ${msg("rememberMe")}
                                            </#if>
										</label>
									</div>
                                </#if>
							</div>
							<div class="${properties.kcFormOptionsWrapperClass!}">
                                <#if realm.resetPasswordAllowed>
									<span><a tabindex="0"
											 href="${url.loginResetCredentialsUrl}">${msg("doForgotPassword")}</a></span>
                                </#if>
							</div>

							<div style="flex-basis: 100%; height: 10px; margin: 0; border: 0;"></div>
							<div class="${properties.kcFormOptionsWrapperClass!}" style="margin-left: auto">
                                <#if realm.name == 'vacme' >
									<span><a tabindex="0"
											 href="/benutzername-vergessen">${msg("doForgotBenutzernamen")}</a></span>
                                </#if>
							</div>
							<div style="flex-basis: 100%; height: 10px; margin: 0; border: 0;"></div>
							<div class="${properties.kcFormOptionsWrapperClass!}" style="margin-left: auto">
                                <#if realm.name == 'vacme' >
									<span><a tabindex="0" target="_blank"
											 href="${msg('forgotZugangsdatenVideoUrl')}">
											${msg("howToLinkForgotZugangsdaten")}
										</a></span>
                                </#if>
							</div>

						</div>

						<div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">
							<input type="hidden" id="id-hidden-input" name="credentialId"
                                   <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>
							<button tabindex="2" class='go-next' type="submit" name="login" id="kc-login">
								<img src="${url.resourcesPath}/img/right-arrow-white.svg"
									 style="margin-right: 10px; height: 19px; width: auto; vertical-align: middle; margin-bottom: 1px;">
								<span>${msg("doLogIn")}</span>
								<img src="${url.resourcesPath}/img/right-arrow-white.svg"
									 style="margin-left: 17px;height:19px;width:auto;vertical-align:middle;margin-bottom: 1px;">
							</button>
						</div>
					</form>
                </#if>
			</div>
            <#if realm.password && social.providers??>
				<div id="kc-social-providers"
					 class="${properties.kcFormSocialAccountContentClass!} ${properties.kcFormSocialAccountClass!}">
					<ul class="${properties.kcFormSocialAccountListClass!} <#if social.providers?size gt 4>${properties.kcFormSocialAccountDoubleListClass!}</#if>">
                        <#list social.providers as p>
                        <#-- if the social provider is hin tell the template to display the hin-button, otherwise use default-->
							<#if p.alias == 'hin-idp'>
								<li>
									<div class="hin-col">
										<p>
											<a href="${p.loginUrl}"
											   tabindex="3"
											   class="btn hin-social hin-btn hin-login-with-hin">
												<i class="hin-icon">
													<img src="${url.resourcesPath}/img/hin_logo.png"></i>
												<span class="login">${p.displayName}</span>
											</a>
										</p>
									</div>
								</li>
                            <#else>
								<li class="${properties.kcFormSocialAccountListLinkClass!}">
									<a href="${p.loginUrl}"
									   id="zocial-${p.alias}"
									   tabindex="3"
									   class="zocial ${p.providerId}">
										<span>${p.displayName}</span>
									</a>
								</li>
                            </#if>
                        </#list>
					</ul>
				</div>
            </#if>
		</div>
    <#elseif section = "info" >
        <#if realm.password && realm.registrationAllowed && !registrationDisabled??>
			<div id="kc-registration">
				<span>${msg("noAccount")} <a tabindex="0" href="${url.registrationUrl}">${msg("doRegister")}</a></span>
			</div>
        </#if>
    </#if>

</@layout.registrationLayout>
