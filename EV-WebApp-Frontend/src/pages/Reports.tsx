import React from 'react';
import { BarChart3, Users, Building2, Calendar, TrendingUp } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { useApi } from '@/hooks/useApi';
import apiService from '@/services/api';

const Reports: React.FC = () => {
  const { data: dashboardStats, loading, refetch } = useApi(() => apiService.getDashboardStats());

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-foreground">Reports & Analytics</h1>
        <p className="text-muted-foreground">System insights and performance metrics</p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Revenue</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">Rs. {(dashboardStats?.totalRevenue || 0).toLocaleString()}</div>
            <p className="text-xs text-muted-foreground">From backend data</p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Reports Dashboard</CardTitle>
          <CardDescription>Detailed analytics from your backend server</CardDescription>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="text-center py-12">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-4"></div>
              <p className="text-muted-foreground">Loading reports...</p>
            </div>
          ) : (
            <div className="text-center py-12">
              <BarChart3 className="h-16 w-16 text-muted-foreground mx-auto mb-4" />
              <h3 className="text-lg font-medium mb-2">Reports Available</h3>
              <p className="text-muted-foreground mb-4">
                Your backend server is connected and reports are being generated.
              </p>
              <Button variant="outline" onClick={refetch}>Refresh Data</Button>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default Reports;