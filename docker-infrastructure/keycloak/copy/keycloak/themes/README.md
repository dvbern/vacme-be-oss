# Theme development with docker

Run the Keycloak Image with a bind mount on the theme folder 
```shell
cd docker-infrastucture/

docker run -it --mount type=bind,source=${pwd}/keycloak/copy/opt/jboss/keycloak/themes/vacme,target=/opt/jboss/keycloak/themes/vacme -p 8180:8080 -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=pw registry.dvbern.ch/vacme/keycloak
```


Oder wie im vacme/docker-infrastructure/README.md beschrieben: Die Keycloak-Theme-Ordner mithilfe von docker-compose.override.yml einbinden.

or if you are using docker-compose the format should be along these lines
```shell
volumes:
- type: bind
  source: ./keycloak/copy/keycloak/themes/vacme
  target: /opt/jboss/keycloak/themes/vacme
- type: bind
  source: ./keycloak/copy/keycloak/themes/vacme-zh
  target: /opt/jboss/keycloak/themes/vacme-zh
- type: bind
  source: ./keycloak/copy/keycloak/themes/theme-development.cli
  target: /opt/jboss/startup-scripts/theme-development.cli
```


Hierarchie der Themes (siehe parent in den theme.properties):

base 
    < keycloak
        < sharedtheme
            < vacme (VacMe Bern)
            < vacme-zh (VacMe ZH)
            < mik