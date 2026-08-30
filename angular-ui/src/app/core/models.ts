export type PropertyStatus = 'AVAILABLE' | 'RENTED';

export interface Property {
  id: number;
  title: string;
  description: string;
  address: string;
  city: string;
  pricePerMonth: number;
  bedrooms: number;
  status: PropertyStatus;
}

export type PropertyInput = Omit<Property, 'id' | 'status'>;

export type RentalStatus = 'ACTIVE' | 'CANCELLED';

export interface Rental {
  id: number;
  propertyId: number;
  userId: string;
  startDate: string;
  status: RentalStatus;
}

export interface UserSummary {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
}
