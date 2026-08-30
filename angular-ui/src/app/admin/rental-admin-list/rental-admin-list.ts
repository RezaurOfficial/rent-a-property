import { Component, OnInit, inject, signal } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { AuthApiService } from '../../core/auth-api.service';
import { Property, Rental, UserSummary } from '../../core/models';
import { PropertyService } from '../../core/property.service';
import { RentalService } from '../../core/rental.service';

interface RentalAdminRow {
  rental: Rental;
  property?: Property;
  user?: UserSummary;
}

@Component({
  selector: 'app-rental-admin-list',
  imports: [],
  templateUrl: './rental-admin-list.html',
  styleUrl: './rental-admin-list.scss',
})
export class RentalAdminList implements OnInit {
  private readonly rentalService = inject(RentalService);
  private readonly propertyService = inject(PropertyService);
  private readonly authApi = inject(AuthApiService);

  protected readonly rows = signal<RentalAdminRow[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.error.set(null);

    this.rentalService.getAll().subscribe({
      next: (rentals) => {
        if (rentals.length === 0) {
          this.rows.set([]);
          this.loading.set(false);
          return;
        }

        const rowRequests = rentals.map((rental) =>
          forkJoin({
            property: this.propertyService
              .getById(rental.propertyId)
              .pipe(catchError(() => of(undefined))),
            user: this.authApi.getUser(rental.userId).pipe(catchError(() => of(undefined))),
          }),
        );

        forkJoin(rowRequests).subscribe((results) => {
          this.rows.set(rentals.map((rental, i) => ({ rental, ...results[i] })));
          this.loading.set(false);
        });
      },
      error: () => {
        this.error.set('Failed to load rentals.');
        this.loading.set(false);
      },
    });
  }
}
