import React, { useState } from 'react';
import { Calendar, Clock, Save, DollarSign, MapPin } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Textarea } from '@/components/ui/textarea';
import { Badge } from '@/components/ui/badge';
import { useToast } from '@/hooks/use-toast';
import apiService from '@/services/api';
import { ChargingStation } from '@/types';

interface BookStationFormProps {
  station: ChargingStation | null;
  isOpen: boolean;
  onClose: () => void;
  onBookingCreated: () => void;
}

const BookStationForm: React.FC<BookStationFormProps> = ({ station, isOpen, onClose, onBookingCreated }) => {
  const { toast } = useToast();
  const [isLoading, setIsLoading] = useState(false);
  const [formData, setFormData] = useState({
    date: '',
    startTime: '',
    duration: '2',
    notes: '',
  });

  // Calculate min date (today) and max date (7 days from today)
  const today = new Date().toISOString().split('T')[0];
  const maxDate = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];

  // Calculate end time based on start time + duration
  const calculateEndTime = (startTime: string, duration: string) => {
    if (!startTime || !duration) return '';
    const [hours, minutes] = startTime.split(':').map(Number);
    const endHour = hours + parseInt(duration);
    const endMinutes = minutes;
    return `${endHour.toString().padStart(2, '0')}:${endMinutes.toString().padStart(2, '0')}`;
  };

  // Calculate total cost based on duration and station price
  const calculateCost = (duration: string) => {
    if (!duration || !station) return 0;
    return parseInt(duration) * station.pricePerHour;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    // Validation
    if (!formData.date || !formData.startTime || !formData.duration) {
      toast({
        title: "Validation Error",
        description: "Please fill in all required fields.",
        variant: "destructive",
      });
      setIsLoading(false);
      return;
    }

    // Check if booking date is within 7 days
    const bookingDate = new Date(formData.date);
    const todayDate = new Date();
    const diffTime = bookingDate.getTime() - todayDate.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays < 0 || diffDays > 7) {
      toast({
        title: "Invalid Date",
        description: "Reservation date must be within 7 days from today.",
        variant: "destructive",
      });
      setIsLoading(false);
      return;
    }
    
    try {
      if (!station) return;
      
      const endTime = calculateEndTime(formData.startTime, formData.duration);
      const totalCost = calculateCost(formData.duration);
      
      const response = await apiService.createBooking({
        stationId: station.id,
        reservationDate: formData.date,
        startTime: formData.startTime,
        endTime: endTime,
        duration: parseInt(formData.duration),
        totalCost: totalCost,
        notes: formData.notes
      });
      
      toast({
        title: "Booking Submitted",
        description: `Your booking at ${station.name} has been submitted for approval.`,
      });
      
      // Reset form
      setFormData({
        date: '',
        startTime: '',
        duration: '2',
        notes: '',
      });
      
      onBookingCreated();
      onClose();
    } catch (error: any) {
      toast({
        title: "Booking Failed",
        description: error.message || "Failed to create booking. Please try again.",
        variant: "destructive",
      });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Book Charging Station</DialogTitle>
          <DialogDescription>Reserve your charging session</DialogDescription>
        </DialogHeader>

        {station && (
          <div className="space-y-4">
            {/* Station Info */}
            <div className="p-3 bg-muted/50 rounded-lg">
              <h4 className="font-medium">{station.name}</h4>
              <p className="text-sm text-muted-foreground flex items-center">
                <MapPin className="h-3 w-3 mr-1" />
                {station.address}
              </p>
              <div className="flex items-center space-x-4 mt-2">
                <Badge variant={station.type === 'DC' ? 'default' : 'secondary'}>
                  {station.type} Charging
                </Badge>
                <span className="text-sm font-medium">
                  LKR {station.pricePerHour}/hour
                </span>
              </div>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="date">Reservation Date *</Label>
                <Input
                  id="date"
                  type="date"
                  min={today}
                  max={maxDate}
                  value={formData.date}
                  onChange={(e) => setFormData({...formData, date: e.target.value})}
                  required
                />
                <p className="text-xs text-muted-foreground">
                  Must be within 7 days from today
                </p>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="startTime">Start Time *</Label>
                  <Input
                    id="startTime"
                    type="time"
                    value={formData.startTime}
                    onChange={(e) => setFormData({...formData, startTime: e.target.value})}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="duration">Duration (hours) *</Label>
                  <Input
                    id="duration"
                    type="number"
                    min="1"
                    max="8"
                    value={formData.duration}
                    onChange={(e) => setFormData({...formData, duration: e.target.value})}
                    required
                  />
                </div>
              </div>

              {/* Booking Summary */}
              {formData.startTime && formData.duration && (
                <div className="p-3 bg-primary/10 rounded-lg space-y-2">
                  <h4 className="font-medium text-primary">Booking Summary</h4>
                  <div className="text-sm space-y-1">
                    <div className="flex justify-between">
                      <span>End Time:</span>
                      <span>{calculateEndTime(formData.startTime, formData.duration)}</span>
                    </div>
                    <div className="flex justify-between">
                      <span>Duration:</span>
                      <span>{formData.duration} hours</span>
                    </div>
                    <div className="flex justify-between font-medium">
                      <span>Total Cost:</span>
                      <span>LKR {calculateCost(formData.duration)}</span>
                    </div>
                  </div>
                </div>
              )}

              <div className="space-y-2">
                <Label htmlFor="notes">Additional Notes</Label>
                <Textarea
                  id="notes"
                  placeholder="Any special requirements or notes..."
                  value={formData.notes}
                  onChange={(e) => setFormData({...formData, notes: e.target.value})}
                  rows={3}
                />
              </div>

              <div className="flex justify-end space-x-2 pt-4">
                <Button type="button" variant="outline" onClick={onClose}>
                  Cancel
                </Button>
                <Button type="submit" disabled={isLoading}>
                  {isLoading ? 'Submitting...' : 'Submit Booking'}
                </Button>
              </div>
            </form>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default BookStationForm;