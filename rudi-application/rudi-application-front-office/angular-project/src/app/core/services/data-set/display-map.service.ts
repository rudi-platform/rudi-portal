import {Injectable} from '@angular/core';
import {KonsultMetierService} from '@core/services/konsult-metier.service';
import {SearchAutocompleteItem} from '@shared/search-autocomplete/search-autocomplete-item.interface';
import {Metadata, MetadataGeographyBoundingBox} from 'micro_service_modules/api-kaccess';
import {Address, KonsultService} from 'micro_service_modules/konsult/konsult-api';
import {LayerInformation, Proj4Information} from 'micro_service_modules/konsult/konsult-model';
import {Feature} from 'ol';
import GeoJSON from 'ol/format/GeoJSON';
import {Geometry, Polygon} from 'ol/geom';
import {get, getTransform, Projection, TransformFunction} from 'ol/proj';
import {register} from 'ol/proj/proj4';
import proj4 from 'proj4';
import {Observable, of} from 'rxjs';
import {map, switchMap} from 'rxjs/operators';
import {KonsultRvaService} from '../rva/konsult/konsult-rva.service';
import {readFile} from './display.function';
import { boundingExtent } from 'ol/extent';

/**
 * Projection utilisée par la vue OpenLayers par défaut
 */
export const DEFAULT_VIEW_PROJECTION = 'EPSG:3857';

/**
 * Projection WGS84 pour la bounding box
 */
export const GPS_PROJECTION = 'EPSG:4326';

/**
 * Objet de parsing des géométries au format GeoJSON
 */
const geoJSON = new GeoJSON();

@Injectable({
    providedIn: 'root'
})
export class DisplayMapService {

    private KONSULT_SERVICE_BASEPATH = '/konsult/v1';

    constructor(
        private readonly konsultService: KonsultService,
        private readonly konsultRvaService: KonsultRvaService,
        private readonly konsultMetierService: KonsultMetierService
    ) {

    }

    /**
     * Créée une feature géo à partir de la bounding box
     * @param boundingBox la bounding box des métadatas
     * @param projectionTransformer fonction de reprojection
     */
    private static createBoundingBoxFeature(boundingBox: MetadataGeographyBoundingBox,
                                            projectionTransformer: TransformFunction): Feature<Polygon> {

        // Calcul des points projetés dans la proj de destination
        const nordOuestSource = [boundingBox.west_longitude, boundingBox.north_latitude];
        const sudEstSource = [boundingBox.east_longitude, boundingBox.south_latitude];
        const nordOuestDestination = projectionTransformer(nordOuestSource, undefined, undefined);
        const sudEstDestination = projectionTransformer(sudEstSource, undefined, undefined);

        // Création des coordonnées du polygone :
        // 1 outer ring qui fait le tour de la bounding box dans le sens horaire
        const polygonCordinates: number[][][] = [
            [
                nordOuestDestination,
                [nordOuestDestination[0], sudEstDestination[1]],
                sudEstDestination,
                [sudEstDestination[0], nordOuestDestination[1]],
                nordOuestDestination
            ]
        ];

        const poly = new Polygon(polygonCordinates);

        // On crée une feature avec les coordonnées
        return new Feature<Polygon>({
            geometry: poly
        });
    }

    /**
     * Recherche d'un code EPSG dans une chaîne de caractère
     * @param epsgString la chaîne contenant le code EPSG d'une projection
     * @private
     */
    private static getEpsgCode(epsgString: string): string {

        if (epsgString == null) {
            return null;
        }

        const searchString = 'EPSG:';
        const regex = new RegExp(`${searchString}(\\d{4})`);
        const match = regex.exec(epsgString);

        if (match && match.length > 1) {
            const matchedNumbers = match[1];
            return 'EPSG:' + matchedNumbers;
        }

        return null;
    }

    /**
     * Récupère et construit une Feature OL à partir de la Bbox définie dans le JDD
     * @param metadata le JDD qui contient la BBOX
     * @param destinationProjection la projection d'affichage de la feature
     */
    getMetadataBoundingBox(metadata: Metadata, destinationProjection: string): Feature<Polygon> {
        if (metadata == null || metadata.geography == null || metadata.geography.bounding_box == null) {
            return null;
        }
        const boundingBox = metadata?.geography?.bounding_box;

        // Une BBOX est toujours en WGS84/GPS
        return DisplayMapService.createBoundingBoxFeature(boundingBox, getTransform(GPS_PROJECTION, destinationProjection));
    }

    /**
     * Récupérer la distribution géographique des données sous forme de géométrie générique
     * @param metadata le JDD qui contient la distribution géographique
     * @param destinationProjection la projection d'affichage de la géométrie
     */
    getMetadataGeolocation(metadata: Metadata, destinationProjection: string): Geometry {
        if (metadata == null || metadata.geography == null || metadata.geography.geographic_distribution == null) {
            return null;
        }
        const geometrie = metadata?.geography?.geographic_distribution;
        const geometryParsed = geoJSON.readGeometry(geometrie);

        const epsgCode = DisplayMapService.getEpsgCode(metadata.geography.projection);
        if (epsgCode == null) {
            return null;
        }

        // Si projection non gérée, on fait rien
        if (get(epsgCode) == null) {
            return null;
        }

        return geometryParsed.transform(epsgCode, destinationProjection);
    }

    /**
     * Récupération des fonds de plan pour l'écran : Carte du détail d'un JDD
     */
    getDatasetBaseLayers(): Observable<LayerInformation[]> {
        return this.konsultService.getDatasetBaseLayers();
    }

    /**
     * Récupération des fonds plan pour l'écran : Mini-carte localisation des données
     */
    getLocalisationBaseLayers(): Observable<LayerInformation[]> {
        return this.konsultService.getLocalisationBaseLayers();

    }

    /**
     * Recherche d'adresse pour le centrage cartographique
     * @param input texte de recherche d'adresse
     */
    searchAddresses(input: string): Observable<SearchAutocompleteItem<Address>[]> {
        if (input != null && input !== '') {
            return this.konsultRvaService.searchAddresses(input).pipe(
                map((addresses: Address[]) =>
                    addresses.map((address: Address) => {
                        return {
                            label: address.label,
                            value: address,
                        };
                    })
                )
            );
        }

        return of([]);
    }

    /**
     * Récupération du contenu au format image pour un layer image
     * @param mediaUrl l'Url du média
     * @param src URL avec les paramètres d'appels (query params) vers le flux image carto
     */
    getImageLayerContent(mediaUrl: string, src: string): Observable<ArrayBuffer> {
        const queryParams = src.split('?')[1];
        const params: { [key: string]: string; } = {};
        const searchParams = new URLSearchParams(queryParams);
        searchParams.forEach((value: string, key: string) => {
            params[key] = value;
        });
        return this.konsultMetierService.callServiceMetadataMedia(mediaUrl, params).pipe(
            switchMap((response: any) => readFile(response.body)),
        );
    }

    /**
     * Récupération du contenu au format vectoriel pour un layer vectoriel
     * @param mediaUrl l'url du média
     * @param src URL avec les paramètres d'appels (query params) vers le flux image vectoriel
     */
    getFeatureLayerContent(mediaUrl: string, src: string): Observable<JSON> {
        const params: { [key: string]: string; } = {};
        const searchParams = new URLSearchParams(src);
        searchParams.forEach((value: string, key: string) => {
            params[key] = value;
        });
        return this.konsultMetierService.callServiceMetadataMedia(mediaUrl, params).pipe(
            switchMap((response: any) => this.toJson(response.body)),
        );
    }

    /**
     * Téléchargement d'un GeoJSON d'un média
     * @param mediaUrl URL du média
     */
    downloadGeojson(mediaUrl: string): Observable<JSON> {

        return this.konsultMetierService.callServiceMetadataMedia(mediaUrl).pipe(
            switchMap((response: any) => this.toJson(response.body)),
        );
    }

    /**
     * Récupération du endpoint vers le servive carto d'un JDD
     * @param globalId UUID du JDD
     * @param mediaId id de média
     */
    getServiceUrl(globalId: string, mediaId: string): string {
        const prefix = this.KONSULT_SERVICE_BASEPATH;
        return prefix + '/datasets/' + globalId + '/media/' + mediaId + '/call-service';
    }

    /**
     * Récupération d'une projection à l'aide d'EPSG.io pour affichage
     * @param projectionString chaîne de caractère qui représente la projection cherchée
     */
    registerAndGetProjection(projectionString: string): Observable<Projection> {

        return this.konsultService.getProj4Information(projectionString).pipe(
            map((proj4Information: Proj4Information) => {
                const proj4String = proj4Information.proj4;
                const epsgString = proj4Information.code;

                // ça c'est bbox, mais attention le format voulu par OL c'est [minlon, minlat, maxlon, maxlat]
                const worldExtent = [
                    proj4Information.bbox.west_longitude,
                    proj4Information.bbox.south_latitude,
                    proj4Information.bbox.east_longitude,
                    proj4Information.bbox.north_latitude
                ];

                const mapCenterTopleft = [worldExtent[0], worldExtent[1]];
                const mapCenterBottomRight = [worldExtent[2], worldExtent[3]];

                proj4.defs(epsgString, proj4String);
                register(proj4);
                const customProjection = get(epsgString);

                const topLeft = proj4(GPS_PROJECTION, epsgString, mapCenterTopleft);
                const bottomRight = proj4(GPS_PROJECTION, epsgString, mapCenterBottomRight);
                const worldExtent2 = boundingExtent([topLeft, bottomRight]);

                customProjection.setWorldExtent(worldExtent2);
                return customProjection;
            })
        );
    }

    /**
     * Conversion Blob (carto) vers GeoJSON
     * @param blob blob de données
     * @private
     */
    private toJson(blob: Blob): Observable<JSON> {
        return readFile(blob).pipe(
            map((arrayBuffer: ArrayBuffer) => {
                const dec = new TextDecoder();
                return JSON.parse(JSON.stringify(dec.decode(arrayBuffer)));
            })
        );
    }
}
