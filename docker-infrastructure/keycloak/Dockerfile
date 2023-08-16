FROM quay.io/keycloak/keycloak:17.0.0-legacy


ARG KEYCLOAK_EXTENSIONS_VERSION=2.0.11

USER root

#keycloak12 does not allow us to copy to /opt/jboss

COPY copy/keycloak /opt/jboss/keycloak
ADD https://nexus.dvbern.ch/nexus/content/repositories/dvb/ch/dvbern/lib/keycloak/keycloak-extensions-ear/${KEYCLOAK_EXTENSIONS_VERSION}/keycloak-extensions-ear-${KEYCLOAK_EXTENSIONS_VERSION}.ear \
      "/opt/jboss/keycloak/standalone/deployments/keycloak-extenions-${KEYCLOAK_EXTENSIONS_VERSION}.ear"

RUN chown -R 1000 /opt/jboss/keycloak

# COPY copy/startup-scripts /opt/jboss/startup-scripts
# RUN chown -R 1000 /opt/jboss/startup-scripts

COPY copy/vacme-realms.json /tmp/realm-import/vacme-realms.json
RUN chown -R 1000 /tmp/realm-import/

COPY copy/wildfly-config/* /opt/jboss/keycloak/standalone/configuration/
RUN chown -R 1000 /opt/jboss/keycloak/standalone/configuration/*

USER 1000

ENV KEYCLOAK_WELCOME_THEME='vacme'

CMD ["-b", "0.0.0.0", "-Dkeycloak.migration.action=import", "-Dkeycloak.migration.provider=singleFile", "-Dkeycloak.migration.file=/tmp/realm-import/vacme-realms.json", "-Dkeycloak.migration.strategy=IGNORE_EXISTING"]