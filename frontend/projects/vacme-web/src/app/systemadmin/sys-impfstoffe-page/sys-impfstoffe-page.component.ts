import {Component, OnInit} from '@angular/core';
import {UntypedFormArray, UntypedFormBuilder, UntypedFormGroup} from '@angular/forms';
import {
    ImpfempfehlungChGrundimmunisierungJaxTS,
    ImpfstoffJaxTS,
    ImpfstoffService,
    ImpfstofftypTS,
    KrankheitIdentifierTS,
    KrankheitJaxTS,
    ZulassungsStatusTS,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {Option} from '../../../../../vacme-web-shared';
import {AuthServiceRsService} from '../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import FormUtil from '../../../../../vacme-web-shared/src/lib/util/FormUtil';

const LOG = LogFactory.createLog('SysImpfstoffePageComponent');

@Component({
    selector: 'app-sys-impfstoffe-page',
    templateUrl: './sys-impfstoffe-page.component.html',
    styleUrls: ['./sys-impfstoffe-page.component.scss'],
})
export class SysImpfstoffePageComponent implements OnInit {
    public impfstoffOptions: Option[] = [];
    public formGroup!: UntypedFormGroup;
    private impfstoffe: ImpfstoffJaxTS[] = [];
    public selectedImpfstoff: ImpfstoffJaxTS | undefined;
    public selectFormGroup!: UntypedFormGroup;
    public zulassungsOptionen: Option[] = Object.values(ZulassungsStatusTS).map(status => {
        return {label: status, value: status};
    });
    public impfstofftypOptionen = Object.values(ImpfstofftypTS).map(status => {
        return {label: status, value: status};
    });
    public krankheitsOptionen: Option[] = Object.values(KrankheitIdentifierTS).map(status => {
        return {label: status, value: status};
    });

    constructor(
        private authService: AuthServiceRsService,
        private impfstoffService: ImpfstoffService,
        private fb: UntypedFormBuilder,
    ) {
    }

    ngOnInit(): void {
        this.initForm();
        this.refreshImpfstoffe();
    }

    private refreshImpfstoffe(): void {
        this.impfstoffService.impfstoffResourceGetCompleteImpfstoffeList().subscribe(impfstoffe => {
            this.impfstoffe = impfstoffe;
            this.buildImpfstoffOptions();
        }, error => LOG.error(error));
    }

    public impfstoffSelectChange(): void {
        const impfstoffId = this.selectFormGroup.get('impfstoff')?.value;
        const impfstoff = this.impfstoffe.find(stoff => stoff.id === impfstoffId);

        if (impfstoff) {
            this.formGroup.patchValue(impfstoff);
            this.formGroup.get('krankheiten')?.setValue(
                impfstoff.krankheiten.map((krankheit: KrankheitJaxTS) => krankheit.identifier));
            this.getImpfempfehlungenFormArray().clear();
            impfstoff.impfempfehlungen?.forEach((empfehlung: ImpfempfehlungChGrundimmunisierungJaxTS) => {
                this.addEmpfehlung(empfehlung);
            });
        }

        this.selectedImpfstoff = impfstoff;
    }

    public getImpfempfehlungenFormArray(): UntypedFormArray {
        return this.formGroup.get('impfempfehlungen') as UntypedFormArray;
    }

    private buildImpfstoffOptions(): void {
        this.impfstoffOptions = this.impfstoffe.map(impfstoff => {
            return {
                label: impfstoff.displayName,
                value: impfstoff.id,
                disabled: false,
            };
        });
    }

    private initForm(): void {
        this.selectFormGroup = this.fb.group({
            impfstoff: this.fb.control(undefined),
        });
        this.formGroup = this.fb.group({
            id: [],
            name: this.fb.control(undefined),
            hersteller: this.fb.control(undefined),
            code: this.fb.control(undefined),
            covidCertProdCode: this.fb.control(undefined),
            hexFarbe: this.fb.control(undefined),
            anzahlDosenBenoetigt: this.fb.control(undefined),
            zulassungsStatus: this.fb.control(undefined),
            impfstofftyp: this.fb.control(undefined),
            zulassungsStatusBooster: this.fb.control(undefined),
            whoch2Code: this.fb.control(undefined),
            informationsLink: this.fb.control(undefined),
            impfempfehlungen: this.fb.array([]),
            eingestellt: this.fb.control(undefined),
            krankheiten: this.fb.control(undefined),
        });
    }

    public correctIfValid(): void {
        FormUtil.doIfValid(this.formGroup, () => {
            const impfstoffJax: ImpfstoffJaxTS = {
                ...this.formGroup.value,
                krankheiten: this.mapKrankheiten(this.formGroup.value.krankheiten),
            };
            this.impfstoffService.impfstoffResourceUpdateImpfstoff(impfstoffJax).subscribe(impfstoff => {
                this.refreshImpfstoffe();
                this.selectedImpfstoff = this.impfstoffe.find(impfs => impfs.id === impfstoff.id);
            }, error => LOG.error(error));
        });
    }

    private mapKrankheiten(krankheiten: Array<any>): Array<KrankheitJaxTS> {
        return krankheiten.map(value => {
            return {
                identifier: value, noFreieTermine: false,
            };
        });
    }

    public reset(): void {
        this.selectedImpfstoff = undefined;
        this.selectFormGroup.get('impfstoff')?.setValue(null);
    }

    public getImpfempfehlungenFormGroup(empfehlungFormGroupAny: any): UntypedFormGroup {
        return empfehlungFormGroupAny as UntypedFormGroup;
    }

    public deleteEmpfehlung(i: number): void {
        this.getImpfempfehlungenFormArray().removeAt(i);
    }

    public addEmpfehlung(empfehlung?: ImpfempfehlungChGrundimmunisierungJaxTS): void {
        this.getImpfempfehlungenFormArray().push(this.fb.group({
            id: this.fb.control(empfehlung ? empfehlung.id : undefined),
            anzahlVerabreicht: this.fb.control(empfehlung ? empfehlung.anzahlVerabreicht : undefined),
            notwendigFuerChGrundimmunisierung: this.fb.control(empfehlung ?
                empfehlung.notwendigFuerChGrundimmunisierung :
                undefined),
        }));
    }
}
