# Entwicklungsumgebung starten
## Config/Credentials

### Quarkus
Lokale Config und Credentials werden im Env-File konfiguriert: [server/vacme-rest/.env] (siehe: [Quarkus Doku](https://quarkus.io/guides/config#overriding-properties-at-runtime))
Eine Vorlage für Copy&Paste findet sich in [server/vacme-rest/.env-template]


### Infrastruktur
Datenbanken und andere Infrastruktur wurden im [docker-infrastructure] Ordner dockerisiert abgelegt

Um zum Beispiel die Datenbank der Applikation zu starten fuehrt man im Ordner [docker-infrastructure]  befehlt aus
Um Ports oder Volumes Lokal zu definieren empfiehlt es sich ein docker-compose.override.yml anzulegen.
Dazu die Vorlage docker-compose.override-template.yml kopieren und nach Wunsch mappings etc einfuegen

Im docker-compose.yml sind einige Secrets definiert die gebraucht werden. Damit diese gefunden werden muessen die
konfigurieren Passwortfiles existieren.
Ein Beispielverzeichnis mit vordefinierten Passwortfiles die dann Kopiert und angepasst werden muessen findet sich
unter [developer_local_settings_template]. Siehe auch das Readme des Infrastrukturprojektes 

## Entwicklungsumgebung starten

Voraussetzungen sind
 
- Java 11, am besten von AdoptOpenJDK (IntelliJ: Project Structure - Project SDK)
- node (allenfalls per nvm) und installierte angular cli
    ```shell script
    ng --version
    ```
    Wenn das einen Fehler gibt:
    ```shell script
    npm install -g @angular/cli@latest
    ```


- Files kopieren:
	- server/vacme-rest/.env-template kopieren
	- docker-infrastructure/docker-compose.override-template.yml kopieren
	- docker-inrfastructure/developer_local_settings_template kopieren
 
## Docker

### RUN
```shell script
cd docker-infrastructure
docker-compose up --build db
```
### beim ersten Mal:

Docker [readme](docker-infrastructure/README.md) lesen!

**Volume erstellen.**
```shell script
cd docker-infrastructure
docker volume create --name=vacme-database-mariadb-data
```

Optional: **DB** in IntelliJ einbinden: MariaDB, vacme / secretPassword


##  Backend
### INSTALL
```shell script
mvn  -DskipTests=true clean install
```

### RUN
```shell script
cd server/vacme-rest
mvn clean compile quarkus:dev
```

--> http://localhost:8080/

##  Frontend
### INSTALL
```shell script
cd frontend
npm install
```

### RUN InitialReg
Started die Registrierungsapplikation
```shell script
cd frontend
npm run start 
npm run startreg:zh 
```

--> http://localhost:4200/

### RUN Web
Startet die Fachapplikation
```shell script
cd frontend
npm run startweb
npm run startweb:zh
```

--> http://localhost:4222/


# Generate Typescript models
```shell script
cd frontend
npm run openapi
```

# Release bauen
Zum Releasen kommt git-flow zum Einsatz.
Die Releases werden per Jenkins Job gebaut. Es gibt eine Jenkins Pipeline die die benötigten Schritte ausführt 


# Settings

- Java:
`@ConfigProperty(name= "x.y.z", defaultValue = "false")`

- application.properties (optional, die @ConfigProperty kann auch direkt aus dem .env lesen):
`x.y.z=${Y_Y_Z}`

- .env:
`X_Y_Z`

- DB ApplicationProperty: für Settings, die schnell ändern können, ohne Neustart

Cron expressions für @scheduled werden NICHT AUTOMATISCH übernommen, sondern in den QRTZ-Tabellen zwischengespeichert. Änderungen übernehmen:
- QRTZ-Tabellen löschen
- DB-Skript V1.0.23__AddQuartzStore.sql ausführen, um die Tabellen neu zu generieren

