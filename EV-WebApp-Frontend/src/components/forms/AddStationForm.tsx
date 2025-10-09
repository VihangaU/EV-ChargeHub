import React, { useState, useRef, useEffect, useCallback } from 'react';
import { Building2, Save, MapPin, Search } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { useToast } from '@/hooks/use-toast';
import apiService from '@/services/api';
import { useAuth } from '@/contexts/AuthContext';
import { GoogleMap, Marker } from '@react-google-maps/api';
import { useJsApiLoader } from '@react-google-maps/api';

interface AddStationFormProps {
  isOpen: boolean;
  onClose: () => void;
  onStationAdded: () => void;
}

const AddStationForm: React.FC<AddStationFormProps> = ({ isOpen, onClose, onStationAdded }) => {
  const { toast } = useToast();
  const { user } = useAuth();
  const [isLoading, setIsLoading] = useState(false);
  const [showMapDialog, setShowMapDialog] = useState(false);
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
  const [mapCenter, setMapCenter] = useState({ lat: 6.9271, lng: 79.8612 }); // Default to Colombo, Sri Lanka
  const [selectedPosition, setSelectedPosition] = useState<google.maps.LatLngLiteral | null>(null);
  const searchInputRef = useRef<HTMLInputElement>(null);

  const { isLoaded, loadError } = useJsApiLoader({
    googleMapsApiKey: "AIzaSyCbuUW7FSLBWgaAn_1r92SIDp_Tk7W96lU",
    libraries: ['places']
  });

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'totalSlots' || name === 'pricePerHour' 
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

  const handleMapClick = (e: google.maps.MapMouseEvent) => {
    if (e.latLng) {
      const lat = e.latLng.lat();
      const lng = e.latLng.lng();
      setSelectedPosition({ lat, lng });
      setFormData(prev => ({
        ...prev,
        latitude: lat,
        longitude: lng
      }));
      setMapCenter({ lat, lng });
    }
  };

  const handleSearch = useCallback(() => {
    if (!isLoaded || !searchInputRef.current || !window.google) {
      return;
    }
    const query = searchInputRef.current.value.trim();
    if (!query) {
      return;
    }

    const service = new window.google.maps.places.PlacesService(
      document.createElement('div')
    );

    service.findPlaceFromQuery(
      {
        query,
        fields: ['name', 'geometry', 'formatted_address'],
      },
      (results, status) => {
        if (
          status === window.google.maps.places.PlacesServiceStatus.OK &&
          results &&
          results[0]
        ) {
          const place = results[0];
          if (place.geometry && place.geometry.location) {
            const lat = place.geometry.location.lat();
            const lng = place.geometry.location.lng();
            setSelectedPosition({ lat, lng });
            setFormData(prev => ({
              ...prev,
              latitude: lat,
              longitude: lng,
              address: place.formatted_address || prev.address
            }));
            setMapCenter({ lat, lng });
          }
        } else {
          toast({
            title: "Search Error",
            description: "No results found for the entered location.",
            variant: "destructive",
          });
        }
      }
    );
  }, [isLoaded, toast]);

  useEffect(() => {
    if (showMapDialog && isLoaded && searchInputRef.current && window.google) {
      const autocomplete = new window.google.maps.places.Autocomplete(searchInputRef.current);
      const listener = autocomplete.addListener('place_changed', () => {
        const place = autocomplete.getPlace();
        if (!place.geometry || !place.geometry.location) {
          return;
        }
        const lat = place.geometry.location.lat();
        const lng = place.geometry.location.lng();
        setSelectedPosition({ lat, lng });
        setFormData(prev => ({
          ...prev,
          latitude: lat,
          longitude: lng,
          address: place.formatted_address || prev.address
        }));
        setMapCenter({ lat, lng });
      });

      return () => {
        window.google.maps.event.removeListener(listener);
      };
    }
  }, [showMapDialog, isLoaded]);

  const openMapPicker = () => {
    setShowMapDialog(true);
  };

  const closeMapPicker = () => {
    setShowMapDialog(false);
    setSelectedPosition(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.name || !formData.address || !formData.latitude || !formData.longitude || formData.totalSlots < 1 || formData.pricePerHour < 0) {
      toast({
        title: "Validation Error",
        description: "Please fill in all required fields with valid values, including selecting a location on the map.",
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
          <div className="space-y-2">
            <Label>Location</Label>
            <Button 
              type="button" 
              variant="outline" 
              onClick={openMapPicker}
              disabled={isLoading}
              className="w-full justify-start"
            >
              <MapPin className="mr-2 h-4 w-4" />
              {formData.latitude && formData.longitude 
                ? `Selected: ${formData.latitude.toFixed(6)}, ${formData.longitude.toFixed(6)}`
                : 'Pick location on map'
              }
            </Button>
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

      {/* Map Dialog */}
      <Dialog open={showMapDialog} onOpenChange={setShowMapDialog}>
        <DialogContent className="max-w-4xl max-h-[90vh] flex flex-col">
          <DialogHeader>
            <DialogTitle>Pick Location on Map</DialogTitle>
            <DialogDescription>Search for a location or click on the map to select the station location.</DialogDescription>
          </DialogHeader>
          {loadError && <div className="text-red-500">Error loading Google Maps</div>}
          {!isLoaded && <div className="text-center py-4">Loading map...</div>}
          {isLoaded && (
            <>
              <div className="space-y-2 mb-4">
                <Label htmlFor="locationSearch">Search Location</Label>
                <div className="flex space-x-2">
                  <Input
                    ref={searchInputRef}
                    id="locationSearch"
                    placeholder="Enter address or location"
                  />
                  <Button type="button" onClick={handleSearch} size="sm">
                    <Search className="h-4 w-4" />
                  </Button>
                </div>
              </div>
              <div className="flex-1 relative">
                <GoogleMap
                  mapContainerStyle={{ width: '100%', height: '400px' }}
                  center={mapCenter}
                  zoom={12}
                  onClick={handleMapClick}
                >
                  {selectedPosition && <Marker position={selectedPosition} />}
                </GoogleMap>
              </div>
            </>
          )}
          <DialogFooter>
            <Button type="button" onClick={closeMapPicker}>Close</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </Dialog>
  );
};

export default AddStationForm;