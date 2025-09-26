import React, { useState } from 'react';
import { Search, Calendar } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { useApi } from '@/hooks/useApi';
import apiService from '@/services/api';

const StationBookings: React.FC = () => {
  const { data: bookings, loading, refetch } = useApi(() => apiService.getBookings());

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-4"></div>
          <p className="text-muted-foreground">Loading bookings...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-foreground">Station Bookings</h1>
        <p className="text-muted-foreground">Manage bookings for your charging stations</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Booking Management</CardTitle>
          <CardDescription>Review and manage all booking requests from your backend</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="text-center py-12">
            <Calendar className="h-16 w-16 text-muted-foreground mx-auto mb-4" />
            <h3 className="text-lg font-medium mb-2">Bookings Ready</h3>
            <p className="text-muted-foreground mb-4">
              Found {(bookings || []).length} bookings from your backend server.
            </p>
            <Button variant="outline" onClick={refetch}>Refresh Bookings</Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default StationBookings;