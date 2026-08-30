import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { PropertyService } from '../../core/property.service';

@Component({
  selector: 'app-property-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './property-form.html',
  styleUrl: './property-form.scss',
})
export class PropertyForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly propertyService = inject(PropertyService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly propertyId = signal<number | null>(null);
  protected readonly editing = computed(() => this.propertyId() !== null);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    title: ['', Validators.required],
    description: ['', Validators.required],
    address: ['', Validators.required],
    city: ['', Validators.required],
    pricePerMonth: [0, [Validators.required, Validators.min(0)]],
    bedrooms: [1, [Validators.required, Validators.min(0)]],
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      return;
    }

    const id = Number(idParam);
    this.propertyId.set(id);
    this.loading.set(true);

    this.propertyService.getById(id).subscribe({
      next: (property) => {
        this.form.patchValue(property);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load property.');
        this.loading.set(false);
      },
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.error.set(null);

    const value = this.form.getRawValue();
    const id = this.propertyId();
    const request = id !== null ? this.propertyService.update(id, value) : this.propertyService.create(value);

    request.subscribe({
      next: () => {
        this.saving.set(false);
        void this.router.navigate(['/admin/properties']);
      },
      error: () => {
        this.saving.set(false);
        this.error.set('Failed to save property.');
      },
    });
  }
}
