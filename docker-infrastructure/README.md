# Setup

Das File `docker'compose.override-template.yml` Template kopieren und abspeichern unter: `docker'compose.override.yml`.
Das neue File anpassen so wie man es auf seiner Entwicklungsumgebung gerne hätte (portmappings etc). Docker wird automatisch die 
overrides verwenden.


Die secrets von [developer_local_settings_template] nach [developer_local_settings] kopieren und anpassen.


# Datenbank

## Starten
Das Dockerfile kann vom Rootverzeichnis aus mit 
`docker-compose up --build db` gestartet werden

Beim ersten Run wird docker darauf hinweisen dass man noch die external volume mounts für die Vacme-DB und die Keycloak-DB anlegen muss mittels:

`docker volume create --name=vacme-database-mariadb-data` 

`docker volume create --name=vacme-database-mariadb-keycloak-data`



Man kann das Dockerfile auch in IntelliJ per Run Configuration starten statt im Terminal. Dann muss man die Overrides explizit angeben:
`./docker-infrastructure/docker-compose.yml; ./docker-infrastructure/docker-compose.override.yml;`




## Import / Export
Falls man einen Dump hat den man einspielen will.
Im `docker-compose.override.yml` in der Section `db` den Pfad zum DB-Dump Verzeichnis angeben
```yaml
services:
  db:
    volumes:
      # DB-Dumps erstellen/einspielen
      - "./developer_local_settings/db-dump/:/dump/:rw"
```

Dadurch wird das Verzeichnis mit dem dump gemountet.
Dann die DB mit der geaenderten Konfiguration starten. Innerhalb des Containers gibt es ein script Namens
vacme-restore.sh welches zum restoren verwendet werden kann.
Zum restoren kann also zum Beispiel folgender command ausgefuehrt werden

`docker exec -i -t vacme-postgres vacme-restore.sh myCoolDump.sql`


# Keycloak

Man kann beim Entwickeln den Keycloak vom Dev-Server oder den lokalen im Docker verwenden. Die Settings dazu sind im .env-File.

## Keycloak lokal
http://localhost:8180/auth/admin/

### Templates entwickeln (z.B. account.ftl):
- volumes in docker-compose.override.yml einbinden (siehe docker-compose.override-template.yml)

### Konfiguration:
- Backend-Kommunikation mit Keycloak läuft über den User vacme-admin-client (für vacme und vacme-web separat) und den Client vacme-admin-client. Im .env muss dafür das VACME_KEYCLOAK_CLIENTSECRET stimmen (die müssen übereinstimmen mit Vacme-Web/Clients/vacme-admin-client credentials).

Wenn es einen Fehler beim Docker starten gibt, wurde vermutlich beim letzten Mal kein docker-compose down gemacht, das kann man einfach nachholen.

### Konfiguration prüfen: Health Check
Keycloak-Health-Check: http://localhost:8080/health

### Fehlersuche
-> User vacme-admin-client das Role Mapping: Client Roles> account > manage-account und realm-management>realm-admin hinzufügen
-> User vacme-admin-client in beiden Realms erstellen mit Passwort vacme
Alle Sessions ausloggen, damit alles übernommen wird!

Die Logs von Keycloak kann man auch anschauen, z.B. sehr einfach wenn man Docker über IntelliJ gestartet hat.
