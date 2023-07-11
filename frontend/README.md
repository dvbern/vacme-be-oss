*********************************************
# Übersetzungen mit ngx-translate
*********************************************


# im HTML:

##	Pipe:
    <div>{{ 'HELLO' | translate:{value: 'VacMe'} }}</div>
    <div>{{ 'IMPFUNGEN' | translate }}</div>
	
##	Direktive:
    <div translate="IMPFUNGEN"></div>
    <div translate>IMPFUNGEN</div>
	
	<div [translate]="'HELLO'" [translateParams]="{value: 'world'}"></div>
    <div translate [translateParams]="{value: 'VacMe'}">HELLO</div>

## uppercase sicherstellen:
    <p>{{ 'ROLES.' + role | uppercase | translate }}</p>

## mit HTML-Tags innerhalb der Übersetzung:
	<div [innerHTML]="'FANCYTEXT' | translate"></div>



# in Typescript (TranslateService):
    translate.get('IMPFUNGEN').subscribe((res: string) => {
        console.log(res);
    });
    translate.get('HELLO', {value: 'world'}).subscribe((res: string) => {
        console.log(res);
    });



#de.json / fr.json:
    {
        "IMPFUNGEN": "Impfungen",
        "HELLO": "Hallo {{value}}",
        "ROLES": {
            "ADMIN": "Administrator"
        },	
        "FANCYTEXT": "Welcome to my Angular application!<br><strong>This is an amazing app which uses the latest technologies!</strong>"
    }
    



# Frontend

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 11.0.3.

## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `--prod` flag for a production build.

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via [Protractor](http://www.protractortest.org/).

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI Overview and Command Reference](https://angular.io/cli) page.
