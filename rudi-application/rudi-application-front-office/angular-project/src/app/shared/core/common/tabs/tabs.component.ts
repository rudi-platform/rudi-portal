import { NgClass } from '@angular/common';
import {AfterViewInit, Component, ContentChild, ContentChildren, ElementRef, Inject, QueryList, ViewChild, ViewChildren, ViewContainerRef, DOCUMENT} from '@angular/core';
import {BreakpointObserverService, MediaSize} from '@core/services/breakpoint-observer.service';
import {TabComponent} from '@shared/core/common/tab/tab.component';
import {WorkInProgressComponent} from '@shared/core/common/work-in-progress/work-in-progress.component';
import {TabContentDirective} from '@shared/utils/directives/tab-content-directive/tab-content.directive';
import {TabsLayoutDirective} from '@shared/utils/directives/tab-layout-directive/tabs-layout.directive';
import {BannerComponent} from '../../banner/banner/banner.component';
import {TabComponent as TabComponent_1} from '../tab/tab.component';

@Component({
    selector: 'app-tabs',
    templateUrl: './tabs.component.html',
    styleUrls: ['./tabs.component.scss'],
    imports: [BannerComponent, TabComponent_1, NgClass]
})
export class TabsComponent implements AfterViewInit {

    private static nextId = 0;

    /** Id kept as is: used by the component styles */
    readonly tabPanelId = 'tab-content-container';

    private readonly tabIdPrefix = `app-tabs-${TabsComponent.nextId++}-tab-`;

    @ContentChildren(TabComponent)
    tabs: QueryList<TabComponent>;

    @ViewChildren(TabComponent_1, {read: ElementRef})
    renderedTabs: QueryList<ElementRef<HTMLElement>>;

    @ViewChild('defaultLayoutTabContent', {read: ViewContainerRef})
    defaultLayoutTabContent: ViewContainerRef;

    /**
     * Layout du contenu des onglets sous la bannière app-banner.
     * Si absent, alors le contenu des onglets prend toute la place sous la bannière.
     */
    @ContentChild(TabsLayoutDirective)
    customLayout: TabsLayoutDirective;

    /**
     * Contenu de l'onglet sélectionné, lorsqu'on a spécifié un {@link customLayout}.
     */
    @ContentChild(TabContentDirective)
    customLayoutTabContent: TabContentDirective;
    mediaSize: MediaSize;

    get selectedTab(): TabComponent {
        return this.tabs
            .find((tab) => tab.active);
    }

    constructor(
        private readonly breakpointObserverService: BreakpointObserverService,
        @Inject(DOCUMENT) private readonly doc: Document
    ) {
        this.mediaSize = this.breakpointObserverService.getMediaSize();
    }

    private static displayTabInto(tab: TabComponent, viewContainer: ViewContainerRef): void {
        viewContainer.clear();
        viewContainer.createEmbeddedView(tab.templateRef);
    }

    ngAfterViewInit(): void {
        if (!this.selectedTab) {
            // setTimeout pour éviter l'erreur ExpressionChangedAfterItHasBeenCheckedError
            // tslint:disable-next-line:max-line-length
            // source : https://indepth.dev/posts/1001/everything-you-need-to-know-about-the-expressionchangedafterithasbeencheckederror-error#asynchronous-update)
            globalThis.setTimeout(() => {
                this.selectTab(this.tabs.first);
            });
        }
    }

    selectTab(tabToSelect: TabComponent): void {
        if (this.isSelectable(tabToSelect)) {
            this.tabs.toArray().forEach(tab => tab.active = false);
            tabToSelect.active = true;
            this.displayTab(tabToSelect);
        }
    }

    private isSelectable(tabToSelect: TabComponent): boolean {
        return tabToSelect !== this.selectedTab && !tabToSelect.disabled;
    }

    getTabId(index: number): string {
        return this.tabIdPrefix + index;
    }

    getSelectedTabId(): string {
        const index = this.tabs?.toArray().indexOf(this.selectedTab) ?? -1;
        return index >= 0 ? this.getTabId(index) : null;
    }

    /**
     * ARIA tabs pattern with manual activation: arrows, Home and End move the focus, Enter or Space selects
     */
    handleTabListKeydown(event: KeyboardEvent): void {
        const tabs = this.tabs.toArray();
        const rendered = this.renderedTabs.toArray();
        const currentIndex = rendered.findIndex(ref => ref.nativeElement.contains(event.target as Node));
        if (currentIndex < 0) {
            return;
        }
        const focusableIndexes = tabs
            .map((tab, index) => (tab.disabled || tab.invisible) ? -1 : index)
            .filter(index => index >= 0);
        if (focusableIndexes.length === 0) {
            return;
        }
        const position = focusableIndexes.indexOf(currentIndex);
        let targetIndex: number;
        switch (event.key) {
            case 'ArrowRight':
                targetIndex = focusableIndexes[(position + 1) % focusableIndexes.length];
                break;
            case 'ArrowLeft':
                targetIndex = focusableIndexes[(position - 1 + focusableIndexes.length) % focusableIndexes.length];
                break;
            case 'Home':
                targetIndex = focusableIndexes[0];
                break;
            case 'End':
                targetIndex = focusableIndexes.at(-1);
                break;
            default:
                return;
        }
        event.preventDefault();
        rendered[targetIndex]?.nativeElement.querySelector('button')?.focus();
    }

    private get viewContainerRef(): ViewContainerRef {
        if (this.customLayout) {
            if (this.customLayoutTabContent) {
                if (this.customLayoutTabContent.viewContainer) {
                    return this.customLayoutTabContent.viewContainer;
                }
            } else {
                throw new Error('Cannot find <ng-container appTabContent> child element in <ng-container appTabsLayout>');
            }
        }
        return this.defaultLayoutTabContent;
    }

    private displayTab(tab: TabComponent): void {
        if (tab.templateRef) {
            TabsComponent.displayTabInto(tab, this.viewContainerRef);
        } else { // On crée et affiche une instance de work-in-progress quand on n'a pas de composant spécifique à afficher
            this.displayWorkInProgressIntoCurrentTab();
        }
    }

    private displayWorkInProgressIntoCurrentTab() {
        this.viewContainerRef.clear();
        this.viewContainerRef.createComponent(WorkInProgressComponent, {
            projectableNodes: [[this.doc.createTextNode('')]]
        });

    }
}
