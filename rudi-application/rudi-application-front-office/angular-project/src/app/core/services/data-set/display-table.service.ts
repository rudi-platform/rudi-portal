import {HttpResponse} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {SPREADSHEET_COLDEF_INDEX} from '@features/data-set/components/spreadsheet/spreadsheet.component';
import {TranslateService} from '@ngx-translate/core';
import {ErrorWithCause} from '@shared/models/error-with-cause';
import {GetBackendPropertyPipe} from '@shared/utils/pipes/get-backend-property.pipe';
import {ColDef} from 'ag-grid-community';
import {KonsultService} from 'micro_service_modules/konsult/konsult-api';
import {Observable} from 'rxjs';
import {map, switchMap} from 'rxjs/operators';
import {read, utils, WorkBook} from 'xlsx';
import {KonsultMetierService} from '../konsult-metier.service';
import {DisplayTableDataInterface} from './display-table-data.interface';
import {readFile} from './display.function';

@Injectable({
    providedIn: 'root'
})
export class DisplayTableService {

    static readonly FILE_SIZE_ERROR_CODE = 12;

    /**
     * le nom de la première "Feuille" de calcul d'un fichier CSV ou XLS importé dans SheetJS
     * @private
     */
    private static readonly FIRST_SHEET_NAME = 'Sheet1';

    constructor(
        private readonly konsultMetierService: KonsultMetierService,
        private readonly getBackendPropertyPipe: GetBackendPropertyPipe,
        private readonly konsultService: KonsultService,
        private readonly translateService: TranslateService
    ) {

    }

    /**
     * Téléchargement d'un fichier tabulaire et récupération du contenu en mémoire au format Workbook XLS
     * @param mediaUrl URL du média tabulaire (CSV/XLS) à télécharger
     * @param fileEncoding encodage source déclaré sur le média publié (MediaFile.file_encoding), s'il est connu.
     *        Permet de décoder correctement les fichiers texte (CSV) et d'éviter les caractères accentués cassés.
     */
    downloadTableFile(mediaUrl: string, fileEncoding?: string): Observable<WorkBook> {

        return this.konsultMetierService.downloadMetadataMedia(mediaUrl).pipe(
            switchMap((responseBlob: HttpResponse<Blob>) => {
                const blob = new Blob([responseBlob.body], {type: responseBlob.body.type});
                // Vérification de la taille max autorisée pour télécharger
                return this.getTableDisplayMaxFileSize().pipe(
                    switchMap((maxFileSize: number) => {
                        if (blob.size > maxFileSize) {
                            const errorMessage = this.translateService.instant('metaData.tabulatedDataTab.errorFileSize');
                            throw new ErrorWithCause(errorMessage, null, DisplayTableService.FILE_SIZE_ERROR_CODE);
                        }
                        // Lecture du flux du blob
                        return readFile(blob);
                    })
                );
            }),
            // parse du blob en XLSX ou CSV en s'appuyant en priorité sur l'encodage déclaré du média
            map((arrayBuffer: ArrayBuffer) => {
                return this.readWorkbook(arrayBuffer, fileEncoding);
            })
        );
    }

    /**
     * Parse un flux binaire en Workbook SheetJS en privilégiant l'encodage source déclaré du média.
     *
     * Stratégie :
     * 1. Si un encodage est déclaré sur le média et supporté par le navigateur, on décode le flux avec cet
     *    encodage puis on laisse SheetJS parser le texte résultant : c'est la source de vérité.
     * 2. Sinon, on applique une heuristique : on tente un décodage UTF-8 strict (mode fatal) ; en cas d'échec
     *    on retombe sur le comportement SheetJS par défaut (décodage binaire, assimilé à du Latin-1).
     *
     * @param arrayBuffer contenu binaire du fichier
     * @param fileEncoding encodage source déclaré sur le média (MediaFile.file_encoding), optionnel
     * @private
     */
    private readWorkbook(arrayBuffer: ArrayBuffer, fileEncoding?: string): WorkBook {
        const declaredEncoding = this.normalizeEncoding(fileEncoding);
        if (declaredEncoding) {
            try {
                const text = new TextDecoder(declaredEncoding, {fatal: true}).decode(arrayBuffer);
                return read(text, {type: 'string'});
            } catch {
                // Encodage déclaré non supporté par le navigateur ou flux incohérent : on bascule sur l'heuristique
            }
        }

        // Heuristique de repli : UTF-8 strict d'abord, sinon comportement SheetJS par défaut (Latin-1)
        try {
            const text = new TextDecoder('utf-8', {fatal: true}).decode(arrayBuffer);
            return read(text, {type: 'string'});
        } catch {
            return read(arrayBuffer, {type: 'array'});
        }
    }

    /**
     * Normalise un libellé d'encodage issu des métadonnées du média vers un label accepté par TextDecoder.
     * Retourne undefined si l'encodage est absent ou vide.
     * @param fileEncoding encodage déclaré sur le média
     * @private
     */
    private normalizeEncoding(fileEncoding?: string): string | undefined {
        const trimmed = fileEncoding?.trim();
        return trimmed ? trimmed : undefined;
    }

    /**
     * Conversion d'un Workbook en un objet affichable dans un tableau
     * @param workbook le workbook extrait d'un CSV ou XLS
     * @param withHeaders si on considère que la première ligne est un en-tête de colonnes
     */
    convertToDisplayableData(workbook: WorkBook, withHeaders: boolean): DisplayTableDataInterface {

        // Par défaut la première feuille de calcul s'appelle 'Sheet1'
        // c'est le cas d'un fichier CSV importé qui ne possèdes pas de 'Workbook'
        let sheetName = DisplayTableService.FIRST_SHEET_NAME;
        if (workbook.Workbook != null) {
            // Dans le cadre d'un fichier XLS la propriété Workbook est définie et il faut chercher le nom de la Sheet dedans
            // On ne gère l'affichage que de la première Sheet
            sheetName = workbook.Workbook.Sheets[0].name;
        }

        const worksheet = workbook.Sheets[sheetName];

        // L'option 'header' à la conversion de la worksheet en JSON permet de définir les en-têtes
        // si = null alors le contenu est parsé en supposant que la première ligne définit les entêtes
        // Si = A alors on considère qu'il n'y a que des données, et les entêtes sont générés comme dans excel (A, B ...)
        let header = null;
        if (!withHeaders) {
            header = 'A';
        }
        const sheet = utils.sheet_to_json(worksheet, {header});

        // Les noms de colonnes du tableau sont les noms des 'clés' des entrées d'une ligne
        const columnNames: string[] = Object.keys(sheet[0]);
        const columnDefs: ColDef[] = [];
        columnDefs.push({
            ...SPREADSHEET_COLDEF_INDEX,
            headerName: this.translateService.instant('metaData.tabulatedDataTab.rowNumberHeader')
        });
        columnNames.forEach((columnName: string) => {
            columnDefs.push({field: columnName});
        });

        // Les coldefs sont construits à partir des noms de colonnes déduits et les données tabulaires sont déjà formatées en JSON
        // pour un affichage dans le tableau
        return {
            columnDefs,
            rowData: sheet
        } as DisplayTableDataInterface;
    }

    /**
     * Récupération de la taille max autorisée pour un affichage tabulaire
     * @private
     */
    private getTableDisplayMaxFileSize(): Observable<number> {
        return this.getBackendPropertyPipe.transform('front.tableDisplayMaxFileSize').pipe(
            map((tableDisplayMaxFileSize: string) => {
                return Number.parseInt(tableDisplayMaxFileSize, 10);
            })
        );
    }
}
