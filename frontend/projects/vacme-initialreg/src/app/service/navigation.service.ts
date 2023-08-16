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

import {Injectable} from '@angular/core';
import {NavigationExtras, Router} from '@angular/router';
// falsch: import Swal from 'sweetalert2'; // Hier wird nicht nur das JS, sondern auch das CSS importiert
import {DashboardJaxTS, KantonaleBerechtigungTS, KrankheitIdentifierTS} from 'vacme-web-generated';
import {VacmeSettingsService} from '../../../../vacme-web-shared/src/lib/service/vacme-settings.service';
import TenantUtil from '../../../../vacme-web-shared/src/lib/util/TenantUtil';

@Injectable({
    providedIn: 'root',
})
export class NavigationService {

    constructor(
        private router: Router,
        private vacmeSettingsService: VacmeSettingsService
    ) {
    }

    // TODO Affenpocken we can pull the Krankheit argument into this method I believe. Or define a Tenant variable
    //  singleGuiKrankheit.
    public navigateToStartpage(
        registrierungsnummer: string | undefined,
        krankheit: KrankheitIdentifierTS,
    ): void {
        if (TenantUtil.isMultiKrankheitGUI()) {
            void this.router.navigate(['impfdossiers-overview', registrierungsnummer]);
        } else {
           this.navigateToDossierDetail(registrierungsnummer, krankheit);
        }
    }

    public navigateToDossierAddition(registrierungsnummer: string | undefined): void {
        if (TenantUtil.isMultiKrankheitGUI()) {
            void this.router.navigate(['impfdossiers-auswahl', registrierungsnummer]);
        } else {
            throw new Error('Cant navigate here because Tenant is not MultiKrankheitGUI enabled.');
        }
    }

    public navigatePreRegistration(keycloakRegistration: () => Promise<void>): void {
        if (TenantUtil.isMultiKrankheitGUI()) {
            void keycloakRegistration();
        } else {
            void this.navigateToImpffaehigkeit();
        }
    }

    public navigatePostKeycloakRegistration(): void {
        if (TenantUtil.isMultiKrankheitGUI()) {
            void this.navigateToPersonendaten();
        } else {
            void this.navigateToImpffaehigkeit();
        }
    }

	public navigateToImpffaehigkeit(): Promise<boolean> {
		return this.router.navigate(['/impffaehigkeit']);
	}

	public navigateToPersonendaten(): Promise<boolean> {
		return this.router.navigate(['/personendaten']);
	}

    public navigateToDossierDetail(
        registrierungsnummer: string | undefined,
        krankheit: KrankheitIdentifierTS,
    ): void {
        this.navigateToDossierDetailWithNavigationExtras(
           registrierungsnummer,
            krankheit,
            undefined);
    }

    public navigateToDossierDetailOrExternGeimpftPage(
        registrierungsnummer: string | undefined,
        krankheit: KrankheitIdentifierTS,
        externGeimpftConfirmationNeeded: boolean,
    ): void {
        if (externGeimpftConfirmationNeeded) {
            this.navigateToExternGeimpftPage(registrierungsnummer, krankheit);
        } else {
            this.navigateToDossierDetail(registrierungsnummer, krankheit);
        }
    }

    public navigateToDossierDetailCheckingPreConditions(
        registrierungsnummer: string | undefined,
        krankheit: KrankheitIdentifierTS,
        leistungerbringerAgbConfirmationNeeded: boolean,
        externGeimpftConfirmationNeeded: boolean
    ): void {
        if (this.vacmeSettingsService.getKantonaleBerechtigung(krankheit)
            === KantonaleBerechtigungTS.LEISTUNGSERBRINGER) {
            if (leistungerbringerAgbConfirmationNeeded) {
                this.navigateToLeistungserbringerAgbPage(registrierungsnummer, krankheit);
                return;
            }
        }
        const externConfirmationNeeded = externGeimpftConfirmationNeeded
            ?? this.vacmeSettingsService.supportsExternesZertifikat(krankheit);
        this.navigateToDossierDetailOrExternGeimpftPage(registrierungsnummer,
            krankheit,
            externConfirmationNeeded);
    }

    public navigateToExternGeimpftPage(
        registrierungsnummer: string | undefined,
        krankheit: KrankheitIdentifierTS
    ): void {
        void this.router.navigate(['/externgeimpft', registrierungsnummer, 'krankheit' , krankheit]);
    }


    public navigateToDossierDetailWithNavigationExtras(
        registrierungsnummer: string | undefined,
        krankheit: KrankheitIdentifierTS,
        extras?: NavigationExtras
    ): void {
        void this.router.navigate([
            '/overview/',
            registrierungsnummer,
            'krankheit',
            krankheit], extras);
    }

    public navigateToErkrankungen(dossier: DashboardJaxTS): void {
        void this.router.navigate([
            'registrierung',
            dossier.registrierungsnummer,
            'erkrankungen',
            'krankheit',
            dossier.krankheitIdentifier
        ]);
    }

    public navigateToLeistungserbringerAgbPage(
        registrierungsnummer: string | undefined,
        krankheit: KrankheitIdentifierTS,
    ): void {
        void this.router.navigate(['/leistungserbringer-agb', registrierungsnummer, 'krankheit' , krankheit]);
    }
}
