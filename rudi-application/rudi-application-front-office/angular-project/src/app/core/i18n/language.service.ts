import {Injectable} from '@angular/core';
import {DictionaryEntry, Language, RichDictionaryEntry} from 'micro_service_modules/api-kaccess';

@Injectable({
    providedIn: 'root'
})
export class LanguageService {
    getCurrentLanguage(): Language {
        return Language.FrFr;
    }

    getTextForCurrentLanguage(entries: DictionaryEntry[]): string | undefined {
        if (!entries) {
            return undefined;
        }

        const entryForCurrentLanguage: DictionaryEntry = this.getEntryForCurrentLanguage(entries);
        if (entryForCurrentLanguage) {
            return entryForCurrentLanguage.text;
        } else {
            console.error(`No entry matching ${this.getCurrentLanguage()} among entries :`, entries);
            return undefined;
        }
    }

    private getEntryForCurrentLanguage(entries: DictionaryEntry[]): DictionaryEntry | undefined {
        if (!entries || entries.length == 0) {
            return undefined;
        }

        if (entries.length > 1) {
            const currentLanguage = this.getCurrentLanguage();
            for (const entry of entries) {
                if (entry.lang === currentLanguage) {
                    return entry;
                }
            }
        }

        return entries[0];
    }


    getRichTextForCurrentLanguage(entries: RichDictionaryEntry[]): string | undefined {
        if (!entries) {
            return undefined;
        }

        const entryForCurrentLanguage: RichDictionaryEntry = this.getRichEntryForCurrentLanguage(entries);
        if (entryForCurrentLanguage) {
            return entryForCurrentLanguage.html ?? entryForCurrentLanguage.text;
        } else {
            console.error(`No entry matching ${this.getCurrentLanguage()} among entries :`, entries);
            return undefined;
        }
    }

    private getRichEntryForCurrentLanguage(entries: RichDictionaryEntry[]): RichDictionaryEntry | undefined {
        if (!entries || entries.length == 0) {
            return undefined;
        }

        if (entries.length > 1) {
            const currentLanguage = this.getCurrentLanguage();
            for (const entry of entries) {
                if (entry.lang === currentLanguage) {
                    return entry;
                }
            }
        }

        return entries[0];
    }

}
