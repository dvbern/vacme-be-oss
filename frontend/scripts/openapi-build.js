/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

const fs = require('fs');
const path = require('path');
const childProcess = require('child_process');

function deleteFile(filePath) {
  fs.unlinkSync(filePath);
}

function deleteOldFiles(directory, timestamp) {
  if (fs.existsSync(directory)) {
    let files = fs.readdirSync(directory);
    for (const file of files) {
      const filePath = path.join(directory, file);
      const stats = fs.statSync(filePath);
      if (stats.mtime.getTime() < timestamp.getTime()) {
        deleteFile(filePath);
      }
    }
  }
}

async function generateOpenApi(directory) {
  const env = {
    ...process.env,
    JAVA_OPTS: ''
  };
  const cmd = 'npx @openapitools/openapi-generator-cli generate -i http://localhost:8080/openapi.yml -g typescript-angular'
    + ' -p stringEnums=true'
    + ' -p modelSuffix=TS'
    + ' -p fileNaming=kebab-case'
    + ' -p ngVersion=13.0.1'
    + ' -p sortModelPropertiesByRequiredFlag=false' // Property-Sortierung: uebernimmt Originalreihenfolge
    + ' -p sortParamsByRequiredFlag=false'
    + ' -p enumPropertyNaming=UPPERCASE' // Enums: uppercase plus underscores
    + ' -p removeEnumValuePrefix=false' // sonst schneidet es bei einigen Enums den vordersten Teil einfach ab
    + ' -t ' + directory + '/templates'
    + ' --type-mappings DateTime=Date,date=Date,Date=Date,AnyType=object'
    + ' -o ' + directory + '/lib'
  console.log('executing command: ', cmd);
  const child = childProcess.exec(
    cmd,
    {env}
  );
  child.stdout.on('data', function (data) {
    console.log(data.toString());
  });
  child.stderr.on('data', function (data) {
    console.error(data.toString());
  });

  return new Promise(
    (resolve, reject) => {
      child.on('exit', code => code === 0 ? resolve() : reject("exit code: " + code));
      child.on('error', reject);
    }
  );
}

async function generateIndexTs(directory) {
  // read dir and sort it, otherwise different OS/Locales handle it case in/-sensitive
  let files = fs.readdirSync(directory)
    .sort((a, b) => a.toLocaleLowerCase().localeCompare(b.toLocaleLowerCase()));
  let writer = fs.createWriteStream(path.join(directory, 'index.ts'));
  for (const file of files.filter(f => f !== 'index.ts')) {
    writer.write('export * from \'./' + path.basename(file, '.ts') + '\'\n');
  }
  writer.close();

  return new Promise(resolve => writer.on('close', resolve));
}

async function sleep(msec) {
  return new Promise((resolve) => setTimeout(resolve, msec));
}

// async main
(async () => {

  const timestamp = new Date();
  // noinspection MagicNumberJS
  await sleep(100); // make sure timestamp ticks

  const generatorPath = 'projects/vacme-web-generated/src';
  const modelsPath = path.join(generatorPath, 'lib/model');

  await generateOpenApi(generatorPath)
    .then(_ => generateIndexTs(modelsPath))
    .catch(err => console.error('Error generating models', err));

  deleteOldFiles(modelsPath, timestamp);

})();
