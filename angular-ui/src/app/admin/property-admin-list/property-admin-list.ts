import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Property } from '../../core/models';
import { PropertyService } from '../../core/property.service';

@Component({
  selector: 'app-property-admin-list',
  imports: [RouterLink],
  templateUrl: './property-admin-list.html',
  styleUrl: './property-admin-list.scss',
})
export class PropertyAdminList implements OnInit {
  private readonly propertyService = inject(PropertyService);

  protected readonly properties = signal<Property[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

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

  remove(property: Property): void {
    if (!confirm(`Delete "${property.title}"? This cannot be undone.`)) {
      return;
    }

    this.propertyService.delete(property.id).subscribe({
      next: () => this.load(),
      error: () => this.error.set('Failed to delete property.'),
    });
  }
}
