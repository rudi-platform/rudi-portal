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

    /**
     * Correspondance entre les valeurs du thésaurus RUDI `file_encoding` (cf. rudi-node-catalog,
     * `src/definitions/thesaurus/Encodings.js`) et les labels reconnus par `TextDecoder`.
     * 'Unicode' et 'ISO/IEC 10646' sont volontairement absents : c'est la valeur par défaut du
     * schéma quand personne n'a renseigné le champ, pas une déclaration d'encodage exploitable.
     * @private
     */
    private static readonly ENCODING_NAME_TO_DECODER_LABEL: {[fileEncoding: string]: string} = {
        'UTF-8': 'utf-8',
        'UTF-16': 'utf-16le',
        'ISO/CEI 8859-1': 'iso-8859-1',
        'MacRoman': 'macintosh',
        'Windows-1252': 'windows-1252',
        'US-ASCII': 'windows-1252',
        'ASCII': 'windows-1252',
        'ANSI': 'windows-1252'
    };

    constructor(
        private readonly konsultMetierService: KonsultMetierService,
        private readonly getBackendPropertyPipe: GetBackendPropertyPipe,
        private readonly konsultService: KonsultService,
        private readonly translateService: TranslateService
    ) {

    }

    /**
     * Téléchargement d'un fichier tabulaire et récupération du contenu en mémoire au format Workbook XLS
     * @param mediaUrl URL du média à télécharger
     * @param fileEncoding encodage source déclaré par le producteur (`MediaFile.file_encoding`), si disponible
     */
    downloadTableFile(mediaUrl, fileEncoding?: string): Observable<WorkBook> {

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
            // parse du blob en XLSX ou CSV
            map((arrayBuffer: ArrayBuffer) => this.decodeTableFile(arrayBuffer, fileEncoding))
        );
    }

    /**
     * Décodage du contenu brut d'un média tabulaire avant passage à SheetJS, qui décode en Latin-1
     * par défaut tout texte sans BOM (XLS/XLSX binaires non concernés, SheetJS les détecte seul).
     *
     * Priorité à l'encodage déclaré par le producteur (`file_encoding`) quand il est exploitable :
     * c'est la source de vérité. À défaut (champ absent, ou valeur par défaut générique du schéma
     * type 'Unicode'), on tente un décodage UTF-8 strict puis on retombe sur le comportement
     * SheetJS historique — la grande majorité des médias existants n'ayant pas encore cette
     * métadonnée renseignée.
     * @private
     */
    private decodeTableFile(arrayBuffer: ArrayBuffer, fileEncoding?: string): WorkBook {
        const declaredLabel = fileEncoding ? DisplayTableService.ENCODING_NAME_TO_DECODER_LABEL[fileEncoding] : undefined;
        if (declaredLabel) {
            try {
                const text = new TextDecoder(declaredLabel, {fatal: true}).decode(arrayBuffer);
                return read(text, {type: 'string'});
            } catch {
                // L'encodage déclaré ne correspond pas aux octets réels : on se replie sur l'heuristique
            }
        }
        try {
            const text = new TextDecoder('utf-8', {fatal: true}).decode(arrayBuffer);
            return read(text, {type: 'string'});
        } catch {
            return read(arrayBuffer, {type: 'array'});
        }
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
        columnDefs.push(SPREADSHEET_COLDEF_INDEX);
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
                return parseInt(tableDisplayMaxFileSize, 10);
            })
        );
    }
}
