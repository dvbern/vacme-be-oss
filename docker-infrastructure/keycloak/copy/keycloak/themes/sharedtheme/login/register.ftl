<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "scriptsforreg">
		<script src="${url.resourcesPath}/js/libphonenumberjs/libphonenumber-max.js" type="text/javascript"></script>
    <#elseif section = "header">
        ${msg("doRegister")}
    <#elseif section = "form">
		<form id="kc-register-form" class="${properties.kcFormClass!}" action="${url.registrationAction}" method="post">

			<div class='inputfield'>
				<input id="email" name="email" value="${(register.formData.email!'')}" type="text"
					   class='notice ${messagesPerField.printIfExists('email',properties.kcFormGroupErrorClass!)}' placeholder='${msg("email")}'
					   autofocus>
				<label for="email" class='floating-label'>${msg("email")}</label>
				<span class="invisible errormessage" id="email_error">${msg("tooLong")}</span>
			</div>

            <#if !realm.registrationEmailAsUsername>
				<div id="usernameInputField" class='inputfield'>
					<input id="username" name="username" value="${(register.formData.username!'')}" type="text"
						   class='notice ${messagesPerField.printIfExists('username',properties.kcFormGroupErrorClass!)}'
						   placeholder='${msg("usernameOrEmail")}'>
					<label for="username" class='floating-label'>${msg("usernameOrEmail")}</label>
					<span class="invisible errormessage" id="username_error">${msg("tooLong")}</span>
				</div>
				<div>
					<span class="hint">${msg("otherPersonExplain")}</span>
				</div>
            </#if>

			<div class='inputfield'>
				<input id="telnummer" name="user.attributes.MobileNummer" type="text"
					   class='notice ${messagesPerField.printIfExists('telnummer',properties.kcFormGroupErrorClass!)}'
					   placeholder='${msg("telnummer")}'
					   value="${(register.formData['user.attributes.MobileNummer']!'')}">
				<label for="telnummer" class='floating-label'>${msg("telnummer")}</label>
				<div>
					<span id="telnummerExplainText" class="hint">${msg("telnummerExplain")?no_esc}</span>
				</div>
				<div>
					<span class="invisible errormessage" id="telnummer_notValid">${msg("mobileNotValid")}</span>
				</div>
				<div>
					<span class="invisible errormessage" id="telnummer_mobileNrWrongType">${msg("mobileNrWrongType")}</span>
				</div>
			</div>

			<div class='inputfield'>
				<input id="lastName" name="lastName" value="${(register.formData.lastName!'')}" type="text"
					   class='notice ${messagesPerField.printIfExists('lastName',properties.kcFormGroupErrorClass!)}'
					   placeholder='${msg("lastName")}' autocomplete="family-name">
				<label for="lastName" class='floating-label'>${msg("lastName")}</label>
				<div>
					<span id="lastNameExplainText" class="hint">${msg("lastNameExplain")?no_esc}</span>
				</div>
				<span class="invisible errormessage" id="lastName_error">${msg("tooLong")}</span>
			</div>


			<div class='inputfield'>
				<input id="firstName" name="firstName" value="${(register.formData.firstName!'')}" type="text"
					   class='notice  ${messagesPerField.printIfExists('firstName',properties.kcFormGroupErrorClass!)}' placeholder='${msg("firstName")}'>
				<label for="firstName" class='floating-label'>${msg("firstName")}</label>
				<div>
					<span id="firstNameExplainText" class="hint">${msg("firstNameExplain")?no_esc}</span>
				</div>
				<span class="invisible errormessage" id="firstName_error">${msg("tooLong")}</span>
			</div>

            <#if passwordRequired>
				<div class='inputfield'>
					<input type="password" id="password" name="password" autocomplete="new-password"
						   class='notice ${messagesPerField.printIfExists('password',properties.kcFormGroupErrorClass!)}' placeholder='${msg("password")}'>
					<label for="password" class='floating-label'>${msg("password")}</label>
					<div>
						<span id="passwordExplainText" class="hint">${msg("passwordExplain")?no_esc}</span>
					</div>
					<span class="invisible errormessage" id="password_error">${msg("tooLong")}</span>
				</div>
				<div class='inputfield'>
					<input type="password" id="password-confirm" name="password-confirm"
						   class='notice  ${messagesPerField.printIfExists('password-confirm',properties.kcFormGroupErrorClass!)}'
						   placeholder='${msg("passwordConfirm")}'>
					<label for="password-confirm" class='floating-label'>${msg("passwordConfirm")}</label>
					<span class="invisible errormessage" id="password-match_error">${msg("notMatchPasswordMessage")}</span>
				</div>

            </#if>

            <#if recaptchaRequired??>
				<div class="form-group">
					<div class="${properties.kcInputWrapperClass!}">
						<div class="g-recaptcha" data-size="compact" data-sitekey="${recaptchaSiteKey}"></div>
					</div>
				</div>
            </#if>

			<div class="${properties.kcFormGroupClass!}">


				<div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
					<button id="login-btn" class='go-next' type="button">
						<img src="${url.resourcesPath}/img/right-arrow-white.svg"
							 style="margin-right: 10px;height:19px;width:auto;vertical-align:middle;margin-bottom: 1px;">
						<span>
                            ${msg("doRegister")}
                        </span>
						<img src="${url.resourcesPath}/img/right-arrow-white.svg"
							 style="margin-left: 17px;height:19px;width:auto;vertical-align:middle;margin-bottom: 1px;">
					</button>
				</div>

				<div id="invisible" class="invisible">
					<button id="submit" type="submit">
					</button>
				</div>

				<div id="kc-return" class="${properties.kcFormButtonsClass!}">
					<a href="${url.loginUrl}">${msg("backToLogin")?no_esc}</a>
				</div>

			</div>
		</form>
    </#if>
</@layout.registrationLayout>
