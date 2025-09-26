export type UserRole = 'backoffice' | 'station_operator' | 'ev_owner';

export interface User {
  id: string;
  email: string;
  name: string;
  role: UserRole;
  status: 'active' | 'inactive';
  createdAt: string;
  updatedAt: string;
}

export interface ChargingStation {
  id: string;
  name: string;
  address: string;
  latitude: number;
  longitude: number;
  type: 'AC' | 'DC';
  totalSlots: number;
  availableSlots: number;
  operatorId: string;
  operatorName: string;
  status: 'active' | 'inactive';
  pricePerHour: number;
  amenities: string[];
  createdAt: string;
  updatedAt: string;
  schedule?: ScheduleSlot[];
}

export interface ScheduleSlot {
  id: string;
  day: string;
  startTime: string;
  endTime: string;
  availableSlots: number;
  isActive: boolean;
}

export interface EVOwner {
  id: string;
  nic: string;
  name: string;
  email: string;
  phone: string;
  address: string;
  vehicleModel: string;
  vehicleNumber: string;
  status: 'active' | 'inactive';
  createdAt: string;
  updatedAt: string;
}

export interface Booking {
  id: string;
  evOwnerId: string;
  evOwnerName: string;
  evOwnerNIC: string;
  stationId: string;
  stationName: string;
  stationAddress: string;
  reservationDate: string;
  startTime: string;
  endTime: string;
  duration: number;
  totalCost: number;
  status: 'pending' | 'approved' | 'cancelled' | 'completed' | 'in_progress';
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface DashboardStats {
  totalUsers?: number;
  totalStations?: number;
  totalBookings?: number;
  totalRevenue?: number;
  activeBookings?: number;
  availableSlots?: number;
}