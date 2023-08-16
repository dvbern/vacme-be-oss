# Architekturbeschrieb

Das System besteht aus mehrere Teilen. Im folgenden werden diese aufgelistet und ihre Funktion wird kurz erklärt.

Es wird mit einer hohen initialen Last bei der erstmaligen Aufschaltung des Systems gerechnet (e.g. 8000 gleichzeitige Benutzer) Initialanmeldung (Anmeldungsschritte bis zum abschicken der Anmeldungsanforderung) gerechnet. Daher ist angedacht insbesondere diesen vorderen Teil der Applikation so skalierbar wie möglich zu bauen.

Zu diesem Zweck wird nicht wie üblich nur 1 Backend und 1 Frontend für vacme erstellt sondern der kritische Teil wird als kleine separate App entwickelt. Diese soll stateless implementiert werden. So wird es möglich sein, schnell neue Server hochzufahren sollte die Last für die bestehenden zu hoch sein. 




##SERVER
###vacme-rest
Dies ist das Restinterface für die Fachapplikation

###vacme-initialreg
Dies ist die kleine Applikation welche lediglich die Initalanmeldungen entegegennimmt und als Json in die Datenbank speichert

###vacme-shared
Hier befindet sich das Entity Model und weitere shared Klassen





##FRONTEND

###projects/vacme-initalreg
In diesem Ordner befindet sich die Angular Applikaton zum erstellen der initialen Registrierung. Sie soll von der grossen Fachapplikation separiert werden um in ihrer Transfergrösse klein zu bleiben. 

###projects/vacme-web
In diesem Ordner befindet sich die Angular Applikation mit der Terminauswahl sowie der Fachapplikaton

###projects/vacme-web-shared
Dies  ist eine Angular Library in welche Components oder Services abgelegt werden können die sowohl von vacme-web-inital wie auch von vacme-web benutzt werden sollen.



