<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "title">
        ${msg("updatePhoneNumberTitle", realm.name)}
    <#elseif section = "header">
        ${msg("updatePhoneNumberTitle", realm.name)}
    <#elseif section = "form">
    <#--
		Hack-alert: Keycloak doesn't provide per-field error messages here,
		so we check global message for need to display validation error styling
	-->
        <#if message?has_content && message.type = "error">
            <#assign errorClass = "govuk-form-group--error" >
        </#if>

    <#--        form to enter the desired new mobile number-->
        <#if showMobilePhoneInput = true>
            <div class="govuk-grid-row">
                <div class="govuk-grid-column-full">
                    <p class="sms-instruction">${msg("updatePhoneNumberMessage")}</p>
                </div>
                <form id="kc-mobile-number-updt-form"
                      action="${url.loginAction}" method="post">
                    <div class="govuk-form-group ${errorClass!""}">
                        <label for="mobileNumber" class="govuk-label">${msg("phoneNumber")}</label>
                        <input type="tel"
                               id="mobileNumber"
                               class="govuk-input"
                               name="mobile_number"
                               value="${(phoneNumber!'')}"
                               autocomplete="mobile tel"
                               aria-describedby="mobileNumber-hint" />
                    </div>

                    <div id="kc-logins" class="govuk-grid-column-full">
                        <button class='go-next' type="submit">
                            <img src="${url.resourcesPath}/img/right-arrow-white.svg" style="margin-right: 10px; height: 19px; width: auto; vertical-align: middle; margin-bottom: 1px;">
                            <span>${msg("doSubmit")}</span>
                            <img src="${url.resourcesPath}/img/right-arrow-white.svg"
                                 style="margin-left: 17px;height:19px;width:auto;vertical-align:middle;margin-bottom: 1px;">
                        </button>
                    </div>
                </form>
            </div>
        </#if>

    <#--        form to enter the security code to verify the  new mobile number -->
        <#if showMobilePhoneInput = false>
            <div class="govuk-grid-row">
                <div class="govuk-grid-column-full">
                    <p>${msg("sms-auth.instruction")}</p>
                </div>
                <form id="kc-totp-login-form" action="${url.loginAction}" method="post">

                    <div class="form-group">
                        <div class='inputfield govuk-grid-column-full ${errorClass!""}'>
                            <input id="mobileNumberChangeConfirmCode"
                                   name="mobile_number_change_code"
                                   type="number"
                                   class='notice'
                                   placeholder='${msg("sms-auth.code")}'
                                   autocomplete="false"
                                   autofocus>
                            <label for="mobileNumberChangeConfirmCode" class='floating-label floating-space-left'>${msg("sms-auth.code")}</label>
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
                </form>
            </div>
        </#if>
        <div>
            <form id="kc-mobile-number-updt-cancel-form" class="${properties.kcFormClass!} govuk-grid-column-full"
                  action="${url.loginAction}" method="post">
                <div class="govuk-form-group">
                    <input type="hidden" name="mobile_number_update_canceled" value="cancel process">
                    <div id="kc-return" class="govuk-grid-column-full">
                        <button class='go-back col-md-4' type="submit" name="goBack" value="goBack">
                            <img src="${url.resourcesPath}/img/go-back.svg"
                                 style="margin-right:13px;height:19px;width:auto;vertical-align:middle;margin-bottom: 1px;">
                            <span>${msg("backToApplication")}</span>
                        </button>
                    </div>
                </div>

            </form>
        </div>

    </#if>
</@layout.registrationLayout>
