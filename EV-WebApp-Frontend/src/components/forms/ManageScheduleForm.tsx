import React, { useState } from 'react';
import { Clock, Save, Plus, Trash2, ToggleLeft, ToggleRight } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Input } from '@/components/ui/input';
import { useToast } from '@/hooks/use-toast';
import apiService from '@/services/api';
import { ChargingStation } from '@/types';

interface ScheduleSlot {
  id: string;
  day: string;
  startTime: string;
  endTime: string;
  availableSlots: number;
  isActive: boolean;
}

interface ManageScheduleFormProps {
  station: ChargingStation | null;
  isOpen: boolean;
  onClose: () => void;
  onScheduleUpdated: () => void;
}

const ManageScheduleForm: React.FC<ManageScheduleFormProps> = ({ station, isOpen, onClose, onScheduleUpdated }) => {
  const { toast } = useToast();
  const [isLoading, setIsLoading] = useState(false);
  
  // Initialize with mock schedule data
  const [scheduleSlots, setScheduleSlots] = useState<ScheduleSlot[]>([
    {
      id: '1',
      day: 'Monday',
      startTime: '06:00',
      endTime: '22:00',
      availableSlots: station?.totalSlots || 8,
      isActive: true,
    },
    {
      id: '2',
      day: 'Tuesday',
      startTime: '06:00',
      endTime: '22:00',
      availableSlots: station?.totalSlots || 8,
      isActive: true,
    },
    {
      id: '3',
      day: 'Wednesday',
      startTime: '06:00',
      endTime: '22:00',
      availableSlots: station?.totalSlots || 8,
      isActive: true,
    },
    {
      id: '4',
      day: 'Thursday',
      startTime: '06:00',
      endTime: '22:00',
      availableSlots: station?.totalSlots || 8,
      isActive: true,
    },
    {
      id: '5',
      day: 'Friday',
      startTime: '06:00',
      endTime: '22:00',
      availableSlots: station?.totalSlots || 8,
      isActive: true,
    },
    {
      id: '6',
      day: 'Saturday',
      startTime: '08:00',
      endTime: '20:00',
      availableSlots: station?.totalSlots || 8,
      isActive: true,
    },
    {
      id: '7',
      day: 'Sunday',
      startTime: '08:00',
      endTime: '20:00',
      availableSlots: station?.totalSlots || 8,
      isActive: false,
    },
  ]);

  const daysOfWeek = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
  const timeSlots = [];
  for (let hour = 0; hour < 24; hour++) {
    const time = hour.toString().padStart(2, '0') + ':00';
    timeSlots.push(time);
    if (hour < 23) {
      const halfTime = hour.toString().padStart(2, '0') + ':30';
      timeSlots.push(halfTime);
    }
  }

  const updateScheduleSlot = (id: string, field: keyof ScheduleSlot, value: any) => {
    setScheduleSlots(prev => prev.map(slot => 
      slot.id === id ? { ...slot, [field]: value } : slot
    ));
  };

  const toggleSlotActive = (id: string) => {
    setScheduleSlots(prev => prev.map(slot => 
      slot.id === id ? { ...slot, isActive: !slot.isActive } : slot
    ));
  };

  const addCustomSlot = () => {
    const newSlot: ScheduleSlot = {
      id: Date.now().toString(),
      day: 'Monday',
      startTime: '09:00',
      endTime: '17:00',
      availableSlots: station?.totalSlots || 8,
      isActive: true,
    };
    setScheduleSlots(prev => [...prev, newSlot]);
  };

  const removeCustomSlot = (id: string) => {
    setScheduleSlots(prev => prev.filter(slot => slot.id !== id));
  };
  
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    
    // Validate schedule slots
    const invalidSlots = scheduleSlots.filter(slot => {
      if (!slot.isActive) return false;
      return slot.startTime >= slot.endTime || slot.availableSlots < 0 || slot.availableSlots > (station?.totalSlots || 8);
    });

    if (invalidSlots.length > 0) {
      toast({
        title: "Validation Error",
        description: "Please check your schedule settings. Start time must be before end time and slots must be valid.",
        variant: "destructive",
      });
      setIsLoading(false);
      return;
    }
    
    try {
      if (!station) return;
      
      const response = await apiService.updateStationSchedule(station.id, scheduleSlots);
      
      toast({
        title: "Schedule Updated",
        description: `Schedule for ${station.name} has been updated successfully.`,
      });
      onScheduleUpdated();
      onClose();
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.message || "Failed to update schedule. Please try again.",
        variant: "destructive",
      });
    } finally {
      setIsLoading(false);
    }
  };

  const activeDays = scheduleSlots.filter(slot => slot.isActive).length;
  const averageHours = scheduleSlots
    .filter(slot => slot.isActive)
    .reduce((sum, slot) => {
      const start = parseInt(slot.startTime.split(':')[0]) + parseInt(slot.startTime.split(':')[1]) / 60;
      const end = parseInt(slot.endTime.split(':')[0]) + parseInt(slot.endTime.split(':')[1]) / 60;
      return sum + (end - start);
    }, 0) / Math.max(1, activeDays);

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-4xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center space-x-2">
            <Clock className="h-5 w-5" />
            <span>Manage Schedule - {station?.name}</span>
          </DialogTitle>
          <DialogDescription>
            Configure operating hours and slot availability for your charging station
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Schedule Overview */}
          <div className="grid gap-4 md:grid-cols-3">
            <Card>
              <CardHeader className="pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">Operating Days</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{activeDays}/7</div>
                <p className="text-xs text-muted-foreground">Days per week</p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">Average Hours</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{Math.round(averageHours)}h</div>
                <p className="text-xs text-muted-foreground">Per operating day</p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">Total Slots</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{station?.totalSlots || 8}</div>
                <p className="text-xs text-muted-foreground">Available slots</p>
              </CardContent>
            </Card>
          </div>

          {/* Weekly Schedule */}
          <Card>
            <CardHeader>
              <CardTitle>Weekly Schedule</CardTitle>
              <CardDescription>Configure operating hours and slot availability for each day</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              {scheduleSlots.map((slot) => (
                <div key={slot.id} className="p-4 border rounded-lg space-y-4">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-4">
                      <div className="w-20">
                        <Badge variant={slot.isActive ? 'default' : 'outline'}>
                          {slot.day}
                        </Badge>
                      </div>
                      <div className="flex items-center space-x-2">
                        <Label className="text-sm">Active:</Label>
                        <Switch
                          checked={slot.isActive}
                          onCheckedChange={() => toggleSlotActive(slot.id)}
                        />
                      </div>
                    </div>
                    
                    {/* Remove button for custom slots */}
                    {scheduleSlots.filter(s => s.day === slot.day).length > 1 && (
                      <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        onClick={() => removeCustomSlot(slot.id)}
                        className="text-red-600 hover:text-red-700"
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    )}
                  </div>

                  {slot.isActive && (
                    <div className="grid gap-4 md:grid-cols-4">
                      <div className="space-y-2">
                        <Label>Start Time</Label>
                        <Select
                          value={slot.startTime}
                          onValueChange={(value) => updateScheduleSlot(slot.id, 'startTime', value)}
                        >
                          <SelectTrigger>
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            {timeSlots.map(time => (
                              <SelectItem key={time} value={time}>{time}</SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </div>

                      <div className="space-y-2">
                        <Label>End Time</Label>
                        <Select
                          value={slot.endTime}
                          onValueChange={(value) => updateScheduleSlot(slot.id, 'endTime', value)}
                        >
                          <SelectTrigger>
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            {timeSlots.map(time => (
                              <SelectItem key={time} value={time}>{time}</SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </div>

                      <div className="space-y-2">
                        <Label>Available Slots</Label>
                        <Input
                          type="number"
                          min="0"
                          max={station?.totalSlots || 8}
                          value={slot.availableSlots}
                          onChange={(e) => updateScheduleSlot(slot.id, 'availableSlots', parseInt(e.target.value))}
                        />
                      </div>

                      <div className="space-y-2">
                        <Label>Duration</Label>
                        <div className="text-sm text-muted-foreground pt-2">
                          {(() => {
                            const start = parseInt(slot.startTime.split(':')[0]) + parseInt(slot.startTime.split(':')[1]) / 60;
                            const end = parseInt(slot.endTime.split(':')[0]) + parseInt(slot.endTime.split(':')[1]) / 60;
                            const duration = Math.max(0, end - start);
                            return `${duration.toFixed(1)} hours`;
                          })()}
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              ))}

              <Button
                type="button"
                variant="outline"
                onClick={addCustomSlot}
                className="w-full"
              >
                <Plus className="h-4 w-4 mr-2" />
                Add Custom Time Slot
              </Button>
            </CardContent>
          </Card>

          {/* Quick Actions */}
          <Card>
            <CardHeader>
              <CardTitle>Quick Actions</CardTitle>
              <CardDescription>Apply common schedule patterns</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid gap-2 md:grid-cols-4">
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => {
                    setScheduleSlots(prev => prev.map(slot => ({
                      ...slot,
                      isActive: true,
                      startTime: '00:00',
                      endTime: '23:59'
                    })));
                  }}
                >
                  24/7 Operation
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => {
                    setScheduleSlots(prev => prev.map(slot => ({
                      ...slot,
                      isActive: ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'].includes(slot.day),
                      startTime: '08:00',
                      endTime: '18:00'
                    })));
                  }}
                >
                  Business Hours
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => {
                    setScheduleSlots(prev => prev.map(slot => ({
                      ...slot,
                      isActive: ['Saturday', 'Sunday'].includes(slot.day),
                      startTime: '09:00',
                      endTime: '17:00'
                    })));
                  }}
                >
                  Weekends Only
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => {
                    setScheduleSlots(prev => prev.map(slot => ({
                      ...slot,
                      isActive: false
                    })));
                  }}
                >
                  Close All
                </Button>
              </div>
            </CardContent>
          </Card>

          {/* Form Actions */}
          <div className="flex justify-end space-x-2 pt-4 border-t">
            <Button type="button" variant="outline" onClick={onClose}>
              Cancel
            </Button>
            <Button type="submit" disabled={isLoading}>
              <Save className="h-4 w-4 mr-2" />
              {isLoading ? 'Updating...' : 'Update Schedule'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ManageScheduleForm;