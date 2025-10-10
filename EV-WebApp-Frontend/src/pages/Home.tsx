import React from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, Car, Building2, Shield, Clock, MapPin, Smartphone } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

const Home: React.FC = () => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-light via-background to-secondary-light">
      {/* Header */}
      <header className="container mx-auto px-4 py-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
          <div className="flex items-center justify-center w-12 h-12 bg-gradient-to-br from-primary to-secondary rounded-xl shadow-lg">
            <span className="text-white font-bold text-xl">EV</span>
          </div>


            <div>
              <h1 className="text-2xl font-bold text-foreground">EV ChargeHub</h1>
              <p className="text-sm text-muted-foreground">Smart Charging Solutions</p>
            </div>
          </div>
          <div className="flex items-center space-x-4">
            <Link to="/login">
              <Button variant="outline">Sign In</Button>
            </Link>
            <Link to="/register">
              <Button>Get Started</Button>
            </Link>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="container mx-auto px-4 py-16 text-center">
        <div className="max-w-4xl mx-auto">
          <h2 className="text-5xl font-bold text-foreground mb-6">
            Power Your Journey with 
            <span className="text-primary"> Smart EV Charging</span>
          </h2>
          <p className="text-xl text-muted-foreground mb-8 max-w-2xl mx-auto">
            Discover, book, and manage electric vehicle charging stations across Sri Lanka. 
            Fast, reliable, and convenient charging solutions for your electric future.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link to="/register">
              <Button size="lg" className="text-lg px-8 py-3">
                Start Charging Today
                <ArrowRight className="ml-2 h-5 w-5" />
              </Button>
            </Link>
            <Link to="/login">
              <Button size="lg" variant="outline" className="text-lg px-8 py-3">
                Sign In to Your Account
              </Button>
            </Link>
          </div>
        </div>
      </section>

      {/* EV Images Section */}
      <section className="container mx-auto px-4 py-16">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-6xl mx-auto">
          <div className="relative overflow-hidden rounded-2xl shadow-lg">
            <div className="h-64 bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center">
              <Car className="h-24 w-24 text-white opacity-80" />
            </div>
            <div className="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent" />
            <div className="absolute bottom-4 left-4 text-white">
              <h3 className="text-xl font-semibold">Tesla Model 3</h3>
              <p className="text-sm opacity-90">Long Range Performance</p>
            </div>
          </div>
          <div className="relative overflow-hidden rounded-2xl shadow-lg">
            <div className="h-64 bg-gradient-to-br from-green-400 to-green-600 flex items-center justify-center">
              <Car className="h-24 w-24 text-white opacity-80" />
            </div>
            <div className="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent" />
            <div className="absolute bottom-4 left-4 text-white">
              <h3 className="text-xl font-semibold">Nissan Leaf</h3>
              <p className="text-sm opacity-90">Eco-Friendly Choice</p>
            </div>
          </div>
          <div className="relative overflow-hidden rounded-2xl shadow-lg">
            <div className="h-64 bg-gradient-to-br from-purple-400 to-purple-600 flex items-center justify-center">
              <Car className="h-24 w-24 text-white opacity-80" />
            </div>
            <div className="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent" />
            <div className="absolute bottom-4 left-4 text-white">
              <h3 className="text-xl font-semibold">BMW iX3</h3>
              <p className="text-sm opacity-90">Luxury Electric SUV</p>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="container mx-auto px-4 py-16">
        <div className="text-center mb-12">
          <h3 className="text-3xl font-bold text-foreground mb-4">Why Choose EV ChargeHub?</h3>
          <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
            Experience the future of electric vehicle charging with our comprehensive platform.
          </p>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 max-w-6xl mx-auto">
          <Card className="border-0 shadow-lg">
            <CardHeader>
              <div className="w-12 h-12 bg-primary/10 rounded-lg flex items-center justify-center mb-2">
                <MapPin className="h-6 w-6 text-primary" />
              </div>
              <CardTitle>Find Stations Nearby</CardTitle>
              <CardDescription>
                Locate charging stations across Sri Lanka with real-time availability and pricing.
              </CardDescription>
            </CardHeader>
          </Card>

          <Card className="border-0 shadow-lg">
            <CardHeader>
              <div className="w-12 h-12 bg-secondary/10 rounded-lg flex items-center justify-center mb-2">
                <Smartphone className="h-6 w-6 text-secondary" />
              </div>
              <CardTitle>Easy Booking</CardTitle>
              <CardDescription>
                Reserve your charging slot in advance with our simple booking system.
              </CardDescription>
            </CardHeader>
          </Card>

          <Card className="border-0 shadow-lg">
            <CardHeader>
              <div className="w-12 h-12 bg-primary/10 rounded-lg flex items-center justify-center mb-2">
                <Clock className="h-6 w-6 text-primary" />
              </div>
              <CardTitle>24/7 Availability</CardTitle>
              <CardDescription>
                Access charging stations round the clock with flexible scheduling options.
              </CardDescription>
            </CardHeader>
          </Card>

          <Card className="border-0 shadow-lg">
            <CardHeader>
            <div className="flex items-center justify-center w-12 h-12 bg-gradient-to-br from-primary to-secondary rounded-xl shadow-lg">
            <span className="text-white font-bold text-xl">EV</span>
          </div>
              <CardTitle>Fast Charging</CardTitle>
              <CardDescription>
                Both AC and DC charging options available for quick and efficient charging.
              </CardDescription>
            </CardHeader>
          </Card>

          <Card className="border-0 shadow-lg">
            <CardHeader>
              <div className="w-12 h-12 bg-primary/10 rounded-lg flex items-center justify-center mb-2">
                <Shield className="h-6 w-6 text-primary" />
              </div>
              <CardTitle>Secure Platform</CardTitle>
              <CardDescription>
                Your data and payments are protected with enterprise-grade security.
              </CardDescription>
            </CardHeader>
          </Card>

          <Card className="border-0 shadow-lg">
            <CardHeader>
              <div className="w-12 h-12 bg-secondary/10 rounded-lg flex items-center justify-center mb-2">
                <Building2 className="h-6 w-6 text-secondary" />
              </div>
              <CardTitle>Station Management</CardTitle>
              <CardDescription>
                Comprehensive tools for station operators to manage their charging infrastructure.
              </CardDescription>
            </CardHeader>
          </Card>
        </div>
      </section>

      {/* Call to Action */}
      <section className="container mx-auto px-4 py-16">
        <div className="bg-gradient-to-r from-primary to-secondary rounded-3xl p-8 md:p-12 text-center text-white">
          <h3 className="text-3xl font-bold mb-4">Ready to Start Your Electric Journey?</h3>
          <p className="text-lg mb-8 opacity-90 max-w-2xl mx-auto">
            Join thousands of EV owners who trust EV ChargeHub for their charging needs. 
            Sign up today and get access to our growing network of charging stations.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link to="/register">
              <Button size="lg" variant="secondary" className="text-lg px-8 py-3">
                Create Account
                <ArrowRight className="ml-2 h-5 w-5" />
              </Button>
            </Link>
            <Link to="/login">
              <Button size="lg" variant="outline" className="text-lg px-8 py-3 text-white border-white hover:bg-white hover:text-primary">
                Sign In
              </Button>
            </Link>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="container mx-auto px-4 py-8 border-t border-border/50">
        <div className="flex flex-col md:flex-row justify-between items-center">
          <div className="flex items-center space-x-3 mb-4 md:mb-0">
          <div className="flex items-center justify-center w-12 h-12 bg-gradient-to-br from-primary to-secondary rounded-xl shadow-lg">
            <span className="text-white font-bold text-xl">EV</span>
          </div>
            <span className="text-lg font-semibold text-foreground">EV ChargeHub</span>
          </div>
          <p className="text-sm text-muted-foreground">
            © 2024 EV ChargeHub. All rights reserved. Powering Sri Lanka's electric future.
          </p>
        </div>
      </footer>
    </div>
  );
};

export default Home;