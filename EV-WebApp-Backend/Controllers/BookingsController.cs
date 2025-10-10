using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using MongoDB.Driver;
using System.Security.Claims;
using EV_WebApp_Backend.Models;
using EV_WebApp_Backend.Services;
using EV_WebApp_Backend.DTOs;  // Add this line

namespace EV_WebApp_Backend.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class BookingsController : ControllerBase
{
    private readonly MongoDBService _mongoService;

    public BookingsController(MongoDBService mongoService)
    {
        _mongoService = mongoService;
    }

    // GET: api/bookings
    [HttpGet]
    public async Task<IActionResult> GetBookings(
        [FromQuery] string? evOwnerId,
        [FromQuery] string? stationId,
        [FromQuery] string? status,
        [FromQuery] string? operatorId)
    {
        try
        {
            var filterBuilder = Builders<Booking>.Filter.Empty;
            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;

            // Role-based filtering
            if (userRole == "ev_owner")
            {
                var evOwners = _mongoService.GetCollection<EVOwner>("evowners");
                var evOwner = await evOwners.Find(e => e.UserId == userId).FirstOrDefaultAsync();
                if (evOwner != null)
                {
                    filterBuilder &= Builders<Booking>.Filter.Eq(b => b.EVOwnerId, evOwner.Id);
                }
            }
            else if (userRole == "station_operator")
            {
                var stations = _mongoService.GetCollection<Station>("stations");
                var operatorStations = await stations.Find(s => s.OperatorId == userId).ToListAsync();
                var stationIds = operatorStations.Select(s => s.Id).ToList();
                filterBuilder &= Builders<Booking>.Filter.In(b => b.StationId, stationIds);
            }

            // Additional filters
            if (!string.IsNullOrEmpty(evOwnerId))
                filterBuilder &= Builders<Booking>.Filter.Eq(b => b.EVOwnerId, evOwnerId);
            if (!string.IsNullOrEmpty(stationId))
                filterBuilder &= Builders<Booking>.Filter.Eq(b => b.StationId, stationId);
            if (!string.IsNullOrEmpty(status))
                filterBuilder &= Builders<Booking>.Filter.Eq(b => b.Status, status);

            var bookings = _mongoService.GetCollection<Booking>("bookings");
            var bookingsList = await bookings.Find(filterBuilder)
                .Sort(Builders<Booking>.Sort.Descending(b => b.CreatedAt))
                .ToListAsync();

            return Ok(bookingsList);
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error fetching bookings", error = ex.Message });
        }
    }

    // GET: api/bookings/{id}
    [HttpGet("{id}")]
    public async Task<IActionResult> GetBookingById(string id)
    {
        try
        {
            var bookings = _mongoService.GetCollection<Booking>("bookings");
            var booking = await bookings.Find(b => b.Id == id).FirstOrDefaultAsync();

            if (booking == null)
            {
                return NotFound(new { message = "Booking not found" });
            }

            // Check access permissions
            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;

            var evOwners = _mongoService.GetCollection<EVOwner>("evowners");
            var evOwner = await evOwners.Find(e => e.UserId == userId).FirstOrDefaultAsync();

            var stations = _mongoService.GetCollection<Station>("stations");
            var station = await stations.Find(s => s.Id == booking.StationId).FirstOrDefaultAsync();

            var canAccess = userRole == "backoffice" ||
                           (userRole == "ev_owner" && evOwner?.Id == booking.EVOwnerId) ||
                           (userRole == "station_operator" && station?.OperatorId == userId);

            if (!canAccess)
            {
                return Forbid();
            }

            return Ok(booking);
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error fetching booking", error = ex.Message });
        }
    }

    // POST: api/bookings
    [HttpPost]
    public async Task<IActionResult> CreateBooking([FromBody] CreateBookingDto bookingDto)
    {
        try
        {
            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            if (userRole != "ev_owner")
            {
                return StatusCode(403, new { message = "Only EV owners can create bookings" });
            }

            // Add validation for reservation date (within 7 days)
            if (!DateTime.TryParse(bookingDto.ReservationDate, out DateTime reservationDate))
            {
                return BadRequest(new { message = "Invalid reservation date format" });
            }

            var daysDifference = (reservationDate.Date - DateTime.Now.Date).TotalDays;
            if (daysDifference < 0)
            {
                return BadRequest(new { message = "Reservation date cannot be in the past" });
            }
            if (daysDifference > 7)
            {
                return BadRequest(new { message = "Reservation date must be within 7 days from today" });
            }

            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            var evOwners = _mongoService.GetCollection<EVOwner>("evowners");
            var evOwner = await evOwners.Find(e => e.UserId == userId).FirstOrDefaultAsync();

            if (evOwner == null)
            {
                return NotFound(new { message = "EV Owner profile not found" });
            }

            var stations = _mongoService.GetCollection<Station>("stations");
            var station = await stations.Find(s => s.Id == bookingDto.StationId).FirstOrDefaultAsync();

            if (station == null)
            {
                return NotFound(new { message = "Station not found" });
            }

            if (station.AvailableSlots <= 0)
            {
                return BadRequest(new { message = "No available slots at this station" });
            }

            // Create booking with all required fields
            var booking = new Booking
            {
                StationId = bookingDto.StationId,
                ReservationDate = bookingDto.ReservationDate,
                StartTime = bookingDto.StartTime,
                EndTime = bookingDto.EndTime,
                Duration = bookingDto.Duration,
                TotalCost = bookingDto.TotalCost,
                Notes = bookingDto.Notes,

                // Backend-managed fields
                EVOwnerId = evOwner.Id,
                EVOwnerName = evOwner.Name,
                EVOwnerNIC = evOwner.NIC,
                StationName = station.Name,
                StationAddress = station.Address,
                Status = "pending",
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow
            };

            var bookings = _mongoService.GetCollection<Booking>("bookings");
            await bookings.InsertOneAsync(booking);

            // Update station availability
            var updateDefinition = Builders<Station>.Update.Inc(s => s.AvailableSlots, -1);
            await stations.UpdateOneAsync(s => s.Id == station.Id, updateDefinition);

            return CreatedAtAction(nameof(GetBookingById), new { id = booking.Id },
                new { message = "Booking created successfully", booking });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error creating booking", error = ex.Message });
        }
    }

    // PUT: api/bookings/{id}/status
    [HttpPut("{id}/status")]
    public async Task<IActionResult> UpdateBookingStatus(string id, [FromBody] BookingStatusUpdateDto statusUpdate)
    {
        try
        {
            var bookings = _mongoService.GetCollection<Booking>("bookings");
            var booking = await bookings.Find(b => b.Id == id).FirstOrDefaultAsync();

            if (booking == null)
            {
                return NotFound(new { message = "Booking not found" });
            }

            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;

            // Check permissions
            var stations = _mongoService.GetCollection<Station>("stations");
            var station = await stations.Find(s => s.Id == booking.StationId).FirstOrDefaultAsync();

            var evOwners = _mongoService.GetCollection<EVOwner>("evowners");
            var evOwner = await evOwners.Find(e => e.UserId == userId).FirstOrDefaultAsync();

            bool canUpdate = false;

            if (userRole == "backoffice")
            {
                canUpdate = true;
            }
            else if (userRole == "station_operator" && station?.OperatorId == userId)
            {
                canUpdate = new[] { "approved", "cancelled", "in_progress", "completed" }.Contains(statusUpdate.Status);
            }
            else if (userRole == "ev_owner" && booking.EVOwnerId == evOwner?.Id)
            {
                canUpdate = statusUpdate.Status == "cancelled" && booking.Status == "pending";
            }

            if (!canUpdate)
            {
                return StatusCode(403, new { message = "Access denied for this status update" });
            }

            string oldStatus = booking.Status;
            booking.Status = statusUpdate.Status;
            booking.UpdatedAt = DateTime.UtcNow;

            await bookings.ReplaceOneAsync(b => b.Id == id, booking);

            // Update station availability based on status change
            if ((oldStatus == "pending" && statusUpdate.Status == "cancelled") ||
                (oldStatus == "approved" && statusUpdate.Status == "cancelled"))
            {
                var updateDefinition = Builders<Station>.Update.Inc(s => s.AvailableSlots, 1);
                await stations.UpdateOneAsync(s => s.Id == booking.StationId, updateDefinition);
            }
            else if (statusUpdate.Status == "completed" && oldStatus != "completed" && oldStatus != "cancelled")
            {
                var updateDefinition = Builders<Station>.Update.Inc(s => s.AvailableSlots, 1);
                await stations.UpdateOneAsync(s => s.Id == booking.StationId, updateDefinition);
            }

            return Ok(new { message = "Booking status updated successfully", booking });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error updating booking status", error = ex.Message });
        }
    }

    // PUT: api/bookings/{id}
    [HttpPut("{id}")]
    public async Task<IActionResult> UpdateBooking(string id, [FromBody] UpdateBookingDto updateDto)
    {
        try
        {
            var bookings = _mongoService.GetCollection<Booking>("bookings");
            var existingBooking = await bookings.Find(b => b.Id == id).FirstOrDefaultAsync();

            if (existingBooking == null)
            {
                return NotFound(new { message = "Booking not found" });
            }

            // Check permissions
            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;

            var evOwners = _mongoService.GetCollection<EVOwner>("evowners");
            var evOwner = await evOwners.Find(e => e.UserId == userId).FirstOrDefaultAsync();

            var stations = _mongoService.GetCollection<Station>("stations");
            var station = await stations.Find(s => s.Id == existingBooking.StationId).FirstOrDefaultAsync();

            var canUpdate = userRole == "backoffice" ||
                           (userRole == "ev_owner" &&
                            existingBooking.EVOwnerId == evOwner?.Id &&
                            existingBooking.Status == "pending") ||
                           (userRole == "station_operator" &&
                            station?.OperatorId == userId);

            if (!canUpdate)
            {
                return StatusCode(403, new { message = "Access denied" });
            }

            // Update only the provided fields using MongoDB Update operations
            var updateDefinitionBuilder = Builders<Booking>.Update;
            var updates = new List<UpdateDefinition<Booking>>();

            if (!string.IsNullOrEmpty(updateDto.ReservationDate))
                updates.Add(updateDefinitionBuilder.Set(b => b.ReservationDate, updateDto.ReservationDate));

            if (!string.IsNullOrEmpty(updateDto.StartTime))
                updates.Add(updateDefinitionBuilder.Set(b => b.StartTime, updateDto.StartTime));

            if (!string.IsNullOrEmpty(updateDto.EndTime))
                updates.Add(updateDefinitionBuilder.Set(b => b.EndTime, updateDto.EndTime));

            if (updateDto.Duration > 0)
                updates.Add(updateDefinitionBuilder.Set(b => b.Duration, updateDto.Duration));

            if (updateDto.TotalCost > 0)
                updates.Add(updateDefinitionBuilder.Set(b => b.TotalCost, updateDto.TotalCost));

            if (updateDto.Notes != null)
                updates.Add(updateDefinitionBuilder.Set(b => b.Notes, updateDto.Notes));

            // Always update the UpdatedAt field
            updates.Add(updateDefinitionBuilder.Set(b => b.UpdatedAt, DateTime.UtcNow));

            if (updates.Count > 1) // More than just UpdatedAt
            {
                var combinedUpdate = updateDefinitionBuilder.Combine(updates);
                await bookings.UpdateOneAsync(b => b.Id == id, combinedUpdate);
            }

            // Fetch and return the updated booking
            var updatedBooking = await bookings.Find(b => b.Id == id).FirstOrDefaultAsync();

            return Ok(new { message = "Booking updated successfully", booking = updatedBooking });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error updating booking", error = ex.Message });
        }
    }

    // DELETE: api/bookings/{id}
    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteBooking(string id)
    {
        try
        {
            var bookings = _mongoService.GetCollection<Booking>("bookings");
            var booking = await bookings.Find(b => b.Id == id).FirstOrDefaultAsync();

            if (booking == null)
            {
                return NotFound(new { message = "Booking not found" });
            }

            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;

            var evOwners = _mongoService.GetCollection<EVOwner>("evowners");
            var evOwner = await evOwners.Find(e => e.UserId == userId).FirstOrDefaultAsync();

            bool canDelete = userRole == "backoffice" ||
                           (userRole == "ev_owner" &&
                            booking.EVOwnerId == evOwner?.Id &&
                            booking.Status == "pending");

            if (!canDelete)
            {
                return StatusCode(403, new { message = "Access denied" });
            }

            // Update station availability if booking was not cancelled or completed
            if (booking.Status != "cancelled" && booking.Status != "completed")
            {
                var stations = _mongoService.GetCollection<Station>("stations");
                var updateDefinition = Builders<Station>.Update.Inc(s => s.AvailableSlots, 1);
                await stations.UpdateOneAsync(s => s.Id == booking.StationId, updateDefinition);
            }

            await bookings.DeleteOneAsync(b => b.Id == id);
            return Ok(new { message = "Booking deleted successfully" });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error deleting booking", error = ex.Message });
        }
    }
}