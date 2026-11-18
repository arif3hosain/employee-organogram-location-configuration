import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

/** Shows the backend's Result.message to the user. */
@Injectable({ providedIn: 'root' })
export class NotifyService {
  private readonly snack = inject(MatSnackBar);

  ok(message: string): void {
    this.snack.open(message, 'OK', { duration: 3000, panelClass: 'snack-ok' });
  }

  error(message: string): void {
    this.snack.open(message, 'Close', { duration: 8000, panelClass: 'snack-error' });
  }
}
