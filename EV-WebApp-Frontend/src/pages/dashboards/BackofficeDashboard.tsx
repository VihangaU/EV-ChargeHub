import React from 'react';
import { Users, Building2, Calendar, DollarSign, UserCheck, UserX, Zap, TrendingUp } from 'lucide-react';
import DashboardCard from '@/components/dashboard/DashboardCard';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { useApi } from '@/hooks/useApi';
import apiService from '@/services/api';

const BackofficeDashboard: React.FC = () => {
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
        <h1 className="text-3xl font-bold text-foreground">Backoffice Dashboard</h1>
        <p className="text-muted-foreground">System overview and administration</p>
      </div>

      {/* Stats Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <DashboardCard
          title="Total Users"
          value={stats?.totalUsers || 0}
          description="System users"
          icon={Users}
          trend={{ value: 12, isPositive: true }}
        />
        <DashboardCard
          title="Charging Stations"
          value={stats?.totalStations || 0}
          description="Active stations"
          icon={Building2}
          trend={{ value: 8, isPositive: true }}
        />
        <DashboardCard
          title="Total Bookings"
          value={stats?.totalBookings || 0}
          description="All time bookings"
          icon={Calendar}
          trend={{ value: 15, isPositive: true }}
        />
        <DashboardCard
          title="Revenue"
          value={`Rs. ${(stats?.totalRevenue || 0).toLocaleString()}`}
          description="Total earnings"
          icon={DollarSign}
          trend={{ value: 25, isPositive: true }}
        />
      </div>

      {/* Additional Stats */}
      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Active Bookings</CardTitle>
            <Zap className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats?.activeBookings || 0}</div>
            <p className="text-xs text-muted-foreground">Currently in progress</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Available Slots</CardTitle>
            <UserCheck className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats?.availableSlots || 0}</div>
            <p className="text-xs text-muted-foreground">Across all stations</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">EV Owners</CardTitle>
            <UserX className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats?.totalEVOwners || 0}</div>
            <p className="text-xs text-muted-foreground">Registered owners</p>
          </CardContent>
        </Card>
      </div>

      {/* Recent Activities */}
      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Recent Activities</CardTitle>
            <CardDescription>Latest system activities</CardDescription>
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
            <CardDescription>Common administrative tasks</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <Button className="w-full justify-start" variant="outline">
              <Users className="h-4 w-4 mr-2" />
              Manage Users
            </Button>
            <Button className="w-full justify-start" variant="outline">
              <Building2 className="h-4 w-4 mr-2" />
              Station Overview
            </Button>
            <Button className="w-full justify-start" variant="outline">
              <Calendar className="h-4 w-4 mr-2" />
              Booking Reports
            </Button>
            <Button className="w-full justify-start" variant="outline">
              <TrendingUp className="h-4 w-4 mr-2" />
              Analytics
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default BackofficeDashboard;