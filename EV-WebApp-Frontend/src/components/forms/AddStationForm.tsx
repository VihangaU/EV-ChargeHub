import React, { useState } from 'react';
import { Building2, Save } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { useToast } from '@/hooks/use-toast';
import apiService from '@/services/api';
import { useAuth } from '@/contexts/AuthContext';

interface AddStationFormProps {
  isOpen: boolean;
  onClose: () => void;
  onStationAdded: () => void;
}

const AddStationForm: React.FC<AddStationFormProps> = ({ isOpen, onClose, onStationAdded }) => {
  const { toast } = useToast();
  const { user } = useAuth();
  const [isLoading, setIsLoading] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    address: '',
    latitude: 0,
    longitude: 0,
    type: 'AC' as 'AC' | 'DC',
    totalSlots: 1,
    pricePerHour: 0,
    amenities: [] as string[]
  });
  
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'totalSlots' || name === 'pricePerHour' || name === 'latitude' || name === 'longitude' 
        ? Number(value) 
        : value
    }));
  };

  const handleTypeChange = (value: string) => {
    setFormData(prev => ({
      ...prev,
      type: value as 'AC' | 'DC'
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.name || !formData.address || formData.totalSlots < 1 || formData.pricePerHour < 0) {
      toast({
        title: "Validation Error",
        description: "Please fill in all required fields with valid values.",
        variant: "destructive",
      });
      return;
    }

    setIsLoading(true);
    
    try {
      const response = await apiService.createStation({
        name: formData.name,
        address: formData.address,
        latitude: formData.latitude,
        longitude: formData.longitude,
        type: formData.type,
        totalSlots: formData.totalSlots,
        availableSlots: formData.totalSlots,
        pricePerHour: formData.pricePerHour,
        amenities: formData.amenities
      });
      
      toast({
        title: "Station Added",
        description: "Your charging station has been added successfully.",
      });
      
      // Reset form
      setFormData({
        name: '',
        address: '',
        latitude: 0,
        longitude: 0,
        type: 'AC',
        totalSlots: 1,
        pricePerHour: 0,
        amenities: []
      });
      
      onStationAdded();
      onClose();
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.message || "Failed to add station. Please try again.",
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
          <DialogTitle>Add New Station</DialogTitle>
          <DialogDescription>Add a new charging station to your network</DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="name">Station Name</Label>
            <Input 
              id="name" 
              name="name"
              value={formData.name}
              onChange={handleInputChange}
              disabled={isLoading}
              required 
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="address">Address</Label>
            <Input 
              id="address" 
              name="address"
              value={formData.address}
              onChange={handleInputChange}
              disabled={isLoading}
              required 
            />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="latitude">Latitude</Label>
              <Input 
                id="latitude" 
                name="latitude"
                type="number"
                step="0.000001"
                value={formData.latitude}
                onChange={handleInputChange}
                disabled={isLoading}
                placeholder="e.g., 6.9271"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="longitude">Longitude</Label>
              <Input 
                id="longitude" 
                name="longitude"
                type="number"
                step="0.000001"
                value={formData.longitude}
                onChange={handleInputChange}
                disabled={isLoading}
                placeholder="e.g., 79.8612"
              />
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label>Charging Type</Label>
              <Select value={formData.type} onValueChange={handleTypeChange} disabled={isLoading}>
                <SelectTrigger>
                  <SelectValue placeholder="Select type" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="AC">AC Charging</SelectItem>
                  <SelectItem value="DC">DC Fast Charging</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label htmlFor="totalSlots">Number of Slots</Label>
              <Input 
                id="totalSlots" 
                name="totalSlots"
                type="number" 
                min="1" 
                value={formData.totalSlots}
                onChange={handleInputChange}
                disabled={isLoading}
                required 
              />
            </div>
          </div>
          <div className="space-y-2">
            <Label htmlFor="pricePerHour">Price per Hour (Rs.)</Label>
            <Input 
              id="pricePerHour" 
              name="pricePerHour"
              type="number" 
              min="0"
              step="0.01"
              value={formData.pricePerHour}
              onChange={handleInputChange}
              disabled={isLoading}
              required 
            />
          </div>
          <div className="flex justify-end space-x-2">
            <Button type="button" variant="outline" onClick={onClose} disabled={isLoading}>Cancel</Button>
            <Button type="submit" disabled={isLoading}>
              {isLoading ? 'Adding...' : 'Add Station'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default AddStationForm;