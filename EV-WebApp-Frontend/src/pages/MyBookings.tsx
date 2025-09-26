import React, { useState } from 'react';
import { Search, Calendar, MapPin, Clock, DollarSign, MoreHorizontal, Edit2, X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import EditBookingForm from '@/components/forms/EditBookingForm';
import { Booking } from '@/types';
import { useApi } from '@/hooks/useApi';
import apiService from '@/services/api';
import { useToast } from '@/hooks/use-toast';

const MyBookings: React.FC = () => {
  const { toast } = useToast();
  const { data: bookings, loading, error, refetch } = useApi<Booking[]>(() => apiService.getBookings());
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedStatus, setSelectedStatus] = useState<string>('all');
  const [isEditBookingOpen, setIsEditBookingOpen] = useState(false);
  const [selectedBooking, setSelectedBooking] = useState<Booking | null>(null);

  const filteredBookings = (bookings || []).filter(booking => {
    const matchesSearch = booking.stationName.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         booking.stationAddress.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = selectedStatus === 'all' || booking.status === selectedStatus;
    return matchesSearch && matchesStatus;
  });

  const handleEditBooking = (booking: Booking) => {
    if (booking.status !== 'pending') {
      toast({
        title: "Cannot Edit",
        description: "Only pending bookings can be edited.",
        variant: "destructive",
      });
      return;
    }
    setSelectedBooking(booking);
    setIsEditBookingOpen(true);
  };

  const handleCancelBooking = async (bookingId: string) => {
    try {
      await apiService.updateBookingStatus(bookingId, 'cancelled');
      refetch();
      toast({
        title: "Booking Cancelled",
        description: "Your booking has been cancelled successfully.",
      });
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.message || "Failed to cancel booking.",
        variant: "destructive",
      });
    }
  };

  const handleBookingUpdated = () => {
    refetch();
    setIsEditBookingOpen(false);
    toast({
      title: "Booking Updated",
      description: "Your booking has been updated successfully.",
    });
  };

  const getStatusBadgeVariant = (status: string) => {
    switch (status) {
      case 'approved':
        return 'default';
      case 'pending':
        return 'secondary';
      case 'completed':
        return 'outline';
      case 'cancelled':
        return 'destructive';
      case 'in_progress':
        return 'default';
      default:
        return 'outline';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'approved':
        return '✓';
      case 'pending':
        return '⏳';
      case 'completed':
        return '✅';
      case 'cancelled':
        return '❌';
      case 'in_progress':
        return '⚡';
      default:
        return '?';
    }
  };

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

  if (error) {
    return (
      <div className="text-center py-8">
        <p className="text-destructive mb-4">Error loading bookings: {error}</p>
        <Button onClick={refetch} variant="outline">Try Again</Button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-foreground">My Bookings</h1>
        <p className="text-muted-foreground">Track and manage your charging session bookings</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Booking History</CardTitle>
          <CardDescription>
            View all your past and upcoming charging sessions
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="flex flex-col sm:flex-row gap-4">
              <div className="relative flex-1">
                <Search className="h-4 w-4 absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground" />
                <Input
                  placeholder="Search bookings..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10"
                />
              </div>
              <select
                value={selectedStatus}
                onChange={(e) => setSelectedStatus(e.target.value)}
                className="px-3 py-2 border border-input bg-background rounded-md"
              >
                <option value="all">All Status</option>
                <option value="pending">Pending</option>
                <option value="approved">Approved</option>
                <option value="in_progress">In Progress</option>
                <option value="completed">Completed</option>
                <option value="cancelled">Cancelled</option>
              </select>
            </div>

            <div className="border rounded-lg">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Station</TableHead>
                    <TableHead>Date & Time</TableHead>
                    <TableHead>Duration</TableHead>
                    <TableHead>Cost</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredBookings.map((booking) => (
                    <TableRow key={booking.id}>
                      <TableCell>
                        <div>
                          <div className="font-medium">{booking.stationName}</div>
                          <div className="text-sm text-muted-foreground flex items-center mt-1">
                            <MapPin className="h-3 w-3 mr-1" />
                            {booking.stationAddress}
                          </div>
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className="space-y-1">
                          <div className="flex items-center text-sm">
                            <Calendar className="h-3 w-3 mr-1" />
                            {booking.reservationDate}
                          </div>
                          <div className="flex items-center text-sm text-muted-foreground">
                            <Clock className="h-3 w-3 mr-1" />
                            {booking.startTime} - {booking.endTime}
                          </div>
                        </div>
                      </TableCell>
                      <TableCell>{booking.duration} hours</TableCell>
                      <TableCell>
                        <div className="flex items-center">
                          <DollarSign className="h-3 w-3 mr-1" />
                          Rs. {booking.totalCost.toLocaleString()}
                        </div>
                      </TableCell>
                      <TableCell>
                        <Badge variant={getStatusBadgeVariant(booking.status)}>
                          {getStatusIcon(booking.status)} {booking.status}
                        </Badge>
                      </TableCell>
                      <TableCell className="text-right">
                        <DropdownMenu>
                          <DropdownMenuTrigger asChild>
                            <Button variant="ghost" className="h-8 w-8 p-0">
                              <MoreHorizontal className="h-4 w-4" />
                            </Button>
                          </DropdownMenuTrigger>
                          <DropdownMenuContent align="end">
                            <DropdownMenuLabel>Actions</DropdownMenuLabel>
                            <DropdownMenuItem onClick={() => handleEditBooking(booking)}>
                              <Edit2 className="h-4 w-4 mr-2" />
                              Edit
                            </DropdownMenuItem>
                            <DropdownMenuSeparator />
                            {booking.status === 'pending' && (
                              <DropdownMenuItem 
                                onClick={() => handleCancelBooking(booking.id)}
                                className="text-destructive"
                              >
                                <X className="h-4 w-4 mr-2" />
                                Cancel
                              </DropdownMenuItem>
                            )}
                          </DropdownMenuContent>
                        </DropdownMenu>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>

            {filteredBookings.length === 0 && (
              <div className="text-center py-12">
                <Calendar className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="text-lg font-medium mb-2">No bookings found</h3>
                <p className="text-muted-foreground">
                  {selectedStatus === 'all' 
                    ? "You haven't made any bookings yet." 
                    : `No ${selectedStatus} bookings found.`}
                </p>
              </div>
            )}
          </div>
        </CardContent>
      </Card>

      <EditBookingForm
        isOpen={isEditBookingOpen}
        onClose={() => setIsEditBookingOpen(false)}
        booking={selectedBooking}
        onBookingUpdated={handleBookingUpdated}
      />
    </div>
  );
};

export default MyBookings;