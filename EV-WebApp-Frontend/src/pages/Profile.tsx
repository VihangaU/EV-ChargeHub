import React, { useState } from 'react';
import { User, Mail, Phone, MapPin, Car, Edit2, Save, X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { useAuth } from '@/contexts/AuthContext';
import { useApi } from '@/hooks/useApi';
import apiService from '@/services/api';
import { useToast } from '@/hooks/use-toast';

const Profile: React.FC = () => {
  const { user } = useAuth();
  const { toast } = useToast();
  const { data: profile, loading, refetch } = useApi(() => apiService.getMyProfile());
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    phone: '',
    address: '',
    vehicleModel: '',
    vehicleNumber: ''
  });

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData(prev => ({
      ...prev,
      [e.target.name]: e.target.value
    }));
  };

  const handleSave = async () => {
    try {
      if (profile?.id) {
        await apiService.updateEVOwner(profile.id, formData);
        refetch();
        setIsEditing(false);
        toast({
          title: "Profile Updated",
          description: "Your profile has been updated successfully.",
        });
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to update profile.",
        variant: "destructive",
      });
    }
  };

  const handleEdit = () => {
    if (profile) {
      setFormData({
        name: profile.name || '',
        phone: profile.phone || '',
        address: profile.address || '',
        vehicleModel: profile.vehicleModel || '',
        vehicleNumber: profile.vehicleNumber || ''
      });
    }
    setIsEditing(true);
  };

  const handleCancel = () => {
    setIsEditing(false);
    setFormData({
      name: '',
      phone: '',
      address: '',
      vehicleModel: '',
      vehicleNumber: ''
    });
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-4"></div>
          <p className="text-muted-foreground">Loading profile...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-foreground">My Profile</h1>
          <p className="text-muted-foreground">Manage your personal information and vehicle details</p>
        </div>
        {!isEditing ? (
          <Button onClick={handleEdit}>
            <Edit2 className="h-4 w-4 mr-2" />
            Edit Profile
          </Button>
        ) : (
          <div className="flex space-x-2">
            <Button onClick={handleSave}>
              <Save className="h-4 w-4 mr-2" />
              Save
            </Button>
            <Button variant="outline" onClick={handleCancel}>
              <X className="h-4 w-4 mr-2" />
              Cancel
            </Button>
          </div>
        )}
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        {/* Personal Information */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center">
              <User className="h-5 w-5 mr-2" />
              Personal Information
            </CardTitle>
            <CardDescription>Your basic personal details</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="name">Full Name</Label>
              {isEditing ? (
                <Input
                  id="name"
                  name="name"
                  value={formData.name}
                  onChange={handleInputChange}
                  placeholder="Enter your full name"
                />
              ) : (
                <div className="px-3 py-2 bg-muted rounded-md">
                  {profile?.name || 'Not provided'}
                </div>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="email">Email Address</Label>
              <div className="px-3 py-2 bg-muted rounded-md flex items-center">
                <Mail className="h-4 w-4 mr-2 text-muted-foreground" />
                {profile?.email || user?.email || 'Not provided'}
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="phone">Phone Number</Label>
              {isEditing ? (
                <Input
                  id="phone"
                  name="phone"
                  value={formData.phone}
                  onChange={handleInputChange}
                  placeholder="Enter your phone number"
                />
              ) : (
                <div className="px-3 py-2 bg-muted rounded-md flex items-center">
                  <Phone className="h-4 w-4 mr-2 text-muted-foreground" />
                  {profile?.phone || 'Not provided'}
                </div>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="address">Address</Label>
              {isEditing ? (
                <Input
                  id="address"
                  name="address"
                  value={formData.address}
                  onChange={handleInputChange}
                  placeholder="Enter your address"
                />
              ) : (
                <div className="px-3 py-2 bg-muted rounded-md flex items-center">
                  <MapPin className="h-4 w-4 mr-2 text-muted-foreground" />
                  {profile?.address || 'Not provided'}
                </div>
              )}
            </div>

            {profile?.nic && (
              <div className="space-y-2">
                <Label htmlFor="nic">NIC Number</Label>
                <div className="px-3 py-2 bg-muted rounded-md">
                  {profile.nic}
                </div>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Vehicle Information */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center">
              <Car className="h-5 w-5 mr-2" />
              Vehicle Information
            </CardTitle>
            <CardDescription>Your electric vehicle details</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="vehicleModel">Vehicle Model</Label>
              {isEditing ? (
                <Input
                  id="vehicleModel"
                  name="vehicleModel"
                  value={formData.vehicleModel}
                  onChange={handleInputChange}
                  placeholder="e.g., Tesla Model 3"
                />
              ) : (
                <div className="px-3 py-2 bg-muted rounded-md">
                  {profile?.vehicleModel || 'Not provided'}
                </div>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="vehicleNumber">Vehicle Number</Label>
              {isEditing ? (
                <Input
                  id="vehicleNumber"
                  name="vehicleNumber"
                  value={formData.vehicleNumber}
                  onChange={handleInputChange}
                  placeholder="e.g., CAR-1234"
                />
              ) : (
                <div className="px-3 py-2 bg-muted rounded-md font-mono">
                  {profile?.vehicleNumber || 'Not provided'}
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      </div>

      {!profile && !loading && (
        <Card>
          <CardContent className="pt-6">
            <div className="text-center py-8">
              <User className="h-16 w-16 text-muted-foreground mx-auto mb-4" />
              <h3 className="text-lg font-medium mb-2">Profile Not Found</h3>
              <p className="text-muted-foreground">
                Your profile data will be available when you start the backend server.
              </p>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
};

export default Profile;