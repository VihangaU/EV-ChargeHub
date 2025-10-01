using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using MongoDB.Driver;
using System.Security.Claims;
using EV_WebApp_Backend.Models;
using EV_WebApp_Backend.Services;

namespace EV_WebApp_Backend.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class DashboardController : ControllerBase
{
    private readonly MongoDBService _mongoService;

    public DashboardController(MongoDBService mongoService)
    {
        _mongoService = mongoService;
    }

    // GET: api/dashboard/stats
    [HttpGet("stats")]
    public async Task<IActionResult> GetDashboardStats()
    {
        try
        {
            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            object stats = null;

            switch (userRole)
            {
                case "backoffice":
                    var users = _mongoService.GetCollection<User>("users");
                    var stations = _mongoService.GetCollection<Station>("stations");
                    var bookings = _mongoService.GetCollection<Booking>("bookings");
                    var evOwners = _mongoService.GetCollection<EVOwner>("evowners");

                    var totalUsers = await users.CountDocumentsAsync(FilterDefinition<User>.Empty);
                    var totalStations = await stations.CountDocumentsAsync(FilterDefinition<Station>.Empty);
                    var totalBookings = await bookings.CountDocumentsAsync(FilterDefinition<Booking>.Empty);
                    var totalEVOwners = await evOwners.CountDocumentsAsync(FilterDefinition<EVOwner>.Empty);

                    var activeBookingsFilter = Builders<Booking>.Filter.In(b => b.Status, new[] { "approved", "in_progress" });
                    var activeBookings = await bookings.CountDocumentsAsync(activeBookingsFilter);

                    var completedBookingsFilter = Builders<Booking>.Filter.Eq(b => b.Status, "completed");
                    var completedBookingsList = await bookings.Find(completedBookingsFilter).ToListAsync();
                    var totalRevenue = completedBookingsList.Sum(b => b.TotalCost);

                    var stationsList = await stations.Find(FilterDefinition<Station>.Empty).ToListAsync();
                    var availableSlots = stationsList.Sum(s => s.AvailableSlots);

                    stats = new
                    {
                        totalUsers,
                        totalStations,
                        totalBookings,
                        totalEVOwners,
                        activeBookings,
                        totalRevenue,
                        availableSlots
                    };
                    break;

                case "station_operator":
                    var operatorStations = await _mongoService.GetCollection<Station>("stations")
                        .Find(s => s.OperatorId == userId).ToListAsync();
                    var stationIds = operatorStations.Select(s => s.Id).ToList();

                    var operatorBookingsFilter = Builders<Booking>.Filter.In(b => b.StationId, stationIds);
                    var operatorBookings = await _mongoService.GetCollection<Booking>("bookings")
                        .CountDocumentsAsync(operatorBookingsFilter);

                    var operatorActiveBookingsFilter = operatorBookingsFilter &
                        Builders<Booking>.Filter.In(b => b.Status, new[] { "approved", "in_progress" });
                    var operatorActiveBookings = await _mongoService.GetCollection<Booking>("bookings")
                        .CountDocumentsAsync(operatorActiveBookingsFilter);

                    var operatorCompletedBookingsFilter = operatorBookingsFilter &
                        Builders<Booking>.Filter.Eq(b => b.Status, "completed");
                    var operatorCompletedBookings = await _mongoService.GetCollection<Booking>("bookings")
                        .Find(operatorCompletedBookingsFilter).ToListAsync();
                    var operatorRevenue = operatorCompletedBookings.Sum(b => b.TotalCost);

                    var operatorAvailableSlots = operatorStations.Sum(s => s.AvailableSlots);

                    stats = new
                    {
                        totalStations = operatorStations.Count,
                        totalBookings = operatorBookings,
                        activeBookings = operatorActiveBookings,
                        totalRevenue = operatorRevenue,
                        availableSlots = operatorAvailableSlots
                    };
                    break;

                case "ev_owner":
                    var evOwner = await _mongoService.GetCollection<EVOwner>("evowners")
                        .Find(e => e.UserId == userId).FirstOrDefaultAsync();

                    if (evOwner != null)
                    {
                        var userBookingsFilter = Builders<Booking>.Filter.Eq(b => b.EVOwnerId, evOwner.Id);
                        var userBookings = await _mongoService.GetCollection<Booking>("bookings")
                            .CountDocumentsAsync(userBookingsFilter);

                        var userActiveBookingsFilter = userBookingsFilter &
                            Builders<Booking>.Filter.In(b => b.Status, new[] { "approved", "in_progress" });
                        var userActiveBookings = await _mongoService.GetCollection<Booking>("bookings")
                            .CountDocumentsAsync(userActiveBookingsFilter);

                        var userCompletedBookingsFilter = userBookingsFilter &
                            Builders<Booking>.Filter.Eq(b => b.Status, "completed");
                        var userCompletedBookings = await _mongoService.GetCollection<Booking>("bookings")
                            .CountDocumentsAsync(userCompletedBookingsFilter);

                        var userCompletedBookingsList = await _mongoService.GetCollection<Booking>("bookings")
                            .Find(userCompletedBookingsFilter).ToListAsync();
                        var totalSpent = userCompletedBookingsList.Sum(b => b.TotalCost);

                        var activeStationsFilter = Builders<Station>.Filter.Eq(s => s.Status, "active");
                        var availableStations = await _mongoService.GetCollection<Station>("stations")
                            .CountDocumentsAsync(activeStationsFilter);

                        stats = new
                        {
                            totalBookings = userBookings,
                            activeBookings = userActiveBookings,
                            completedBookings = userCompletedBookings,
                            totalSpent,
                            availableStations
                        };
                    }
                    break;

                default:
                    return StatusCode(403, new { message = "Invalid user role" });
            }

            return Ok(stats);
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error fetching dashboard stats", error = ex.Message });
        }
    }

    // GET: api/dashboard/activities
    [HttpGet("activities")]
    public async Task<IActionResult> GetRecentActivities()
    {
        try
        {
            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            List<object> activities = new();

            switch (userRole)
            {
                case "backoffice":
                    var recentBookings = await _mongoService.GetCollection<Booking>("bookings")
                        .Find(FilterDefinition<Booking>.Empty)
                        .Sort(Builders<Booking>.Sort.Descending(b => b.CreatedAt))
                        .Limit(10)
                        .ToListAsync();

                    var recentStations = await _mongoService.GetCollection<Station>("stations")
                        .Find(FilterDefinition<Station>.Empty)
                        .Sort(Builders<Station>.Sort.Descending(s => s.CreatedAt))
                        .Limit(5)
                        .ToListAsync();

                    activities.AddRange(recentBookings.Select(b => new
                    {
                        type = "booking",
                        title = $"New booking by {b.EVOwnerName}",
                        description = $"Station: {b.StationName}",
                        timestamp = b.CreatedAt,
                        status = b.Status
                    }));

                    activities.AddRange(recentStations.Select(s => new
                    {
                        type = "station",
                        title = "New station registered",
                        description = $"{s.Name} - {s.Address}",
                        timestamp = s.CreatedAt,
                        status = s.Status
                    }));

                    activities = activities.OrderByDescending(a => ((dynamic)a).timestamp).Take(10).ToList();
                    break;

                case "station_operator":
                    var operatorStations = await _mongoService.GetCollection<Station>("stations")
                        .Find(s => s.OperatorId == userId).ToListAsync();
                    var stationIds = operatorStations.Select(s => s.Id).ToList();

                    var operatorBookings = await _mongoService.GetCollection<Booking>("bookings")
                        .Find(b => stationIds.Contains(b.StationId))
                        .Sort(Builders<Booking>.Sort.Descending(b => b.CreatedAt))
                        .Limit(10)
                        .ToListAsync();

                    activities = operatorBookings.Select(b => new
                    {
                        type = "booking",
                        title = $"Booking {b.Status}",
                        description = $"{b.EVOwnerName} - {b.StationName}",
                        timestamp = b.CreatedAt,
                        status = b.Status
                    }).ToList<object>();
                    break;

                case "ev_owner":
                    var evOwner = await _mongoService.GetCollection<EVOwner>("evowners")
                        .Find(e => e.UserId == userId).FirstOrDefaultAsync();

                    if (evOwner != null)
                    {
                        var userBookings = await _mongoService.GetCollection<Booking>("bookings")
                            .Find(b => b.EVOwnerId == evOwner.Id)
                            .Sort(Builders<Booking>.Sort.Descending(b => b.CreatedAt))
                            .Limit(10)
                            .ToListAsync();

                        activities = userBookings.Select(b => new
                        {
                            type = "booking",
                            title = $"Booking {b.Status}",
                            description = $"{b.StationName} - {b.ReservationDate}",
                            timestamp = b.CreatedAt,
                            status = b.Status
                        }).ToList<object>();
                    }
                    break;
            }

            return Ok(activities);
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error fetching activities", error = ex.Message });
        }
    }
}