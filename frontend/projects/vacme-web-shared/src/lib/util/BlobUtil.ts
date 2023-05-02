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

import {LogFactory} from '../logging';

const LOG = LogFactory.createLog('BlobUtil');

export class BlobUtil {

    /**
     * Opens a blob file in a new tab.
     * To prevent popup blocking it is better to use openBlobInWindow,
     * but the download doesn't work properly with openBlobInWindow (yet)
     *
     * @param blob file
     * @param doc current DOM
     */
    public static openInNewTab(blob: Blob, doc: Document = document): void {
        const downloadUrl = URL.createObjectURL(blob); // create a blob url, which will be removed when the user closes the application

        const anchor = doc.createElement('a');
        anchor.href = downloadUrl;
        anchor.target = '_blank'; // open in new tab
        anchor.click();
    }

    /**
     * Currently not used
     * - Cannot download the file in chrome, there is a network error.
     * - The file extension is not added (.pdf) in chrome
     * - Only works on Firefox for now
     *
     * @param blob
     * @param newWindow
     */
    public static openBlobInWindow(blob: Blob, newWindow: Window | null): void {
        LOG.trace('blob response: ', blob.type, blob.size, newWindow);
        if (!newWindow) {
            LOG.error('Could not open new window');
            return;
        }

        // data-url lifecycle is tied to the document by which it was created (here: newWindow.document).
        //  => since this is a new window containing only the document, no manual cleanup is needed.
        const dataUrl = (newWindow as any).URL.createObjectURL(blob);
        newWindow.location.href = dataUrl;
    }

    public static parseErrorMessage(blob: Blob, handledViolations: string[]): Promise<string> {
        return new Promise<string>(((resolve, reject) => {

            const reader = new FileReader();

            reader.addEventListener('loadend', () => {
                const error = JSON.parse(reader.result as string);
                LOG.error('could not parse errormessage', error);

                // hacky way required as the response is a blob and not parsed by the global handler
                if ('type' in error && error.type === 'VALIDATION') {
                    if ('violations' in error) {
                        const violations = error.violations as { key: string; message: string }[];
                        if (violations.length >= 0) {
                            const violation = violations[0];
                            if (handledViolations.includes(violation.key)) {
                                resolve(violations[0].message);
                            }

                        }
                    }
                }
                reject();
            });

            reader.readAsText(blob);
        }));
    }

    public static readBlobAsJson(blob: Blob): Promise<any> {

        /*
        Kurzform mit text(), die auf Linux&Chrome nicht funktioniert hat (HEFR 20211109):
        return blob.text().then(text => {
            try {
                return JSON.parse(text);
            } catch (e) {
                console.error('Unable to parse Blob as json');
                return blob; // fallback: return the unparsed blob (or whatever it actually was)
            }
        });*/

        return new Promise<string>(((resolve, reject) => {
            // laengere Variante mit FileReader.readAsText, die wir vorher schon benutzten:
            const reader = new FileReader();
            reader.addEventListener('loadend', () => {
                try {
                    const json = JSON.parse(reader.result as string);
                    resolve(json);
                } catch (e) {
                    reject();
                }
            });
            reader.readAsText(blob);
        }));

    }

}
