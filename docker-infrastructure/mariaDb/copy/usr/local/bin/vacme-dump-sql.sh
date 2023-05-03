#!/bin/bash
#
# Copyright (C) 2022 DV Bern AG, Switzerland
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.
#

set -e

# Optionally set the filename prefix from commandline.
# Usecase: if you want to dump the db before starting a new feature,
# you might want to use the feature-name as prefix
FILENAME_PREFIX=${1:-vacme-dump}

DUMP_DIR="/dump"
DATABASE="${VACME_DB}" # configured in docker-compose

timestamp=$(date -Iseconds | sed -e 's/:/-/g')
DUMP_FILENAME="${FILENAME_PREFIX}-${timestamp}.sql"
DUMP_FILE="${DUMP_DIR}/${DUMP_FILENAME}"

echo "Dumping to '${DUMP_FILENAME}' in Docker directory: ${DUMP_DIR}"

if [ ! -d "${DUMP_DIR}" ]; then
  echo "Directory for Dump did not exist in Docker. Creating: ${DUMP_DIR}"
  mkdir  "${DUMP_DIR}";
fi

mysqldump \
  --user="root" \
  --password="${MYSQL_ROOT_PASSWORD}" \
  --hex-blob \
  --databases "${DATABASE}" > "${DUMP_FILE}"
echo "done dumping"