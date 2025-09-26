import React, { useState } from 'react';
import { Plus, Search, MoreHorizontal, Edit2, Calendar, MapPin, Zap } from 'lucide-react';
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
import AddStationForm from '@/components/forms/AddStationForm';
import EditStationForm from '@/components/forms/EditStationForm';
import ManageScheduleForm from '@/components/forms/ManageScheduleForm';
import { ChargingStation } from '@/types';
import { useApi } from '@/hooks/useApi';
import apiService from '@/services/api';
import { useAuth } from '@/contexts/AuthContext';
import { useToast } from '@/hooks/use-toast';

const MyStations: React.FC = () => {
  const { user } = useAuth();
  const { toast } = useToast();
  const { data: stations, loading, error, refetch } = useApi<ChargingStation[]>(() => 
    apiService.getStations({ operatorId: user?.id })
  );
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedType, setSelectedType] = useState<string>('all');
  const [isAddStationOpen, setIsAddStationOpen] = useState(false);
  const [isEditStationOpen, setIsEditStationOpen] = useState(false);
  const [isScheduleOpen, setIsScheduleOpen] = useState(false);
  const [selectedStation, setSelectedStation] = useState<ChargingStation | null>(null);

  const filteredStations = (stations || []).filter(station => {
    const matchesSearch = station.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         station.address.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesType = selectedType === 'all' || station.type === selectedType;
    return matchesSearch && matchesType;
  });

  const handleStationAdded = () => {
    refetch();
    setIsAddStationOpen(false);
    toast({
      title: "Station Added",
      description: "Station has been successfully added.",
    });
  };

  const handleStationUpdated = () => {
    refetch();
    setIsEditStationOpen(false);
    toast({
      title: "Station Updated",
      description: "Station has been successfully updated.",
    });
  };

  const handleScheduleUpdated = () => {
    refetch();
    setIsScheduleOpen(false);
    toast({
      title: "Schedule Updated",
      description: "Station schedule has been successfully updated.",
    });
  };

  const handleEdit = (station: ChargingStation) => {
    setSelectedStation(station);
    setIsEditStationOpen(true);
  };

  const handleManageSchedule = (station: ChargingStation) => {
    setSelectedStation(station);
    setIsScheduleOpen(true);
  };

  const getTypeBadgeVariant = (type: string) => {
    return type === 'DC' ? 'default' : 'secondary';
  };

  const getStatusBadgeVariant = (status: string) => {
    return status === 'active' ? 'default' : 'destructive';
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
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-foreground">My Stations</h1>
          <p className="text-muted-foreground">Manage your charging stations</p>
        </div>
        <Button onClick={() => setIsAddStationOpen(true)}>
          <Plus className="h-4 w-4 mr-2" />
          Add Station
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Charging Stations</CardTitle>
          <CardDescription>
            View and manage all your charging stations
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="flex flex-col sm:flex-row gap-4">
              <div className="relative flex-1">
                <Search className="h-4 w-4 absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground" />
                <Input
                  placeholder="Search stations..."
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

            <div className="border rounded-lg">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Station</TableHead>
                    <TableHead>Type</TableHead>
                    <TableHead>Slots</TableHead>
                    <TableHead>Price/Hour</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredStations.map((station) => (
                    <TableRow key={station.id}>
                      <TableCell>
                        <div>
                          <div className="font-medium">{station.name}</div>
                          <div className="text-sm text-muted-foreground flex items-center mt-1">
                            <MapPin className="h-3 w-3 mr-1" />
                            {station.address}
                          </div>
                        </div>
                      </TableCell>
                      <TableCell>
                        <Badge variant={getTypeBadgeVariant(station.type)}>
                          {station.type}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center space-x-2">
                          <Zap className="h-4 w-4 text-green-600" />
                          <span>{station.availableSlots}/{station.totalSlots}</span>
                        </div>
                      </TableCell>
                      <TableCell>Rs. {station.pricePerHour.toLocaleString()}</TableCell>
                      <TableCell>
                        <Badge variant={getStatusBadgeVariant(station.status)}>
                          {station.status}
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
                            <DropdownMenuItem onClick={() => handleEdit(station)}>
                              <Edit2 className="h-4 w-4 mr-2" />
                              Edit
                            </DropdownMenuItem>
                            <DropdownMenuItem onClick={() => handleManageSchedule(station)}>
                              <Calendar className="h-4 w-4 mr-2" />
                              Manage Schedule
                            </DropdownMenuItem>
                          </DropdownMenuContent>
                        </DropdownMenu>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          </div>
        </CardContent>
      </Card>

      <AddStationForm
        isOpen={isAddStationOpen}
        onClose={() => setIsAddStationOpen(false)}
        onStationAdded={handleStationAdded}
      />

      <EditStationForm
        isOpen={isEditStationOpen}
        onClose={() => setIsEditStationOpen(false)}
        station={selectedStation}
        onStationUpdated={handleStationUpdated}
      />

      <ManageScheduleForm
        isOpen={isScheduleOpen}
        onClose={() => setIsScheduleOpen(false)}
        station={selectedStation}
        onScheduleUpdated={handleScheduleUpdated}
      />
    </div>
  );
};

export default MyStations;