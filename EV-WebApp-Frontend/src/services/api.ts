const API_BASE_URL = 'http://localhost:5001/api';

class ApiService {
  private baseURL: string;
  private token: string | null = null;

  constructor(baseURL: string = API_BASE_URL) {
    this.baseURL = baseURL;
    this.token = localStorage.getItem('evcharge_token');
  }

  private getHeaders(): HeadersInit {
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
    };

    if (this.token) {
      headers.Authorization = `Bearer ${this.token}`;
    }

    return headers;
  }

  setToken(token: string | null) {
    this.token = token;
    if (token) {
      localStorage.setItem('evcharge_token', token);
    } else {
      localStorage.removeItem('evcharge_token');
    }
  }

  async request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
    const url = `${this.baseURL}${endpoint}`;
    const config: RequestInit = {
      ...options,
      headers: {
        ...this.getHeaders(),
        ...options.headers,
      },
    };

    try {
      const response = await fetch(url, config);
      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || `HTTP error! status: ${response.status}`);
      }

      return data;
    } catch (error) {
      console.error('API request failed:', error);
      throw error;
    }
  }

  // Authentication endpoints
  async login(email: string, password: string) {
    return this.request<{
      message: string;
      token: string;
      user: any;
    }>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });
  }

  async register(userData: {
    email: string;
    password: string;
    name: string;
    nic: string;
    phone: string;
    address: string;
    vehicleModel: string;
    vehicleNumber: string;
  }) {
    return this.request<{
      message: string;
      token: string;
      user: any;
    }>('/auth/register', {
      method: 'POST',
      body: JSON.stringify(userData),
    });
  }

  async verifyToken() {
    return this.request<{ user: any }>('/auth/verify');
  }

  // User endpoints
  async getUsers(params?: { role?: string; status?: string }) {
    const query = params ? '?' + new URLSearchParams(params).toString() : '';
    return this.request<any[]>(`/users${query}`);
  }

  async getUserById(id: string) {
    return this.request<any>(`/users/${id}`);
  }

  async createUser(userData: any) {
    return this.request<{ message: string; user: any }>('/users', {
      method: 'POST',
      body: JSON.stringify(userData),
    });
  }

  async updateUser(id: string, userData: any) {
    return this.request<{ message: string; user: any }>(`/users/${id}`, {
      method: 'PUT',
      body: JSON.stringify(userData),
    });
  }

  async updateUserStatus(id: string, status: string) {
    return this.request<{ message: string; user: any }>(`/users/${id}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status }),
    });
  }

  async deleteUser(id: string) {
    return this.request<{ message: string }>(`/users/${id}`, {
      method: 'DELETE',
    });
  }

  // Station endpoints
  async getStations(params?: { operatorId?: string; status?: string; type?: string }) {
    const query = params ? '?' + new URLSearchParams(params).toString() : '';
    return this.request<any[]>(`/stations${query}`);
  }

  async getStationById(id: string) {
    return this.request<any>(`/stations/${id}`);
  }

  async createStation(stationData: any) {
    return this.request<{ message: string; station: any }>('/stations', {
      method: 'POST',
      body: JSON.stringify(stationData),
    });
  }

  async updateStation(id: string, stationData: any) {
    return this.request<{ message: string; station: any }>(`/stations/${id}`, {
      method: 'PUT',
      body: JSON.stringify(stationData),
    });
  }

  async updateStationSchedule(id: string, schedule: any[]) {
    return this.request<{ message: string; station: any }>(`/stations/${id}/schedule`, {
      method: 'PUT',
      body: JSON.stringify(schedule),
    });
  }

  async deleteStation(id: string) {
    return this.request<{ message: string }>(`/stations/${id}`, {
      method: 'DELETE',
    });
  }

  // Booking endpoints
  async getBookings(params?: { evOwnerId?: string; stationId?: string; status?: string }) {
    const query = params ? '?' + new URLSearchParams(params).toString() : '';
    return this.request<any[]>(`/bookings${query}`);
  }

  async getBookingById(id: string) {
    return this.request<any>(`/bookings/${id}`);
  }

  async createBooking(bookingData: any) {
    return this.request<{ message: string; booking: any }>('/bookings', {
      method: 'POST',
      body: JSON.stringify(bookingData),
    });
  }

  async updateBooking(id: string, bookingData: any) {
    return this.request<{ message: string; booking: any }>(`/bookings/${id}`, {
      method: 'PUT',
      body: JSON.stringify(bookingData),
    });
  }

  async updateBookingStatus(id: string, status: string) {
    return this.request<{ message: string; booking: any }>(`/bookings/${id}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status }),
    });
  }

  async deleteBooking(id: string) {
    return this.request<{ message: string }>(`/bookings/${id}`, {
      method: 'DELETE',
    });
  }

  // EV Owner endpoints
  async getEVOwners(params?: { status?: string }) {
    const query = params ? '?' + new URLSearchParams(params).toString() : '';
    return this.request<any[]>(`/evowners${query}`);
  }

  async getEVOwnerById(id: string) {
    return this.request<any>(`/evowners/${id}`);
  }

  async getMyProfile() {
    return this.request<any>('/evowners/profile/me');
  }

  async updateEVOwner(id: string, data: any) {
    return this.request<{ message: string; evOwner: any }>(`/evowners/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  }

  async updateEVOwnerStatus(id: string, status: string) {
    return this.request<{ message: string; evOwner: any }>(`/evowners/${id}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status }),
    });
  }

  async deleteEVOwner(id: string) {
    return this.request<{ message: string }>(`/evowners/${id}`, {
      method: 'DELETE',
    });
  }

  // Dashboard endpoints
  async getDashboardStats() {
    return this.request<any>('/dashboard/stats');
  }

  async getDashboardActivities() {
    return this.request<any[]>('/dashboard/activities');
  }
}

export const apiService = new ApiService();
export default apiService;