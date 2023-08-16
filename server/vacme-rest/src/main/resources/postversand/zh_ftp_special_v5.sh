#!/bin/bash

DATE=`date "+%Y-%m-%d 07:45"`
DATES=`date "+%Y-%m-%d-07-45"`
ZIP="vacme-zh-${DATES}"
DATABASE=vacme_zh

DOKUMENTTERMIN=`mysql -Ns -D ${DATABASE} -e "SELECT registrierungsnummer FROM Registrierung INNER JOIN Impfdossier I on Registrierung.id = I.registrierung_id INNER JOIN ImpfdossierFile F on I.id = F.impfdossier_id WHERE I.krankheitIdentifier = 'COVID' AND F.fileTyp LIKE 'TERMIN_BESTAETIGUNG' AND F.abgeholt = FALSE AND F.timestampErstellt < '${DATE}';"`
DOKUMENTREG=`mysql -Ns -D ${DATABASE} -e "select registrierungsnummer from Registrierung inner join RegistrierungFile RF ON Registrierung.id = RF.registrierung_id where RF.fileTyp = 'REGISTRIERUNG_BESTAETIGUNG' and abgeholt = false and RF.timestampErstellt < '${DATE}';"`
DOKUMENTABSAGE=`mysql -Ns -D ${DATABASE} -e "SELECT registrierungsnummer FROM Registrierung INNER JOIN Impfdossier I on Registrierung.id = I.registrierung_id INNER JOIN ImpfdossierFile F on I.id = F.impfdossier_id WHERE I.krankheitIdentifier = 'COVID' AND F.fileTyp LIKE 'TERMIN_ABSAGE' AND abgeholt = FALSE AND F.timestampErstellt < '${DATE}';"`
DOKUMENTZERTSTORNO=`mysql -Ns -D ${DATABASE} -e "SELECT DISTINCT registrierungsnummer FROM Registrierung INNER JOIN Impfdossier I on Registrierung.id = I.registrierung_id INNER JOIN ImpfdossierFile F on I.id = F.impfdossier_id WHERE I.krankheitIdentifier = 'COVID' AND F.fileTyp = 'TERMIN_ZERTIFIKAT_STORNIERUNG' AND abgeholt = FALSE AND F.timestampErstellt < '${DATE}';"`
DOKUMENTONBOARDING=`mysql -Ns -D ${DATABASE} -e "select distinct registrierungsnummer from Registrierung inner join RegistrierungFile RF ON Registrierung.id = RF.registrierung_id where RF.fileTyp = 'ONBOARDING_LETTER' and abgeholt = false and RF.timestampErstellt < '${DATE}' and RegistrierungsEingang != 'ONLINE_REGISTRATION';"`
DOKUMENTBOOSTER=`mysql -Ns -D ${DATABASE} -e "SELECT DISTINCT registrierungsnummer FROM Registrierung INNER JOIN Impfdossier I on Registrierung.id = I.registrierung_id INNER JOIN ImpfdossierFile F on I.id = F.impfdossier_id WHERE I.krankheitIdentifier = 'COVID' AND F.fileTyp = 'FREIGABE_BOOSTER_INFO' AND F.abgeholt = FALSE AND F.timestampErstellt < '${DATE}' ORDER BY F.timestampErstellt LIMIT 7000;"`

#echo $DOKUMENTTERMIN

for d in ${DOKUMENTTERMIN}; do
  echo "Termin: $d"
  mysql -D ${DATABASE} -e "select data into DUMPFILE '/tmp/Terminbestaetigung_${d}.pdf' from Registrierung INNER JOIN Impfdossier I on Registrierung.id = I.registrierung_id INNER JOIN ImpfdossierFile F on I.id = F.impfdossier_id WHERE I.krankheitIdentifier = 'COVID' AND F.fileTyp LIKE 'TERMIN_BESTAETIGUNG' AND abgeholt = FALSE AND registrierungsnummer = '${d}';"
  ID=`mysql -Ns -D ${DATABASE} -e "SELECT DISTINCT I.id FROM Registrierung INNER JOIN Impfdossier I on Registrierung.id = I.registrierung_id INNER JOIN ImpfdossierFile F on I.id = F.impfdossier_id WHERE I.krankheitIdentifier = 'COVID' AND F.fileTyp LIKE 'TERMIN_BESTAETIGUNG' AND registrierungsnummer = '${d}';"`
  if [ "$ID" != "" ]; then
    mysql -D ${DATABASE} -e "UPDATE ImpfdossierFile SET abgeholt = TRUE WHERE impfdossier_id = '${ID}';"
  else
    echo "$d: ERROR ID for $d empty"
  fi
done

for d in ${DOKUMENTREG}; do
  echo "Registrierung: $d"
  # ugly but im not looking for a price here
  if ! [[ ${DOKUMENTTERMIN} =~ (^|[[:space:]])$d($|[[:space:]]) ]]; then
    mysql -D ${DATABASE} -e "select data into DUMPFILE '/tmp/Registrierungsbestaetigung_${d}.pdf' from Registrierung inner join RegistrierungFile RF ON Registrierung.id = RF.registrierung_id where RF.fileTyp like 'REGISTRIERUNG_BESTAETIGUNG' AND registrierungsnummer = '${d}';"
  else
    echo "$d: Skipping export of Registration Form because Termin pdf exists"
  fi

  ID=`mysql -Ns -D ${DATABASE} -e "select Registrierung.id from Registrierung inner join RegistrierungFile RF ON Registrierung.id = RF.registrierung_id where RF.fileTyp like 'REGISTRIERUNG_BESTAETIGUNG' AND registrierungsnummer = '${d}';"`
  if [ "$ID" != "" ]; then
    mysql -D ${DATABASE} -e "update RegistrierungFile SET abgeholt = true where registrierung_id = '${ID}';"
  else
    echo "$d: ERRRO ID for $d empty"
  fi
done

for d in ${DOKUMENTABSAGE}; do
  echo "Absage: $d"
  mysql -D ${DATABASE} -e "select data into DUMPFILE '/tmp/Terminabsage_${d}.pdf' from Registrierung INNER JOIN Impfdossier I on Registrierung.id = I.registrierung_id INNER JOIN ImpfdossierFile F on I.id = F.impfdossier_id WHERE I.krankheitIdentifier = 'COVID' AND F.fileTyp LIKE 'TERMIN_ABSAGE' AND registrierungsnummer = '${d}';"
  ID=`mysql -Ns -D ${DATABASE} -e "SELECT I.id FROM Registrierung INNER JOIN Impfdossier I on Registrierung.id = I.registrierung_id INNER JOIN ImpfdossierFile F on I.id = F.impfdossier_id WHERE I.krankheitIdentifier = 'COVID' AND F.fileTyp LIKE 'TERMIN_ABSAGE' AND registrierungsnummer = '${d}';"`
  if [ "$ID" != "" ]; then
    mysql -D ${DATABASE} -e "UPDATE ImpfdossierFile SET abgeholt = TRUE WHERE impfdossier_id = '${ID}';"
  else
    echo "$d: ERROR ID for $d empty"
  fi
done

for d in ${DOKUMENTZERTSTORNO}; do
  DOKUMENTE=`mysql -Ns -D ${DATABASE} -e "SELECT ImpfdossierFile.id FROM ImpfdossierFile INNER JOIN Impfdossier I on ImpfdossierFile.impfdossier_id = I.id INNER JOIN Registrierung R on I.registrierung_id = R.id WHERE R.registrierungsnummer = '${d}' AND fileTyp = 'TERMIN_ZERTIFIKAT_STORNIERUNG' AND abgeholt = FALSE AND I.krankheitIdentifier = 'COVID';"`

  for id in ${DOKUMENTE}; do
  if [ "$id" != "" ]; then
    mysql -D ${DATABASE} -e "select data into DUMPFILE '/tmp/Zertifikatsstornierung_${d}_${id}.pdf' from ImpfdossierFile where id = '${id}';"
    mysql -D ${DATABASE} -e "UPDATE ImpfdossierFile SET abgeholt = TRUE WHERE id = '${id}';"
  else
    echo "$d: ERROR ID for $d empty"
  fi
  done
done

for d in ${DOKUMENTONBOARDING}; do
  DOKUMENTE=`mysql -Ns -D ${DATABASE} -e "select RegistrierungFile.id from RegistrierungFile inner join Registrierung REG on RegistrierungFile.registrierung_id = REG.id where REG.registrierungsnummer = '$d' AND fileTyp = 'ONBOARDING_LETTER' and abgeholt = false;"`

  for id in ${DOKUMENTE}; do
    if [ -z "$id" ]; then
      echo "$d: ERROR ID for $d empty"
    else
      mysql -D ${DATABASE} -e "select data into DUMPFILE '/tmp/Onboarding_${d}_${id}.pdf' from RegistrierungFile where id = '${id}';"
      mysql -D ${DATABASE} -e "UPDATE RegistrierungFile SET abgeholt = TRUE WHERE id = '${id}';"
    fi
  done
done

for d in ${DOKUMENTBOOSTER}; do
  DOKUMENTE=`mysql -Ns -D ${DATABASE} -e "SELECT ImpfdossierFile.id FROM ImpfdossierFile INNER JOIN Impfdossier I on ImpfdossierFile.impfdossier_id = I.id INNER JOIN Registrierung R on I.registrierung_id = R.id WHERE R.registrierungsnummer = '$d' AND ImpfdossierFile.fileTyp = 'FREIGABE_BOOSTER_INFO' AND ImpfdossierFile.abgeholt = FALSE AND I.krankheitIdentifier = 'COVID';"`

  for id in ${DOKUMENTE}; do
    if [ -z "$id" ]; then
      echo "$d: ERROR ID for $d empty"
    else
      mysql -D ${DATABASE} -e "select data into DUMPFILE '/tmp/Boostereinladung_${d}_${id}.pdf' from ImpfdossierFile where id = '${id}';"
      mysql -D ${DATABASE} -e "UPDATE ImpfdossierFile SET abgeholt = TRUE WHERE id = '${id}';"
    fi
  done
done

cd /tmp/systemd-private-*-mariadb.service-*/tmp/
zip -9 $ZIP.zip *.pdf

# Lad mich hoch # EDMA (02.02.2021)
lftp -u prj_vacmesps,jahth9iesh5O vacme.transfer.dvbern.ch << EOF

mput $ZIP.zip
bye

EOF

ret=$?

if [ "$ret" == 0 ]; then
  rm -f /tmp/systemd-private-*-mariadb.service-*/tmp/*.pdf
#  cp /tmp/systemd-private-*-mariadb.service-*/tmp/$ZIP.zip /root/
  rm -f /tmp/systemd-private-*-mariadb.service-*/tmp/$ZIP.zip
else
  echo "Error: ftp failed for unknown reason"
  exit 1
fi

exit 0
