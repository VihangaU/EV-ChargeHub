import React from 'react';
import { Building2, Calendar, DollarSign, Users, Zap, MapPin } from 'lucide-react';
import DashboardCard from '@/components/dashboard/DashboardCard';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { useApi } from '@/hooks/useApi';
import apiService from '@/services/api';

const StationOperatorDashboard: React.FC = () => {
  const { data: stats, loading: statsLoading } = useApi(() => apiService.getDashboardStats());
  const { data: activities, loading: activitiesLoading } = useApi(() => apiService.getDashboardActivities());

  const getStatusBadgeVariant = (status: string) => {
    switch (status) {
      case 'active':
      case 'approved':
      case 'completed':
        return 'default';
      case 'pending':
        return 'secondary';
      case 'cancelled':
      case 'inactive':
        return 'destructive';
      default:
        return 'outline';
    }
  };

  if (statsLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-4"></div>
          <p className="text-muted-foreground">Loading dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-foreground">Station Operator Dashboard</h1>
        <p className="text-muted-foreground">Manage your charging stations and bookings</p>
      </div>

      {/* Stats Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <DashboardCard
          title="My Stations"
          value={stats?.totalStations || 0}
          description="Active stations"
          icon={Building2}
          trend={{ value: 5, isPositive: true }}
        />
        <DashboardCard
          title="Total Bookings"
          value={stats?.totalBookings || 0}
          description="All time bookings"
          icon={Calendar}
          trend={{ value: 12, isPositive: true }}
        />
        <DashboardCard
          title="Revenue"
          value={`Rs. ${(stats?.totalRevenue || 0).toLocaleString()}`}
          description="Total earnings"
          icon={DollarSign}
          trend={{ value: 18, isPositive: true }}
        />
        <DashboardCard
          title="Available Slots"
          value={stats?.availableSlots || 0}
          description="Ready for booking"
          icon={Zap}
          trend={{ value: 0, isPositive: true }}
        />
      </div>

      {/* Active Bookings */}
      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <div>
            <CardTitle className="text-lg">Active Bookings</CardTitle>
            <CardDescription>Currently ongoing charging sessions</CardDescription>
          </div>
          <Users className="h-5 w-5 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div className="text-3xl font-bold">{stats?.activeBookings || 0}</div>
          <p className="text-sm text-muted-foreground">Sessions in progress or approved</p>
        </CardContent>
      </Card>

      {/* Recent Activities and Quick Actions */}
      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Recent Bookings</CardTitle>
            <CardDescription>Latest booking activities</CardDescription>
          </CardHeader>
          <CardContent>
            {activitiesLoading ? (
              <div className="text-center py-4">
                <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary mx-auto mb-2"></div>
                <p className="text-sm text-muted-foreground">Loading activities...</p>
              </div>
            ) : (
              <div className="space-y-4">
                {(activities || []).slice(0, 5).map((activity: any, index: number) => (
                  <div key={index} className="flex items-start space-x-3">
                    <div className="w-2 h-2 bg-primary rounded-full mt-2 flex-shrink-0"></div>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-medium text-foreground">
                        {activity.title}
                      </p>
                      <p className="text-sm text-muted-foreground">
                        {activity.description}
                      </p>
                      <p className="text-xs text-muted-foreground mt-1">
                        {new Date(activity.timestamp).toLocaleString()}
                      </p>
                    </div>
                    <Badge variant={getStatusBadgeVariant(activity.status)} className="text-xs">
                      {activity.status}
                    </Badge>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Quick Actions</CardTitle>
            <CardDescription>Manage your stations efficiently</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <Button className="w-full justify-start" variant="outline">
              <Building2 className="h-4 w-4 mr-2" />
              Manage Stations
            </Button>
            <Button className="w-full justify-start" variant="outline">
              <Calendar className="h-4 w-4 mr-2" />
              View Bookings
            </Button>
            <Button className="w-full justify-start" variant="outline">
              <Zap className="h-4 w-4 mr-2" />
              Update Schedule
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default StationOperatorDashboard;