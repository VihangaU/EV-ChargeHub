import React from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import {
  LayoutDashboard,
  Users,
  Building2,
  Car,
  Calendar,
  FileText,
  Settings,
  Zap,
  MapPin,
  BookOpen,
  BarChart3,
  UserPlus,
} from 'lucide-react';
import { useAuth } from '@/contexts/AuthContext';
import { cn } from '@/lib/utils';

interface NavItem {
  title: string;
  href: string;
  icon: React.ComponentType<{ className?: string }>;
  roles: string[];
}

const navItems: NavItem[] = [
  // Backoffice items
  {
    title: 'Dashboard',
    href: '/dashboard',
    icon: LayoutDashboard,
    roles: ['backoffice', 'station_operator', 'ev_owner'],
  },
  {
    title: 'User Management',
    href: '/dashboard/users',
    icon: Users,
    roles: ['backoffice'],
  },
  {
    title: 'EV Owners',
    href: '/dashboard/ev-owners',
    icon: Car,
    roles: ['backoffice'],
  },
  {
    title: 'Reports',
    href: '/dashboard/reports',
    icon: BarChart3,
    roles: ['backoffice'],
  },
  
  // Station Operator items
  {
    title: 'My Stations',
    href: '/dashboard/stations',
    icon: Building2,
    roles: ['station_operator'],
  },
  {
    title: 'Station Bookings',
    href: '/dashboard/station-bookings',
    icon: Calendar,
    roles: ['station_operator'],
  },

  // EV Owner items
  {
    title: 'Find Stations',
    href: '/dashboard/find-stations',
    icon: Zap,
    roles: ['ev_owner'],
  },
  {
    title: 'My Bookings',
    href: '/dashboard/my-bookings',
    icon: BookOpen,
    roles: ['ev_owner'],
  },
  {
    title: 'Profile',
    href: '/dashboard/profile',
    icon: UserPlus,
    roles: ['ev_owner'],
  },
];

const Sidebar: React.FC = () => {
  const { user } = useAuth();
  const location = useLocation();

  const filteredNavItems = navItems.filter(item =>
    user && item.roles.includes(user.role)
  );

  return (
    <div className="bg-card border-r border-border w-64 min-h-screen p-6">
      <div className="space-y-2">
        {filteredNavItems.map((item) => {
          const Icon = item.icon;
          const isActive = location.pathname === item.href;
          
          return (
            <NavLink
              key={item.href}
              to={item.href}
              className={cn(
                'flex items-center space-x-3 px-4 py-3 rounded-lg transition-all duration-200',
                isActive
                  ? 'bg-primary text-primary-foreground shadow-md'
                  : 'text-muted-foreground hover:text-foreground hover:bg-muted'
              )}
            >
              <Icon className="h-5 w-5" />
              <span className="font-medium">{item.title}</span>
            </NavLink>
          );
        })}
      </div>
    </div>
  );
};

export default Sidebar;