import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "@/contexts/AuthContext";
import ProtectedRoute from "@/components/ProtectedRoute";
import MainLayout from "@/components/layout/MainLayout";
import Home from "@/pages/Home";
import Login from "@/pages/Login";
import Register from "@/pages/Register";
import Dashboard from "@/pages/Dashboard";
import UserManagement from "@/pages/UserManagement";
import EVOwnersManagement from "@/pages/EVOwnersManagement";
import Reports from "@/pages/Reports";
import StationBookings from "@/pages/StationBookings";
import MyBookings from "@/pages/MyBookings";
import FindStations from "@/pages/FindStations";
import MyStations from "@/pages/MyStations";
import Profile from "@/pages/Profile";
import NotFound from "./pages/NotFound";

const queryClient = new QueryClient();

const App = () => (
  <QueryClientProvider client={queryClient}>
    <TooltipProvider>
      <AuthProvider>
        <Toaster />
        <Sonner />
        <BrowserRouter>
          <Routes>
            <Route path="/home" element={<Home />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/" element={<Navigate to="/home" replace />} />
            <Route path="/dashboard" element={
              <ProtectedRoute>
                <MainLayout />
              </ProtectedRoute>
            }>
              <Route index element={<Dashboard />} />
              <Route path="users" element={
                <ProtectedRoute allowedRoles={['backoffice']}>
                  <UserManagement />
                </ProtectedRoute>
              } />
              <Route path="ev-owners" element={
                <ProtectedRoute allowedRoles={['backoffice']}>
                  <EVOwnersManagement />
                </ProtectedRoute>
              } />
              <Route path="reports" element={
                <ProtectedRoute allowedRoles={['backoffice']}>
                  <Reports />
                </ProtectedRoute>
              } />
              <Route path="stations" element={
                <ProtectedRoute allowedRoles={['station_operator']}>
                  <MyStations />
                </ProtectedRoute>
              } />
              <Route path="station-bookings" element={
                <ProtectedRoute allowedRoles={['station_operator', 'backoffice']}>
                  <StationBookings />
                </ProtectedRoute>
              } />
              <Route path="my-bookings" element={
                <ProtectedRoute allowedRoles={['ev_owner']}>
                  <MyBookings />
                </ProtectedRoute>
              } />
              <Route path="find-stations" element={
                <ProtectedRoute allowedRoles={['ev_owner']}>
                  <FindStations />
                </ProtectedRoute>
              } />
              <Route path="profile" element={
                <ProtectedRoute allowedRoles={['ev_owner']}>
                  <Profile />
                </ProtectedRoute>
              } />
            </Route>
            <Route path="*" element={<NotFound />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </TooltipProvider>
  </QueryClientProvider>
);

export default App;
