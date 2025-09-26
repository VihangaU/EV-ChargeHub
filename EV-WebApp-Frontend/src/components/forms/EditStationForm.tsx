import React, { useState } from 'react';
import { Edit, Save } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { useToast } from '@/hooks/use-toast';
import apiService from '@/services/api';
import { ChargingStation } from '@/types';

interface EditStationFormProps {
  station: ChargingStation | null;
  isOpen: boolean;
  onClose: () => void;
  onStationUpdated: () => void;
}

const EditStationForm: React.FC<EditStationFormProps> = ({ station, isOpen, onClose, onStationUpdated }) => {
  const { toast } = useToast();
  const [isLoading, setIsLoading] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    address: '',
    latitude: 0,
    longitude: 0,
    pricePerHour: 0
  });

  React.useEffect(() => {
    if (station) {
      setFormData({
        name: station.name,
        address: station.address,
        latitude: station.latitude,
        longitude: station.longitude,
        pricePerHour: station.pricePerHour
      });
    }
  }, [station]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'pricePerHour' || name === 'latitude' || name === 'longitude' 
        ? Number(value) 
        : value
    }));
  };
  
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!station) return;
    
    if (!formData.name || !formData.address || formData.pricePerHour < 0) {
      toast({
        title: "Validation Error",
        description: "Please fill in all required fields with valid values.",
        variant: "destructive",
      });
      return;
    }

    setIsLoading(true);
    
    try {
      const response = await apiService.updateStation(station.id, {
        name: formData.name,
        address: formData.address,
        latitude: formData.latitude,
        longitude: formData.longitude,
        pricePerHour: formData.pricePerHour
      });
      
      toast({
        title: "Station Updated",
        description: `${station.name} has been updated successfully.`,
      });
      onStationUpdated();
      onClose();
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.message || "Failed to update station. Please try again.",
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
          <DialogTitle>Edit Station</DialogTitle>
          <DialogDescription>Update station information</DialogDescription>
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
              <Save className="h-4 w-4 mr-2" />
              {isLoading ? 'Updating...' : 'Update Station'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default EditStationForm;