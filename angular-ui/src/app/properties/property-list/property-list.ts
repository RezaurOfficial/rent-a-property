import { Component, OnInit, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';

import { AuthService } from '../../core/auth.service';
import { Property } from '../../core/models';
import { PropertyService } from '../../core/property.service';
import { RentalService } from '../../core/rental.service';

@Component({
  selector: 'app-property-list',
  imports: [DecimalPipe],
  templateUrl: './property-list.html',
  styleUrl: './property-list.scss',
})
export class PropertyList implements OnInit {
  private readonly propertyService = inject(PropertyService);
  private readonly rentalService = inject(RentalService);
  protected readonly auth = inject(AuthService);

  protected readonly properties = signal<Property[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly message = signal<string | null>(null);
  protected readonly pendingId = signal<number | null>(null);

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.propertyService.getAll().subscribe({
      next: (properties) => {
        this.properties.set(properties);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load properties.');
        this.loading.set(false);
      },
    });
  }

  rent(property: Property): void {
    this.message.set(null);
    this.error.set(null);
    this.pendingId.set(property.id);

    this.rentalService.rent(property.id).subscribe({
      next: () => {
        this.pendingId.set(null);
        this.message.set(`Successfully rented "${property.title}".`);
        this.load();
      },
      error: (err) => {
        this.pendingId.set(null);
        if (err.status === 409) {
          this.error.set('This property is no longer available.');
        } else {
          this.error.set('Failed to rent property. Please try again.');
        }
      },
    });
  }
}
