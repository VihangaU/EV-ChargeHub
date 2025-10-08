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
public class UsersController : ControllerBase
{
    private readonly MongoDBService _mongoService;

    public UsersController(MongoDBService mongoService)
    {
        _mongoService = mongoService;
    }

    // GET: api/users
    [HttpGet]
    public async Task<IActionResult> GetAllUsers([FromQuery] string? role, [FromQuery] string? status)
    {
        try
        {
            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            if (userRole != "backoffice")
            {
                return Forbid();
            }

            var users = _mongoService.GetCollection<User>("users");
            var filterBuilder = Builders<User>.Filter.Empty;

            if (!string.IsNullOrEmpty(role))
            {
                filterBuilder &= Builders<User>.Filter.Eq(u => u.Role, role);
            }
            if (!string.IsNullOrEmpty(status))
            {
                filterBuilder &= Builders<User>.Filter.Eq(u => u.Status, status);
            }

            var userList = await users.Find(filterBuilder)
                .Sort(Builders<User>.Sort.Descending(u => u.CreatedAt))
                .ToListAsync();

            return Ok(userList);
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error fetching users", error = ex.Message });
        }
    }

    // GET: api/users/{id}
    [HttpGet("{id}")]
    public async Task<IActionResult> GetUserById(string id)
    {
        try
        {
            var users = _mongoService.GetCollection<User>("users");
            var user = await users.Find(u => u.Id == id).FirstOrDefaultAsync();

            if (user == null)
            {
                return NotFound(new { message = "User not found" });
            }

            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;

            if (userRole != "backoffice" && userId != id)
            {
                return Forbid();
            }

            // Remove password from response
            user.Password = string.Empty;

            return Ok(user);
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error fetching user", error = ex.Message });
        }
    }

    // POST: api/users
    [HttpPost]
    public async Task<IActionResult> CreateUser([FromBody] CreateUserDto createUserDto)
    {
        try
        {
            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            if (userRole != "backoffice")
            {
                return Forbid();
            }

            var users = _mongoService.GetCollection<User>("users");

            // Check if user exists
            if (await users.Find(u => u.Email == createUserDto.Email).AnyAsync())
            {
                return BadRequest(new { message = "User already exists with this email" });
            }

            var user = new User
            {
                Email = createUserDto.Email,
                Password = BCrypt.Net.BCrypt.HashPassword(createUserDto.Password),
                Name = createUserDto.Name,
                Role = createUserDto.Role,
                Status = createUserDto.Status,
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow
            };

            await users.InsertOneAsync(user);

            // Remove password from response
            user.Password = string.Empty;

            return CreatedAtAction(nameof(GetUserById), new { id = user.Id }, user);
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error creating user", error = ex.Message });
        }
    }

    // PUT: api/users/{id}
    [HttpPut("{id}")]
    public async Task<IActionResult> UpdateUser(string id, [FromBody] UpdateUserDto updateDto)
    {
        try
        {
            var users = _mongoService.GetCollection<User>("users");
            var existingUser = await users.Find(u => u.Id == id).FirstOrDefaultAsync();

            if (existingUser == null)
            {
                return NotFound(new { message = "User not found" });
            }

            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            var canUpdate = userRole == "backoffice" || userId == id;

            if (!canUpdate)
            {
                return Forbid();
            }

            // Non-backoffice users cannot change role or status
            if (userRole != "backoffice")
            {
                updateDto.Role = null;
                updateDto.Status = null;
            }

            // Check email uniqueness if updating email
            if (updateDto.Email != null && updateDto.Email != existingUser.Email)
            {
                var emailExists = await users.Find(u => u.Email == updateDto.Email && u.Id != id).AnyAsync();
                if (emailExists)
                {
                    return BadRequest(new { message = "Email already exists" });
                }
            }

            // Build update operations for provided fields only
            var update = Builders<User>.Update.Set(u => u.UpdatedAt, DateTime.UtcNow);

            if (updateDto.Name != null)
            {
                update = update.Set(u => u.Name, updateDto.Name);
            }
            if (updateDto.Email != null)
            {
                update = update.Set(u => u.Email, updateDto.Email);
            }
            if (updateDto.Role != null)
            {
                update = update.Set(u => u.Role, updateDto.Role);
            }
            if (updateDto.Status != null)
            {
                update = update.Set(u => u.Status, updateDto.Status);
            }
            if (!string.IsNullOrEmpty(updateDto.Password))
            {
                update = update.Set(u => u.Password, BCrypt.Net.BCrypt.HashPassword(updateDto.Password));
            }

            var result = await users.UpdateOneAsync(u => u.Id == id, update);

            if (result.MatchedCount == 0)
            {
                return NotFound(new { message = "User not found" });
            }

            // Fetch the updated user
            var updatedUser = await users.Find(u => u.Id == id).FirstOrDefaultAsync();

            // Remove password from response
            updatedUser.Password = string.Empty;

            return Ok(new { message = "User updated successfully", user = updatedUser });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error updating user", error = ex.Message });
        }
    }

    // PUT: api/users/{id}/status
    [HttpPut("{id}/status")]
    public async Task<IActionResult> UpdateUserStatus(string id, [FromBody] UserStatusUpdateDto statusUpdate)
    {
        try
        {
            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            if (userRole != "backoffice")
            {
                return Forbid();
            }

            var users = _mongoService.GetCollection<User>("users");
            var update = Builders<User>.Update
                .Set(u => u.Status, statusUpdate.Status)
                .Set(u => u.UpdatedAt, DateTime.UtcNow);

            var result = await users.UpdateOneAsync(u => u.Id == id, update);

            if (result.MatchedCount == 0)
            {
                return NotFound(new { message = "User not found" });
            }

            var updatedUser = await users.Find(u => u.Id == id).FirstOrDefaultAsync();

            // Remove password from response
            updatedUser.Password = string.Empty;

            return Ok(new { message = "User status updated successfully", user = updatedUser });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error updating user status", error = ex.Message });
        }
    }

    // DELETE: api/users/{id}
    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteUser(string id)
    {
        try
        {
            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            if (userRole != "backoffice")
            {
                return Forbid();
            }

            var users = _mongoService.GetCollection<User>("users");
            var result = await users.DeleteOneAsync(u => u.Id == id);

            if (result.DeletedCount == 0)
            {
                return NotFound(new { message = "User not found" });
            }

            return Ok(new { message = "User deleted successfully" });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error deleting user", error = ex.Message });
        }
    }
}