import React from 'react';
import { useAuth } from '@/contexts/AuthContext';
import BackofficeDashboard from './dashboards/BackofficeDashboard';
import StationOperatorDashboard from './dashboards/StationOperatorDashboard';
import EVOwnerDashboard from './dashboards/EVOwnerDashboard';

const Dashboard: React.FC = () => {
  const { user } = useAuth();

  if (!user) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-foreground mb-4">Access Denied</h2>
          <p className="text-muted-foreground">Please log in to access the dashboard.</p>
        </div>
      </div>
    );
  }

  switch (user.role) {
    case 'backoffice':
      return <BackofficeDashboard />;
    case 'station_operator':
      return <StationOperatorDashboard />;
    case 'ev_owner':
      return <EVOwnerDashboard />;
    default:
      return (
        <div className="flex items-center justify-center min-h-screen">
          <div className="text-center">
            <h2 className="text-2xl font-bold text-foreground mb-4">Unknown Role</h2>
            <p className="text-muted-foreground">Your user role is not recognized.</p>
          </div>
        </div>
      );
  }
};

export default Dashboard;