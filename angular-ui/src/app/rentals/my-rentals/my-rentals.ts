import { Component, OnInit, inject, signal } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { Property, Rental } from '../../core/models';
import { PropertyService } from '../../core/property.service';
import { RentalService } from '../../core/rental.service';

interface RentalRow {
  rental: Rental;
  property?: Property;
}

@Component({
  selector: 'app-my-rentals',
  imports: [],
  templateUrl: './my-rentals.html',
  styleUrl: './my-rentals.scss',
})
export class MyRentals implements OnInit {
  private readonly rentalService = inject(RentalService);
  private readonly propertyService = inject(PropertyService);

  protected readonly rows = signal<RentalRow[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly cancellingId = signal<number | null>(null);

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.error.set(null);

    this.rentalService.getMy().subscribe({
      next: (rentals) => {
        if (rentals.length === 0) {
          this.rows.set([]);
          this.loading.set(false);
          return;
        }

        const propertyRequests = rentals.map((rental) =>
          this.propertyService.getById(rental.propertyId).pipe(catchError(() => of(undefined))),
        );

        forkJoin(propertyRequests).subscribe((properties) => {
          this.rows.set(rentals.map((rental, i) => ({ rental, property: properties[i] })));
          this.loading.set(false);
        });
      },
      error: () => {
        this.error.set('Failed to load your rentals.');
        this.loading.set(false);
      },
    });
  }

  cancel(rental: Rental): void {
    this.cancellingId.set(rental.id);
    this.error.set(null);

    this.rentalService.cancel(rental.id).subscribe({
      next: () => {
        this.cancellingId.set(null);
        this.load();
      },
      error: () => {
        this.cancellingId.set(null);
        this.error.set('Failed to cancel rental.');
      },
    });
  }
}
