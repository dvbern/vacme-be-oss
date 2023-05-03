# Theme development with docker

Run the Keycloak Image with a bind mount on the theme folder 
```shell
cd docker-infrastucture/

docker run -it --mount type=bind,source=${pwd}/keycloak/copy/opt/jboss/keycloak/themes/vacme,target=/opt/jboss/keycloak/themes/vacme -p 8180:8080 -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=pw registry.dvbern.ch/vacme/keycloak
```
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