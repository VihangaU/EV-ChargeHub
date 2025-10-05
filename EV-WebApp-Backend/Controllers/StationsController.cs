using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using MongoDB.Driver;
using System.Security.Claims;
using EV_WebApp_Backend.Models;
using EV_WebApp_Backend.Services;
using EV_WebApp_Backend.DTOs;

namespace EV_WebApp_Backend.Controllers;

[ApiController]
[Route("api/[controller]")]
public class StationsController : ControllerBase
{
    private readonly MongoDBService _mongoService;

    public StationsController(MongoDBService mongoService)
    {
        _mongoService = mongoService;
    }

    // GET: api/stations
    [HttpGet]
    public async Task<IActionResult> GetAllStations(
        [FromQuery] string? operatorId,
        [FromQuery] string? status,
        [FromQuery] string? type)
    {
        try
        {
            var stations = _mongoService.GetCollection<Station>("stations");
            var filterBuilder = Builders<Station>.Filter.Empty;

            if (!string.IsNullOrEmpty(operatorId))
            {
                filterBuilder &= Builders<Station>.Filter.Eq(s => s.OperatorId, operatorId);
            }
            if (!string.IsNullOrEmpty(status))
            {
                filterBuilder &= Builders<Station>.Filter.Eq(s => s.Status, status);
            }
            if (!string.IsNullOrEmpty(type))
            {
                filterBuilder &= Builders<Station>.Filter.Eq(s => s.Type, type);
            }

            var stationList = await stations.Find(filterBuilder)
                .Sort(Builders<Station>.Sort.Descending(s => s.CreatedAt))
                .ToListAsync();

            return Ok(stationList);
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error fetching stations", error = ex.Message });
        }
    }

    // GET: api/stations/{id}
    [HttpGet("{id}")]
    public async Task<IActionResult> GetStationById(string id)
    {
        try
        {
            var stations = _mongoService.GetCollection<Station>("stations");
            var station = await stations.Find(s => s.Id == id).FirstOrDefaultAsync();

            if (station == null)
            {
                return NotFound(new { message = "Station not found" });
            }

            return Ok(station);
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error fetching station", error = ex.Message });
        }
    }

    // POST: api/stations
    [Authorize]
    [HttpPost]
    public async Task<IActionResult> CreateStation([FromBody] CreateStationDto stationDto)
    {
        try
        {
            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            if (userRole != "station_operator" && userRole != "backoffice")
            {
                return Forbid();
            }

            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;

            // Fetch the actual user name from the database
            var users = _mongoService.GetCollection<User>("users");
            var user = await users.Find(u => u.Id == userId).FirstOrDefaultAsync();
            var userName = user?.Name ?? "Station Officer";

            // Create the Station object from DTO and add backend-managed fields
            var station = new Station
            {
                Name = stationDto.Name,
                Address = stationDto.Address,
                Latitude = stationDto.Latitude,
                Longitude = stationDto.Longitude,
                Type = stationDto.Type,
                TotalSlots = stationDto.TotalSlots,
                AvailableSlots = stationDto.TotalSlots, // Set available slots equal to total slots initially
                PricePerHour = stationDto.PricePerHour,
                Amenities = stationDto.Amenities,

                // Backend-managed fields
                OperatorId = userId!,
                OperatorName = userName,
                Status = "active",
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow
            };

            var stations = _mongoService.GetCollection<Station>("stations");
            await stations.InsertOneAsync(station);

            return CreatedAtAction(nameof(GetStationById), new { id = station.Id },
                new { message = "Station created successfully", station });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error creating station", error = ex.Message });
        }
    }

    // PUT: api/stations/{id}
    [Authorize]
    [HttpPut("{id}")]
    public async Task<IActionResult> UpdateStation(string id, [FromBody] UpdateStationDto updateDto)
    {
        try
        {
            var stations = _mongoService.GetCollection<Station>("stations");
            var existingStation = await stations.Find(s => s.Id == id).FirstOrDefaultAsync();

            if (existingStation == null)
            {
                return NotFound(new { message = "Station not found" });
            }

            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;

            if (userRole != "backoffice" && existingStation.OperatorId != userId)
            {
                return Forbid();
            }

            // Update only specific fields using MongoDB Update operations
            var update = Builders<Station>.Update
                .Set(s => s.Name, updateDto.Name)
                .Set(s => s.Address, updateDto.Address)
                .Set(s => s.Latitude, updateDto.Latitude)
                .Set(s => s.Longitude, updateDto.Longitude)
                .Set(s => s.PricePerHour, updateDto.PricePerHour)
                .Set(s => s.UpdatedAt, DateTime.UtcNow);

            await stations.UpdateOneAsync(s => s.Id == id, update);

            // Fetch the updated station to return
            var updatedStation = await stations.Find(s => s.Id == id).FirstOrDefaultAsync();

            return Ok(new { message = "Station updated successfully", station = updatedStation });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error updating station", error = ex.Message });
        }
    }

    // PUT: api/stations/{id}/schedule
    [Authorize]
    [HttpPut("{id}/schedule")]
    public async Task<IActionResult> UpdateStationSchedule(string id, [FromBody] List<ScheduleSlot> schedule)
    {
        try
        {
            var stations = _mongoService.GetCollection<Station>("stations");
            var station = await stations.Find(s => s.Id == id).FirstOrDefaultAsync();

            if (station == null)
            {
                return NotFound(new { message = "Station not found" });
            }

            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;

            if (userRole != "backoffice" && station.OperatorId != userId)
            {
                return Forbid();
            }

            var update = Builders<Station>.Update
                .Set(s => s.Schedule, schedule)
                .Set(s => s.UpdatedAt, DateTime.UtcNow);

            await stations.UpdateOneAsync(s => s.Id == id, update);

            station.Schedule = schedule;
            return Ok(new { message = "Station schedule updated successfully", station });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error updating station schedule", error = ex.Message });
        }
    }

    // DELETE: api/stations/{id}
    [Authorize]
    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteStation(string id)
    {
        try
        {
            var stations = _mongoService.GetCollection<Station>("stations");
            var station = await stations.Find(s => s.Id == id).FirstOrDefaultAsync();

            if (station == null)
            {
                return NotFound(new { message = "Station not found" });
            }

            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;

            if (userRole != "backoffice" && station.OperatorId != userId)
            {
                return Forbid();
            }

            await stations.DeleteOneAsync(s => s.Id == id);
            return Ok(new { message = "Station deleted successfully" });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error deleting station", error = ex.Message });
        }
    }
}