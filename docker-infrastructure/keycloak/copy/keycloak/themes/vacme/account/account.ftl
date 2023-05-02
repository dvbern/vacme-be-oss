<#import "template.ftl" as layout>
<@layout.mainLayout active='account' bodyClass='user'; section>

	<div class="row">
		<div class="col-md-12">
			<h1 id="kc-page-title">${msg("editAccountHtmlTitle")}</h1>
		</div>
	</div>

	<form action="${url.accountUrl}" class="form-horizontal" method="post">

		<input type="hidden" id="stateChecker" name="stateChecker" value="${stateChecker}">

        <#if !realm.registrationEmailAsUsername>
			<div class="form-group ${messagesPerField.printIfExists('username','has-error')}">
				<div class='inputfield col-sm-12 col-md-12'>
					<input id="username" name="username" value="${(account.username!'')}" type="text"
						   class='notice ${messagesPerField.printIfExists('username',properties.kcFormGroupErrorClass!)}'
						   placeholder='${msg("username")}'
                           <#if !realm.editUsernameAllowed>disabled="disabled"</#if>>
					<label for="username" class='floating-label'>${msg("username")}</label>
				</div>
			</div>
        </#if>

		<div class="form-group ${messagesPerField.printIfExists('email','has-error')}">
			<div class='inputfield col-sm-12 col-md-12'>
				<input id="email" name="email" value="${(account.email!'')}" type="text"
					   class='notice ${messagesPerField.printIfExists('email',properties.kcFormGroupErrorClass!)}'
					   placeholder='${msg("email")}'>
				<label for="email" class='floating-label'>${msg("email")}</label>
			</div>
		</div>

		<div class="form-group ${messagesPerField.printIfExists('telnummer','has-error')}">
			<div class='inputfield col-sm-12 col-md-12'>
                <#--to change this number the Required Action 'sms_update_mobile_number' must be triggered -->
				<input id="telnummer" name="user.attributes.MobileNummer" type="text" disabled="disabled"
					   class='notice ${messagesPerField.printIfExists('telnummer',properties.kcFormGroupErrorClass!)}'
					   placeholder='${msg("telnummer")}'
					   value="${(account.attributes.MobileNummer!'')}"
					   required>
				<label for="telnummer" class='floating-label'>${msg("telnummer")}</label>
			</div>
		</div>

		<div class="form-group ${messagesPerField.printIfExists('firstName','has-error')}">
			<div class='inputfield col-sm-12 col-md-12'>
				<input id="firstName" name="firstName" value="${(account.firstName!'')}" type="text"
					   class='notice ${messagesPerField.printIfExists('firstName',properties.kcFormGroupErrorClass!)}'
					   placeholder='${msg("firstName")}'>
				<label for="firstName" class='floating-label'>${msg("firstName")}</label>
			</div>
		</div>

		<div class="form-group ${messagesPerField.printIfExists('lastName','has-error')}">

			<div class='inputfield col-sm-12 col-md-12'>
				<input id="lastName" name="lastName" value="${(account.lastName!'')}" type="text"
					   class='notice ${messagesPerField.printIfExists('lastName',properties.kcFormGroupErrorClass!)}'
					   placeholder='${msg("lastName")}'>
				<label for="lastName" class='floating-label'>${msg("lastName")}</label>
			</div>
		</div>

		<div class="form-group">
			<div id="kc-form-buttons" class="col-sm-12 col-md-12">

				<!-- Zurueck zu VacMe / abbrechen -->
                <#if referrer?has_content>
					<a href="${referrer.url}" class='go-back'>
						<img src="${url.resourcesPath}/img/go-back.svg"
							 style="margin-right:13px;height:19px;width:auto;vertical-align:middle;margin-bottom: 1px;">
						<span>${msg("doCancel")}</span>
						<img
								src="${url.resourcesPath}/img/go-back.svg"
								style="margin-left:9px;height:19px;width:auto;vertical-align:middle;margin-bottom: 1px;">
					</a>
                </#if>

				<span style="flex-grow: 1;">&nbsp;</span>

				<!-- Speichern -->
				<button class='go-next' type="submit" name="submitAction" value="Save">
					<img src="${url.resourcesPath}/img/right-arrow-white.svg"
						 style="margin-right: 10px; height: 19px; width: auto; vertical-align: middle; margin-bottom: 1px;">
					<span>
                            ${msg("doSave")}
                        </span>
					<img src="${url.resourcesPath}/img/right-arrow-white.svg"
						 style="margin-left: 17px;height:19px;width:auto;vertical-align:middle;margin-bottom: 1px;">
				</button>


			</div>
		</div>
	</form>

</@layout.mainLayout>
