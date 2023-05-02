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

DUMP_DIR="/dump"
DATABASE="${VACME_DB}" # configured in docker-compose

DUMP_FILENAME="${1}"

DUMP_FILE="${DUMP_DIR}/${DUMP_FILENAME}"
if [ ! -f "${DUMP_FILE}" ]; then
  echo "Dump file not found: ${DUMP_FILE}"
fi

echo "Restoring from ${DUMP_FILENAME} in docker directory: ${DUMP_DIR}"
mysql \
  --user=root \
   --password="$MYSQL_ROOT_PASSWORD <  "${DUMP_FILE}"
