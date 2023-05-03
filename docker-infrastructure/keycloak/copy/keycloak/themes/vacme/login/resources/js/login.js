/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

document.addEventListener('DOMContentLoaded', () => {
    // check if on registration form
    const registration = document.getElementById('kc-register-form');
    if (registration) {
        document.getElementById('email').oninput = updateUsername;
        document.getElementById('username').onblur = onUsernameChange;
        document.getElementById('login-btn').onclick = checkForm;
    }
});

let usernameModified = false;

function updateUsername() {
    const email = document.getElementById("email").value;

    if (!usernameModified) {
        const usernameInput = document.getElementById("username");
        usernameInput.value = email;
    }
}

function onUsernameChange() {
    const email = document.getElementById("email").value;
    const username = document.getElementById("username").value;

    if (email === username) {
        usernameModified = false;
        return;
    }

    usernameModified = true;
}

function checkForm() {

    var success = 0;
    success += checkLenght("email");
    success += checkLenght("username");
    success += checkLenght("lastName");
    success += checkLenght("firstName");
    success += checkLenght("password");
    success += checkMobile("telnummer");
    success += checkPasswordMatch();

    if (success === 0) {
        document.getElementById("submit").click();
    }
}

function checkLenght(elementId) {
    const maxLenght = 50;
    const elementById = document.getElementById(elementId);
    const value = elementById.value;
    if (value.length > maxLenght) {
        elementById.classList.add('has-error')
        document.getElementById(elementId + "_error").classList.remove('invisible');
        return 1;
    } else {
        elementById.classList.remove('has-error')
        document.getElementById(elementId + "_error").classList.add('invisible');
    }
    return 0;
}

function checkPasswordMatch() {
    const password = document.getElementById('password');
    const passwordConfirm = document.getElementById('password-confirm');
    const passwordMatchError = document.getElementById('password-match_error');

    if (password.value === passwordConfirm.value) {
        passwordMatchError.classList.add('invisible');
        return 0;
    }

    passwordMatchError.classList.remove('invisible');
    return 1;
}

function checkMobile(elementId) {
    const elementById = document.getElementById(elementId);
    const REGEX_TELEFON = '^[0-9\ \+]*$';
    removeErrors(elementId);

    const value = elementById.value;
    const parsedNumber = libphonenumber.parsePhoneNumberFromString(value, 'CH');
    const allowedNumberTypes = ['MOBILE', 'FIXED_LINE_OR_MOBILE', 'PAGER']
    let parsedNumValid = parsedNumber ? parsedNumber.isValid() : false;

    if (!value.match(REGEX_TELEFON)) {
        elementById.classList.add('has-error')
        document.getElementById(elementId + "_notValid").classList.remove('invisible');
        return 1;
    } else if (!parsedNumValid) {
        elementById.classList.add('has-error')
        document.getElementById(elementId + "_notValid").classList.remove('invisible');
        return 1;
    } else if (!allowedNumberTypes.includes(parsedNumber.getType())) {
        elementById.classList.add('has-error')
        document.getElementById(elementId + "_mobileNrWrongType").classList.remove('invisible');
        return 1;
    }
    else {
        removeErrors(elementById);
    }
    return 0;
}

function removeErrors(elementId) {
    const elementById = document.getElementById(elementId);
    if(elementById) {
        elementById.classList.remove('has-error')
        document.getElementById(elementId + "_notValid").classList.add('invisible');
        document.getElementById(elementId + "_mobileNrWrongType").classList.add('invisible');
    }
}
