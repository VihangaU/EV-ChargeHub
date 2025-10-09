import React, { useState } from 'react';
import { Plus, Search, Filter, MoreHorizontal, UserCheck, Building2, Edit, Shield, ShieldOff } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import AddUserForm from '@/components/forms/AddUserForm';
import EditUserForm from '@/components/forms/EditUserForm';
import { User } from '@/types';
import { useToast } from '@/hooks/use-toast';
import { useApi } from '@/hooks/useApi';
import apiService from '@/services/api';

const UserManagement: React.FC = () => {
  const { toast } = useToast();
  const { data: users, loading, error, refetch } = useApi<User[]>(() => apiService.getUsers());
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedRole, setSelectedRole] = useState<string>('all');
  const [isAddUserOpen, setIsAddUserOpen] = useState(false);
  const [isEditUserOpen, setIsEditUserOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);

  const filteredUsers = (users || []).filter(user => {
    const matchesSearch = user.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         user.email.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesRole = selectedRole === 'all' || user.role === selectedRole;
    return matchesSearch && matchesRole;
  });

  const handleUserAdded = () => {
    refetch();
    setIsAddUserOpen(false);
    toast({
      title: "User Added",
      description: "User has been successfully added.",
    });
  };

  const handleUserUpdated = () => {
    refetch();
    setIsEditUserOpen(false);
    toast({
      title: "User Updated", 
      description: "User has been successfully updated.",
    });
  };

  const handleUserStatusChange = async (userId: string) => {
    try {
      const user = users?.find(u => u.id === userId);
      if (!user) return;
      
      const newStatus = user.status === 'active' ? 'inactive' : 'active';
      await apiService.updateUserStatus(userId, newStatus);
      refetch();
      toast({
        title: "Status Updated",
        description: `User status changed to ${newStatus}.`,
      });
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to update user status.",
        variant: "destructive",
      });
    }
  };

  const handleEdit = (user: User) => {
    setSelectedUser(user);
    setIsEditUserOpen(true);
  };

  const getStatusBadgeVariant = (status: string) => {
    return status === 'active' ? 'default' : 'destructive';
  };

  const getRoleBadgeVariant = (role: string) => {
    switch (role) {
      case 'backoffice':
        return 'default';
      case 'station_operator':
        return 'secondary';
      case 'ev_owner':
        return 'outline';
      default:
        return 'outline';
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-4"></div>
          <p className="text-muted-foreground">Loading users...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center py-8">
        <p className="text-destructive mb-4">Error loading users: {error}</p>
        <Button onClick={refetch} variant="outline">Try Again</Button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-foreground">User Management</h1>
          <p className="text-muted-foreground">Manage system users and their permissions</p>
        </div>
        <Button onClick={() => setIsAddUserOpen(true)}>
          <Plus className="h-4 w-4 mr-2" />
          Add User
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Users</CardTitle>
          <CardDescription>
            Manage all system users including backoffice staff and station operators
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="flex flex-col sm:flex-row gap-4">
              <div className="relative flex-1">
                <Search className="h-4 w-4 absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground" />
                <Input
                  placeholder="Search users..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10"
                />
              </div>
              <select
                value={selectedRole}
                onChange={(e) => setSelectedRole(e.target.value)}
                className="px-3 py-2 border border-input bg-background rounded-md"
              >
                <option value="all">All Roles</option>
                <option value="backoffice">Backoffice</option>
                <option value="station_operator">Station Operator</option>
                <option value="ev_owner">EV Owner</option>
              </select>
            </div>

            <div className="border rounded-lg">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Name</TableHead>
                    <TableHead>NIC</TableHead>
                    <TableHead>Role</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Created</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredUsers.map((user) => (
                    <TableRow key={user.id}>
                      <TableCell className="font-medium">{user.name}</TableCell>
                      <TableCell>{user.email}</TableCell>
                      <TableCell>
                        <Badge variant={getRoleBadgeVariant(user.role)}>
                          {user.role.replace('_', ' ')}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <Badge variant={getStatusBadgeVariant(user.status)}>
                          {user.status}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        {new Date(user.createdAt).toLocaleDateString()}
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
                            <DropdownMenuItem onClick={() => handleEdit(user)}>
                              <Edit className="h-4 w-4 mr-2" />
                              Edit
                            </DropdownMenuItem>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem onClick={() => handleUserStatusChange(user.id)}>
                              {user.status === 'active' ? (
                                <>
                                  <ShieldOff className="h-4 w-4 mr-2" />
                                  Deactivate
                                </>
                              ) : (
                                <>
                                  <Shield className="h-4 w-4 mr-2" />
                                  Activate
                                </>
                              )}
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

      <AddUserForm 
        isOpen={isAddUserOpen}
        onClose={() => setIsAddUserOpen(false)}
        onUserAdded={handleUserAdded}
      />

      <EditUserForm
        isOpen={isEditUserOpen}
        onClose={() => setIsEditUserOpen(false)}
        user={selectedUser}
        onUserUpdated={handleUserUpdated}
      />
    </div>
  );
};

export default UserManagement;