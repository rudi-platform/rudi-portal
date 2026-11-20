import {hasModifierKey} from '@angular/cdk/keycodes';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {MatDialogRef} from '@angular/material/dialog';
import {filter} from 'rxjs/operators';

export class DialogUtils {

    /**
     * Closes a dialog opened with disableClose when Escape is pressed, backdrop clicks still do not close it (RGAA 7.1, APG dialog).
     * Must be called in an injection context (e.g. the component constructor).
     *
     * @param dialogRef the dialog to close
     * @param result the value returned by the dialog, same as its Cancel button
     */
    static closeOnEscape<R>(dialogRef: MatDialogRef<unknown, R>, result: R = null): void {
        dialogRef.keydownEvents().pipe(
            filter(event => event.key === 'Escape' && !hasModifierKey(event)),
            takeUntilDestroyed()
        ).subscribe(event => {
            event.preventDefault();
            dialogRef.close(result);
        });
    }
}
