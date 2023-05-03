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

import {canton} from '../../cantons/canton';


export default class TenantUtil {

    public static BERN = canton.name === 'be';
    public static ZURICH = canton.name === 'zh';

    public static is(tenant: string): boolean {
        return canton.name === tenant;
    }

    /**
     * Returns whether mobile orte der impfung are enable for the current canton
     * or not
     * Aus Sicht der Registrierung
     */
    public static hasMobileOrtDerImpfung(): boolean {
        return canton.mobileOrtDerImpfung;
    }

    /**
     * Returns whether mobile orte der impfung are enable for the current canton
     * or not
     * Aus Sicht der Administration
     */
    public static hasMobilerOrtDerImpfungAdministration(): boolean {
        return canton.mobileOrtDerImpfungAdmin;
    }

    /**
     * Indicates whether the Http Translation loader should load specific translations
     * for the canton.
     *
     * e.g. de.json and de.zh.json where the translations in de.zh.json will be merged
     * into the base translations
     */
    public static hasAdditionalTranslations(): boolean {
        return canton.additionalTranslations;
    }

    static getBeruflicheTaetigkeit(): any[] {
        return canton.beruflicheTaetigkeit;
    }

    public static hasOnboarding(): boolean {
        return canton.hasOnboarding;
    }

    public static hasFachanwendungOnboarding(): boolean {
        return canton.hasFachanwendungOnboarding;
    }

    public static showSchwangerWarnung(): boolean {
        return canton.showSchwangerWarnung;
    }

    public static showFHIRDownload(): boolean {
        return canton.showFHIRDownload;
    }

    static hasContactTracing(): boolean {
        return canton.contactTracing;
    }

    static hasMassenTermineAbsagen(): boolean {
        return canton.massenTermineAbsagen;
    }

    static hasOdiAttributesEnabled(): boolean {
        return canton.odiAttributesEnabled;
    }

    static zweiKinderProTerminErlaubt(): boolean {
        return canton.zweiKinderProTermin;
    }

    static hasKeinKontakt(): boolean {
        return canton.keinKontakt;
    }

    static isMultiKrankheitGUI(): boolean {
        return canton.multiKrankheitGUI;
    }

    static hasFluechtlingUeberKrankenkasse(): boolean {
        return canton.fluechtlingUeberKrankenkasse;
    }

    static hasSimplifiedLebensumstaende(): boolean {
        return canton.simplifiedLebensumstaende;
    }

    public static showQRCode(): boolean {
        return canton.showQRCode;
    }

    public static isWellPartnerDomain(hostname: string): boolean {
        // check hostname through document and laod appropriate config, this is needed for the well keycloak-client
        const wellPartnerDomainMarker: string | null = canton.wellPartnerDomainMarker;
        if (wellPartnerDomainMarker) {
            return hostname.startsWith(wellPartnerDomainMarker);
        }
        return false;
    }
}
