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
[Authorize]
public class EVOwnersController : ControllerBase
{
    private readonly MongoDBService _mongoService;

    public EVOwnersController(MongoDBService mongoService)
    {
        _mongoService = mongoService;
    }

    // GET: api/evowners
    [HttpGet]
    public async Task<IActionResult> GetAllEVOwners([FromQuery] string? status)
    {
        try
        {
            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            if (userRole != "backoffice")
            {
                return Forbid();
            }

            var evOwners = _mongoService.GetCollection<EVOwner>("evowners");
            var filterBuilder = Builders<EVOwner>.Filter.Empty;

            if (!string.IsNullOrEmpty(status))
            {
                filterBuilder &= Builders<EVOwner>.Filter.Eq(e => e.Status, status);
            }

            var evOwnersList = await evOwners.Find(filterBuilder)
                .Sort(Builders<EVOwner>.Sort.Ascending(e => e.Name))
                .ToListAsync();

            // Get associated user data
            var users = _mongoService.GetCollection<User>("users");
            var userIds = evOwnersList.Select(e => e.UserId).ToList();
            var usersList = await users.Find(u => userIds.Contains(u.Id)).ToListAsync();

            var result = evOwnersList.Select(evOwner =>
            {
                var user = usersList.FirstOrDefault(u => u.Id == evOwner.UserId);
                return new
                {
                    evOwner.Id,
                    evOwner.UserId,
                    evOwner.Name,
                    evOwner.Email,
                    evOwner.Phone,
                    evOwner.Address,
                    evOwner.NIC,
                    evOwner.VehicleModel,
                    evOwner.VehicleNumber,
                    evOwner.Status,
                    User = user != null ? new
                    {
                        user.Email,
                        user.Status,
                        user.CreatedAt,
                        user.UpdatedAt
                    } : null
                };
            });

            return Ok(result);
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error fetching EV owners", error = ex.Message });
        }
    }

    // GET: api/evowners/{id}
    [HttpGet("{id}")]
    public async Task<IActionResult> GetEVOwnerById(string id)
    {
        try
        {
            var evOwners = _mongoService.GetCollection<EVOwner>("evowners");
            var evOwner = await evOwners.Find(e => e.Id == id).FirstOrDefaultAsync();

            if (evOwner == null)
            {
                return NotFound(new { message = "EV Owner not found" });
            }

            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;

            var canView = userRole == "backoffice" ||
                         (userRole == "ev_owner" && evOwner.UserId == userId);

            if (!canView)
            {
                return Forbid();
            }

            var users = _mongoService.GetCollection<User>("users");
            var user = await users.Find(u => u.Id == evOwner.UserId).FirstOrDefaultAsync();

            var result = new
            {
                evOwner.Id,
                evOwner.UserId,
                evOwner.Name,
                evOwner.Email,
                evOwner.Phone,
                evOwner.Address,
                evOwner.NIC,
                evOwner.VehicleModel,
                evOwner.VehicleNumber,
                evOwner.Status,
                User = user != null ? new
                {
                    user.Email,
                    user.Status,
                    user.CreatedAt,
                    user.UpdatedAt
                } : null
            };

            return Ok(result);
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error fetching EV owner", error = ex.Message });
        }
    }

    // GET: api/evowners/profile/me
    [HttpGet("profile/me")]
    public async Task<IActionResult> GetCurrentUserProfile()
    {
        try
        {
            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            if (userRole != "ev_owner")
            {
                return Forbid();
            }

            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            var evOwners = _mongoService.GetCollection<EVOwner>("evowners");
            var evOwner = await evOwners.Find(e => e.UserId == userId).FirstOrDefaultAsync();

            if (evOwner == null)
            {
                return NotFound(new { message = "EV Owner profile not found" });
            }

            var users = _mongoService.GetCollection<User>("users");
            var user = await users.Find(u => u.Id == userId).FirstOrDefaultAsync();

            var result = new
            {
                evOwner.Id,
                evOwner.UserId,
                evOwner.Name,
                evOwner.Email,
                evOwner.Phone,
                evOwner.Address,
                evOwner.NIC,
                evOwner.VehicleModel,
                evOwner.VehicleNumber,
                evOwner.Status,
                User = user != null ? new
                {
                    user.Email,
                    user.Status,
                    user.CreatedAt,
                    user.UpdatedAt
                } : null
            };

            return Ok(result);
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error fetching profile", error = ex.Message });
        }
    }

    // PUT: api/evowners/{id}
    [HttpPut("{id}")]
    public async Task<IActionResult> UpdateEVOwner(string id, [FromBody] EVOwner updatedEVOwner)
    {
        try
        {
            var evOwners = _mongoService.GetCollection<EVOwner>("evowners");
            var existingEVOwner = await evOwners.Find(e => e.Id == id).FirstOrDefaultAsync();

            if (existingEVOwner == null)
            {
                return NotFound(new { message = "EV Owner not found" });
            }

            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;

            var canUpdate = userRole == "backoffice" ||
                          (userRole == "ev_owner" && existingEVOwner.UserId == userId);

            if (!canUpdate)
            {
                return Forbid();
            }

            // Check unique constraints
            if (updatedEVOwner.NIC != existingEVOwner.NIC)
            {
                var nicExists = await evOwners.Find(e => e.NIC == updatedEVOwner.NIC && e.Id != id).AnyAsync();
                if (nicExists)
                {
                    return BadRequest(new { message = "NIC already exists" });
                }
            }

            if (updatedEVOwner.VehicleNumber.ToUpper() != existingEVOwner.VehicleNumber)
            {
                var vehicleExists = await evOwners.Find(e =>
                    e.VehicleNumber == updatedEVOwner.VehicleNumber.ToUpper() && e.Id != id).AnyAsync();
                if (vehicleExists)
                {
                    return BadRequest(new { message = "Vehicle number already exists" });
                }
            }

            // Preserve original values
            updatedEVOwner.UserId = existingEVOwner.UserId;
            updatedEVOwner.VehicleNumber = updatedEVOwner.VehicleNumber.ToUpper();

            await evOwners.ReplaceOneAsync(e => e.Id == id, updatedEVOwner);

            var users = _mongoService.GetCollection<User>("users");
            var user = await users.Find(u => u.Id == updatedEVOwner.UserId).FirstOrDefaultAsync();

            var result = new
            {
                message = "EV Owner updated successfully",
                evOwner = new
                {
                    updatedEVOwner.Id,
                    updatedEVOwner.UserId,
                    updatedEVOwner.Name,
                    updatedEVOwner.Email,
                    updatedEVOwner.Phone,
                    updatedEVOwner.Address,
                    updatedEVOwner.NIC,
                    updatedEVOwner.VehicleModel,
                    updatedEVOwner.VehicleNumber,
                    updatedEVOwner.Status,
                    User = user != null ? new
                    {
                        user.Email,
                        user.Status,
                        user.CreatedAt,
                        user.UpdatedAt
                    } : null
                }
            };

            return Ok(result);
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error updating EV owner", error = ex.Message });
        }
    }

    // PUT: api/evowners/{id}/status
    [HttpPut("{id}/status")]
    public async Task<IActionResult> UpdateEVOwnerStatus(string id, [FromBody] UserStatusUpdateDto statusUpdate)
    {
        try
        {
            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            if (userRole != "backoffice")
            {
                return Forbid();
            }

            var evOwners = _mongoService.GetCollection<EVOwner>("evowners");
            var evOwner = await evOwners.Find(e => e.Id == id).FirstOrDefaultAsync();

            if (evOwner == null)
            {
                return NotFound(new { message = "EV Owner not found" });
            }

            // Update both EV owner and user status
            var update = Builders<EVOwner>.Update
                .Set(e => e.Status, statusUpdate.Status);

            await evOwners.UpdateOneAsync(e => e.Id == id, update);

            var users = _mongoService.GetCollection<User>("users");
            await users.UpdateOneAsync(
                u => u.Id == evOwner.UserId,
                Builders<User>.Update.Set(u => u.Status, statusUpdate.Status)
            );

            var updatedEVOwner = await evOwners.Find(e => e.Id == id).FirstOrDefaultAsync();
            var user = await users.Find(u => u.Id == updatedEVOwner.UserId).FirstOrDefaultAsync();

            var result = new
            {
                message = "EV Owner status updated successfully",
                evOwner = new
                {
                    updatedEVOwner.Id,
                    updatedEVOwner.UserId,
                    updatedEVOwner.Name,
                    updatedEVOwner.Email,
                    updatedEVOwner.Phone,
                    updatedEVOwner.Address,
                    updatedEVOwner.NIC,
                    updatedEVOwner.VehicleModel,
                    updatedEVOwner.VehicleNumber,
                    updatedEVOwner.Status,
                    User = user != null ? new
                    {
                        user.Email,
                        user.Status,
                        user.CreatedAt,
                        user.UpdatedAt
                    } : null
                }
            };

            return Ok(result);
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error updating EV owner status", error = ex.Message });
        }
    }

    // DELETE: api/evowners/{id}
    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteEVOwner(string id)
    {
        try
        {
            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            if (userRole != "backoffice")
            {
                return Forbid();
            }

            var evOwners = _mongoService.GetCollection<EVOwner>("evowners");
            var evOwner = await evOwners.Find(e => e.Id == id).FirstOrDefaultAsync();

            if (evOwner == null)
            {
                return NotFound(new { message = "EV Owner not found" });
            }

            // Delete associated user account
            var users = _mongoService.GetCollection<User>("users");
            await users.DeleteOneAsync(u => u.Id == evOwner.UserId);

            // Delete EV owner profile
            await evOwners.DeleteOneAsync(e => e.Id == id);

            return Ok(new { message = "EV Owner deleted successfully" });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error deleting EV owner", error = ex.Message });
        }
    }
}