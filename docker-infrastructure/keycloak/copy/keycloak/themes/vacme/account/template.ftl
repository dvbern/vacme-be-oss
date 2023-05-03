<#macro mainLayout active bodyClass>
	<!doctype html>
	<html>
	<head>
		<meta charset="utf-8">
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<meta name="robots" content="noindex, nofollow">
		<meta name="viewport" content="width=device-width, initial-scale=1">

		<title>${msg("accountManagementTitle")}</title>
		<link rel="icon" href="${url.resourcesPath}/img/favicon.ico">
        <#if properties.stylesCommon?has_content>
            <#list properties.stylesCommon?split(' ') as style>
				<link href="${url.resourcesCommonPath}/${style}" rel="stylesheet"/>
            </#list>
        </#if>
        <#if properties.styles?has_content>
            <#list properties.styles?split(' ') as style>
				<link href="${url.resourcesPath}/${style}" rel="stylesheet"/>
            </#list>
        </#if>
        <#if properties.scripts?has_content>
            <#list properties.scripts?split(' ') as script>
				<script type="text/javascript" src="${url.resourcesPath}/${script}"></script>
            </#list>
        </#if>
	</head>
	<body class="admin-console user ${bodyClass}">

	<div class="vacme-account">
		<div id="vacme-header">
			<div id="vacme-header-container">
				<div id="vacme-branding">
					<a href="/" style="display: block">
						<img alt="logo" src="${url.resourcesPath}/img/Kanton-Bern.svg">
						<div>${kcSanitize(realm.displayNameHtml)?no_esc}</div>
					</a>
				</div>
				<#if realm.internationalizationEnabled  && locale.supported?size gt 1>
					<div id="kc-locale">
						<div class="kc-signOut">
							<a href="${url.logoutUrl}">${msg("doSignOut")}</a>
						</div>
						<div id="kc-locale-wrapper" class="${properties.kcLocaleWrapperClass!}">
							<div>
								<#assign de = locale.supported[0]>
								<!--a href="#" id="kc-current-locale-link">${locale.current}${(de == locale.current)?then('kc-locale-item-active','')}</-->
								<a class="kc-locale-item ${(de.languageTag == locale.currentLanguageTag)?then('kc-locale-item-active','')}"
								   href="${de.url}">${de.languageTag}</a>
								<span class="kc-locale-divider"> | </span>
								<#assign fr = locale.supported[1]>
								<a class="kc-locale-item ${(fr.languageTag == locale.currentLanguageTag)?then('kc-locale-item-active','')}"
								   href="${fr.url}">${fr.languageTag}</a>
                                <#if locale.supported?size gt 2>
									<span class="kc-locale-divider"> | </span>
                                    <#assign en = locale.supported[2]>
									<a class="kc-locale-item ${(en.languageTag == locale.currentLanguageTag)?then('kc-locale-item-active','')}"
									   href="${en.url}">${en.languageTag}</a>
                                </#if>
							</div>
						</div>
					</div>
				</#if>
			</div>
		</div>

		<div class="container">
			<div class="bs-sidebar col-sm-3">
				<ul>
					<li class="<#if active=='account'>active</#if>"><a href="${url.accountUrl}">${msg("account")}</a></li>
                    <#if features.passwordUpdateSupported>
					<li class="<#if active=='password'>active</#if>"><a href="${url.passwordUrl}">${msg("password")}</a></li></#if>
					<li class="<#if active=='totp'>active</#if>"><a href="${url.totpUrl}">${msg("authenticator")}</a></li>
                    <#if features.identityFederation>
					<li class="<#if active=='social'>active</#if>"><a href="${url.socialUrl}">${msg("federatedIdentity")}</a></li></#if>
					<li class="<#if active=='sessions'>active</#if>"><a href="${url.sessionsUrl}">${msg("sessions")}</a></li>
					<li class="<#if active=='applications'>active</#if>"><a href="${url.applicationsUrl}">${msg("applications")}</a></li>
                    <#if features.log>
					<li class="<#if active=='log'>active</#if>"><a href="${url.logUrl}">${msg("log")}</a></li></#if>
                    <#if realm.userManagedAccessAllowed && features.authorization>
					<li class="<#if active=='authorization'>active</#if>"><a href="${url.resourceUrl}">${msg("myResources")}</a></li></#if>
				</ul>
			</div>

			<div class="col-sm-9 content-area">
                <#if message?has_content>
					<div class="alert alert-${message.type}">
                        <#if message.type=='success' >
							<span class="pficon pficon-ok"></span>
							<!-- Beim erfolgreichen Speichern gehen wir direkt zurueck zu VacMe -->
							<#if referrer?has_content>
								<script type="application/javascript">
									setTimeout(() => {
										window.location = "${referrer.url}"
									}, 1000);
								</script>
							</#if>
                        </#if>
                        <#if message.type=='error' ><span class="pficon pficon-error-circle-o"></span></#if>
						<span class="kc-feedback-text">${kcSanitize(message.summary)?no_esc}</span>
					</div>
                </#if>

                <#nested "content">
			</div>
		</div>
	</div>
	</body>
	</html>
</#macro>
