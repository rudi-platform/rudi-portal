import {
    AfterViewInit,
    ChangeDetectorRef,
    Component,
    ElementRef,
    Input,
    NgZone,
    OnChanges,
    OnDestroy,
    SimpleChanges,
    ViewChild,
} from '@angular/core';
import {MatTooltip, TooltipPosition} from '@angular/material/tooltip';

/**
 * Composant réutilisable affichant un texte tronqué de manière flexible (aucune longueur maximale
 * fixe : la troncature s'appuie sur la hauteur réellement disponible à l'écran, calculée à partir
 * du nombre de lignes voulu et de la hauteur de ligne réelle, et s'adapte donc à la taille de la
 * card, à la police, au zoom, etc.).
 *
 * Respecte le pattern ARIA Tooltip (https://www.w3.org/WAI/ARIA/apg/patterns/tooltip/) grâce à
 * `MatTooltip` : déclenchement au survol ET au focus clavier, fermeture avec Echap, association du
 * texte complet via `aria-describedby`. L'élément est rendu focusable (`tabindex="0"`) uniquement
 * lorsqu'il est tronqué, pour ne pas ajouter d'arrêt de tabulation inutile sinon.
 * ```
 */
@Component({
    selector: 'app-truncated-text',
    templateUrl: './truncated-text.component.html',
    styleUrls: ['./truncated-text.component.scss'],
    imports: [MatTooltip],
    standalone: true
})
export class TruncatedTextComponent implements AfterViewInit, OnChanges, OnDestroy {
    private static readonly ELLIPSIS = '…';

    /** Texte complet à afficher (et à utiliser comme message de l'infobulle si tronqué). */
    @Input({required: true}) text: string;
    /** Nombre de lignes maximum affichées avant troncature. */
    @Input() lines = 1;
    /** Position de l'infobulle Material. */
    @Input() tooltipPosition: TooltipPosition = 'above';
    /**
     * When false, a truncated text is not a tab stop (e.g. inside a card already reachable through its link):
     * the tooltip stays on hover and the full text is exposed to screen readers through a visually hidden copy.
     */
    @Input() focusable = true;

    @ViewChild('textRef', {static: true}) private readonly textRef: ElementRef<HTMLElement>;

    protected isOverflowing = false;
    /** Texte réellement affiché : identique à `text`, ou tronqué et suffixé de "…" si trop long. */
    protected displayedText = '';
    /** Hauteur maximale (en px) autorisée avant troncature, calculée à partir de `lines`. */
    protected maxHeightPx: number | null = null;

    private resizeObserver?: ResizeObserver;

    constructor(
        private readonly ngZone: NgZone,
        private readonly changeDetectorRef: ChangeDetectorRef,
    ) {
    }

    ngAfterViewInit(): void {
        queueMicrotask(() => this.checkOverflow());
        if (typeof document !== 'undefined' && document.fonts !== undefined) {
            document.fonts.ready.then(() => this.checkOverflow()).catch(() => undefined);
        }
        this.ngZone.runOutsideAngular(() => {
            this.resizeObserver = new ResizeObserver(() => {
                this.ngZone.run(() => this.checkOverflow());
            });
            this.resizeObserver.observe(this.textRef.nativeElement);
        });
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['text']) {
            // Affichage immédiat (non tronqué) en attendant la mesure du débordement, pour éviter
            // tout texte vide le temps que la vue soit initialisée.
            this.displayedText = this.text;
            if (!changes['text'].firstChange) {
                // Laisse le DOM se mettre à jour avec le nouveau texte avant de mesurer le débordement.
                queueMicrotask(() => this.checkOverflow());
            }
        }
    }

    ngOnDestroy(): void {
        this.resizeObserver?.disconnect();
    }

    private checkOverflow(): void {
        const element = this.textRef?.nativeElement;
        if (!element) {
            return;
        }

        // Mesure toujours sur le texte complet : un affichage précédent a pu être tronqué, il ne
        // faut pas mesurer le débordement d'un texte déjà coupé.
        element.textContent = this.text;

        let lineHeight = Number.parseFloat(getComputedStyle(element).lineHeight);
        if (!lineHeight || Number.isNaN(lineHeight)) {

            lineHeight = Number.parseFloat(getComputedStyle(element).fontSize) * 1.2;
        }
        this.maxHeightPx = Math.round(lineHeight * this.lines);

        const overflowTolerancePx = Math.max(2, lineHeight * 0.3);
        this.isOverflowing = element.scrollHeight - this.maxHeightPx > overflowTolerancePx;

        this.displayedText = this.isOverflowing
            ? this.computeTruncatedText(element, this.maxHeightPx, overflowTolerancePx)
            : this.text;
        // Remet l'élément en cohérence avec la liaison Angular (`displayedText`) après les
        // manipulations directes du DOM faites ci-dessus pour la mesure.
        element.textContent = this.displayedText;
        this.changeDetectorRef.detectChanges();
    }

    /**
     * Recherche dichotomique de la plus grande portion du texte pouvant être affichée (suffixée de
     * "…") sans dépasser la hauteur maximale autorisée. Manipule temporairement le contenu de
     * `element` pour mesurer chaque candidat.
     */
    private computeTruncatedText(element: HTMLElement, maxHeightPx: number, tolerancePx: number): string {
        const fullText = this.text ?? '';
        let low = 0;
        let high = fullText.length;
        let result = TruncatedTextComponent.ELLIPSIS;

        while (low <= high) {
            const mid = Math.floor((low + high) / 2);
            const candidate = `${fullText.slice(0, mid).trimEnd()}${TruncatedTextComponent.ELLIPSIS}`;
            element.textContent = candidate;
            if (element.scrollHeight - maxHeightPx <= tolerancePx) {
                result = candidate;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return result;
    }
}











