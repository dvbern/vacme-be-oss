export class Settings {

    public static userApplikationsSupport(): string {
        const users = Cypress.env('users');
        const usr = users['APP_SUPPORT'];
        return usr;
    }

    public static userFachverantwortung(): string {
        const users = Cypress.env('users');
        const usr = users['FACHVERANTWORTUNG'];
        return usr;
    }

    public static passwords(usernname: string): string {
        const passwords = Cypress.env('passwords');
        const pwd = passwords[usernname];
        return pwd;
    }
}
