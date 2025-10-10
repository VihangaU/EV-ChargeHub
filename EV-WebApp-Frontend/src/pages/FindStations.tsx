import React, { useState } from 'react';
import { Search, MapPin, Zap, DollarSign, Star, Navigation } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import BookStationForm from '@/components/forms/BookStationForm';
import { ChargingStation } from '@/types';
import { useApi } from '@/hooks/useApi';
import apiService from '@/services/api';
import { useToast } from '@/hooks/use-toast';

const FindStations: React.FC = () => {
  const { toast } = useToast();
  const { data: stations, loading, error, refetch } = useApi<ChargingStation[]>(() => 
    apiService.getStations({ status: 'active' })
  );
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedType, setSelectedType] = useState<string>('all');
  const [isBookingOpen, setIsBookingOpen] = useState(false);
  const [selectedStation, setSelectedStation] = useState<ChargingStation | null>(null);

  const filteredStations = (stations || []).filter(station => {
    const matchesSearch = station.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         station.address.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesType = selectedType === 'all' || station.type === selectedType;
    const hasAvailableSlots = station.availableSlots > 0;
    return matchesSearch && matchesType && hasAvailableSlots;
  });

  const handleBookStation = (station: ChargingStation) => {
    setSelectedStation(station);
    setIsBookingOpen(true);
  };

  const handleBookingComplete = () => {
    refetch();
    setIsBookingOpen(false);
    toast({
      title: "Booking Submitted",
      description: "Your booking request has been submitted for approval.",
    });
  };

  const getTypeBadgeVariant = (type: string) => {
    return type === 'DC' ? 'default' : 'secondary';
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-4"></div>
          <p className="text-muted-foreground">Loading stations...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center py-8">
        <p className="text-destructive mb-4">Error loading stations: {error}</p>
        <Button onClick={refetch} variant="outline">Try Again</Button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-foreground">Find Charging Stations</h1>
        <p className="text-muted-foreground">Discover and book available charging stations near you</p>
      </div>

      {/* Search and Filters */}
      <Card>
        <CardContent className="pt-6">
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="relative flex-1">
              <Search className="h-4 w-4 absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground" />
              <Input
                placeholder="Search by station name or location..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10"
              />
            </div>
            <select
              value={selectedType}
              onChange={(e) => setSelectedType(e.target.value)}
              className="px-3 py-2 border border-input bg-background rounded-md"
            >
              <option value="all">All Types</option>
              <option value="AC">AC Charging</option>
              <option value="DC">DC Fast Charging</option>
            </select>
          </div>
        </CardContent>
      </Card>

      {/* Stations Grid */}
      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        {filteredStations.map((station) => (
          <Card key={station.id} className="hover:shadow-lg transition-shadow">
            <CardHeader>
              <div className="flex items-start justify-between">
                <div>
                  <CardTitle className="text-lg">{station.name}</CardTitle>
                  <CardDescription className="flex items-center mt-1">
                    <MapPin className="h-4 w-4 mr-1" />
                    {station.address}
                  </CardDescription>
                </div>
                <Badge variant={getTypeBadgeVariant(station.type)}>
                  {station.type}
                </Badge>
              </div>
            </CardHeader>
            <CardContent className="space-y-4">
              {/* Station Stats */}
              <div className="grid grid-cols-2 gap-4">
                <div className="flex items-center space-x-2">
                  <Zap className="h-4 w-4 text-green-600" />
                  <div>
                    <div className="text-sm font-medium">{station.availableSlots}/{station.totalSlots}</div>
                    <div className="text-xs text-muted-foreground">Available Slots</div>
                  </div>
                </div>
                <div className="flex items-center space-x-2">
                  <DollarSign className="h-4 w-4 text-blue-600" />
                  <div>
                    <div className="text-sm font-medium">Rs. {station.pricePerHour}</div>
                    <div className="text-xs text-muted-foreground">Per Hour</div>
                  </div>
                </div>
              </div>

              {/* Amenities */}
              {station.amenities && station.amenities.length > 0 && (
                <div>
                  <div className="text-sm font-medium mb-2">Amenities</div>
                  <div className="flex flex-wrap gap-1">
                    {station.amenities.slice(0, 3).map((amenity, index) => (
                      <Badge key={index} variant="outline" className="text-xs">
                        {amenity}
                      </Badge>
                    ))}
                    {station.amenities.length > 3 && (
                      <Badge variant="outline" className="text-xs">
                        +{station.amenities.length - 3} more
                      </Badge>
                    )}
                  </div>
                </div>
              )}

              {/* Action Buttons */}
              <div className="flex space-x-2 pt-2">
                <Button 
                  onClick={() => handleBookStation(station)}
                  className="flex-1"
                  disabled={station.availableSlots === 0}
                >
                  {station.availableSlots === 0 ? 'No Slots Available' : 'Book Now'}
                </Button>
                <Button variant="outline" size="icon">
                  <Navigation className="h-4 w-4" />
                </Button>
              </div>

              {/* Operator Info */}
              <div className="text-xs text-muted-foreground border-t pt-2">
                Operated by {station.operatorName}
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {filteredStations.length === 0 && !loading && (
        <div className="text-center py-12">
          <div className="mx-auto w-24 h-24 bg-muted rounded-full flex items-center justify-center mb-4">
            <Search className="h-8 w-8 text-muted-foreground" />
          </div>
          <h3 className="text-lg font-medium text-foreground mb-2">No stations found</h3>
          <p className="text-muted-foreground mb-4">
            Try adjusting your search criteria or check back later.
          </p>
          <Button variant="outline" onClick={refetch}>
            Refresh Results
          </Button>
        </div>
      )}

      <BookStationForm
        isOpen={isBookingOpen}
        onClose={() => setIsBookingOpen(false)}
        station={selectedStation}
        onBookingCreated={handleBookingComplete}
      />
    </div>
  );
};

export default FindStations;