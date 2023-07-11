# Keycloak Updates

Wenn man den Keycloak updaten muss ist es einfacher das vacme-initial.cli Skript laufen zu lassen als 
manuell alles ins standalone.xml zu übernehmen.

Daher wird folgendes Vorgehen vorgeschlagen

## Schritt1 
- Backup der aktuellen Keycloak Datenbank

```
mysqldump   -ukeycloak -pchangeme -P 3304 keycloak --hex-blob --result-file="C:\\Users\\myuser\\keycloak-11.02.2022-dump.sql"
```

## Schritt 2
- Version in Dockerfile anpassen zum Beispiel auf Keycloak 16
- In Dockerfile die Zeilen einkommentieren welche den Ordner mit dem cli Skript bereitstellen
```
  COPY copy/startup-scripts /opt/jboss/startup-scripts
  RUN chown -R 1000 /opt/jboss/startup-scripts
  
 ```
- In Dockerfile die Zeilen auskommentieren welche das fixe standalone-ha.xml rüberkopieren
```
#COPY copy/wildfly-config/* /opt/jboss/keycloak/standalone/configuration/
#RUN #chown -R 1000 /opt/jboss/keycloak/standalone/configuration/*
```


## Schritt 3
- Den Keylcoak einmal neu Builden

```
docker-compose down      
docker-compose up --build
```
## Schritt 4
Aus dem laufenden Keycloak das standalone-ha.xml rauskopieren

```
docker exec -i -t  vacme-keycloak bash
$ cd /opt/jboss/keycloak/standalone/configuration
$ cat stadalone-ha.xml

# Inhalt kopieren nach 
./keycloak/wildfly-config/standalone-ha.xml
```

## Schritt 5 
Zum vergleich aus einem unmodifizierten Keylcoak ebenfalls das standalone-ha.xml rauskopieren
```
docker run -e KEYCLOAK_USER=keycloak -e KEYCLOAK_PASSWORD=secret -p 8020:8080 jboss/keycloak
docker ps
docker-exec -i -t <containername> bash
$ cd /opt/jboss/keycloak/standalone/configuration
$ cat stadalone-ha.xml
# Inhalt kopieren nach 
./keycloak/wildfly-config/standalone-ha.orig.xml

```

## Schritt 6
Die beiden standalone-ha.orig.xml und  standalone-ha.xml vergleichen und prüfen ob alles korrekt ist
Wenn ja das cli Skript aus Schritt 2 wieder auskommentieren und das kopieren des standalone.xml aus 
Schritt 3 wieder einkommentieren