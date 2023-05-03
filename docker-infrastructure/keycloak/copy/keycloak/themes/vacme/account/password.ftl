<#import "template.ftl" as layout>
<@layout.mainLayout active='password' bodyClass='password'; section>

	<div class="row">
		<div class="col-md-12">
			<h1 id="kc-page-title">${msg("changePasswordHtmlTitle")}</h1>
		</div>
	</div>

	<form action="${url.passwordUrl}" class="form-horizontal" method="post">
		<input type="text" id="username" name="username" value="${(account.username!'')}" autocomplete="username" readonly="readonly" style="display:none;">

        <#if password.passwordSet>



			<div class='inputfield col-sm-12 col-md-12'>
				<input id="password" name="password" type="password"
					   class='notice' placeholder='${msg("password")}'
					   autofocus autocomplete="current-password">
				<label for="password" class='floating-label'>${msg("password")}</label>
			</div>

        </#if>

		<input type="hidden" id="stateChecker" name="stateChecker" value="${stateChecker}">


		<div class='inputfield col-sm-12 col-md-12'>
			<input id="password-new" name="password-new" type="password"
				   class='notice' placeholder='${msg("passwordNew")}'
				   autofocus autocomplete="password-new">
			<label for="password-new" class='floating-label'>${msg("passwordNew")}</label>
		</div>

		<div class='inputfield col-sm-12 col-md-12'>
			<input id="password-confirm" name="password-confirm" type="password"
				   class='notice' placeholder='${msg("passwordConfirm")}'
				   autofocus autocomplete="password-confirm">
			<label for="password-confirm" class='floating-label'>${msg("passwordConfirm")}</label>
		</div>

		<div id="kc-form-buttons" class="col-sm-12 col-md-12" style="margin-top: 30px;">

			<!-- Zurueck zu VacMe / Abbrechen -->
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

	</form>

</@layout.mainLayout>
