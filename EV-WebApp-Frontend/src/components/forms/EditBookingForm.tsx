import React, { useState } from 'react';
import { Edit, Save } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { useToast } from '@/hooks/use-toast';
import apiService from '@/services/api';
import { Booking } from '@/types';

interface EditBookingFormProps {
  booking: Booking | null;
  isOpen: boolean;
  onClose: () => void;
  onBookingUpdated: () => void;
}

const EditBookingForm: React.FC<EditBookingFormProps> = ({ booking, isOpen, onClose, onBookingUpdated }) => {
  const { toast } = useToast();
  const [isLoading, setIsLoading] = useState(false);
  const [formData, setFormData] = useState({
    reservationDate: '',
    startTime: '',
    duration: 0,
    notes: ''
  });

  React.useEffect(() => {
    if (booking) {
      setFormData({
        reservationDate: booking.reservationDate,
        startTime: booking.startTime,
        duration: booking.duration,
        notes: booking.notes || ''
      });
    }
  }, [booking]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'duration' ? Number(value) : value
    }));
  };

  const calculateEndTime = (startTime: string, duration: number) => {
    if (!startTime || !duration) return '';
    const [hours, minutes] = startTime.split(':').map(Number);
    const endHour = hours + duration;
    const endMinutes = minutes;
    return `${endHour.toString().padStart(2, '0')}:${endMinutes.toString().padStart(2, '0')}`;
  };
  
   // Handle form submission (update booking)
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!booking) return;
    
    if (!formData.reservationDate || !formData.startTime || formData.duration < 1) {
      toast({
        title: "Validation Error",
        description: "Please fill in all required fields with valid values.",
        variant: "destructive",
      });
      return;
    }

    setIsLoading(true);
    
    try {
      // Calculate end time before update
      const endTime = calculateEndTime(formData.startTime, formData.duration);
      
      // API request to update booking
      const response = await apiService.updateBooking(booking.id, {
        reservationDate: formData.reservationDate,
        startTime: formData.startTime,
        endTime: endTime,
        duration: formData.duration,
        notes: formData.notes
      });
      
      toast({
        title: "Booking Updated",
        description: "Your booking has been updated successfully.",
      });
      onBookingUpdated();
      onClose();
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.message || "Failed to update booking. Please try again.",
        variant: "destructive",
      });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Edit Booking</DialogTitle>
          <DialogDescription>Modify your booking details</DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="reservationDate">Date</Label>
            <Input 
              id="reservationDate" 
              name="reservationDate"
              type="date" 
              value={formData.reservationDate}
              onChange={handleInputChange}
              disabled={isLoading}
              required 
            />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="startTime">Start Time</Label>
              <Input 
                id="startTime" 
                name="startTime"
                type="time" 
                value={formData.startTime}
                onChange={handleInputChange}
                disabled={isLoading}
                required 
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="duration">Duration (hours)</Label>
              <Input 
                id="duration" 
                name="duration"
                type="number" 
                min="1" 
                max="8" 
                value={formData.duration}
                onChange={handleInputChange}
                disabled={isLoading}
                required 
              />
            </div>
          </div>
          <div className="space-y-2">
            <Label htmlFor="notes">Notes</Label>
            <Input 
              id="notes" 
              name="notes"
              type="text"
              placeholder="Additional notes..."
              value={formData.notes}
              onChange={handleInputChange}
              disabled={isLoading}
            />
          </div>
          <div className="flex justify-end space-x-2">
            <Button type="button" variant="outline" onClick={onClose} disabled={isLoading}>Cancel</Button>
            <Button type="submit" disabled={isLoading}>
              <Save className="h-4 w-4 mr-2" />
              {isLoading ? 'Updating...' : 'Update Booking'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default EditBookingForm;