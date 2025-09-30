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
public class AuthController : ControllerBase
{
    private readonly MongoDBService _mongoService;
    private readonly AuthService _authService;

    public AuthController(MongoDBService mongoService, AuthService authService)
    {
        _mongoService = mongoService;
        _authService = authService;
    }

    // POST: api/auth/login
    [HttpPost("login")]
    public async Task<IActionResult> Login([FromBody] LoginDto loginDto)
    {
        try
        {
            var users = _mongoService.GetCollection<User>("users");
            var user = await users.Find(u => u.Email == loginDto.Email).FirstOrDefaultAsync();

            if (user == null || !BCrypt.Net.BCrypt.Verify(loginDto.Password, user.Password))
            {
                return Unauthorized(new { message = "Invalid credentials" });
            }

            if (user.Status != "active")
            {
                return Unauthorized(new { message = "Account is inactive" });
            }

            var token = _authService.GenerateJwtToken(user);

            return Ok(new
            {
                message = "Login successful",
                token,
                user = new
                {
                    user.Id,
                    user.Email,
                    user.Name,
                    user.Role,
                    user.Status
                }
            });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Login failed", error = ex.Message });
        }
    }

    // POST: api/auth/register
    [HttpPost("register")]
    public async Task<IActionResult> Register([FromBody] RegisterDto registerDto)
    {
        try
        {
            var users = _mongoService.GetCollection<User>("users");
            var evOwners = _mongoService.GetCollection<EVOwner>("evowners");

            // Check if user exists
            if (await users.Find(u => u.Email == registerDto.Email).AnyAsync())
            {
                return BadRequest(new { message = "User already exists with this email" });
            }

            // Check if NIC exists
            if (await evOwners.Find(e => e.NIC == registerDto.NIC).AnyAsync())
            {
                return BadRequest(new { message = "NIC already registered" });
            }

            // Create user
            var user = new User
            {
                Email = registerDto.Email,
                Password = BCrypt.Net.BCrypt.HashPassword(registerDto.Password),
                Name = registerDto.Name,
                Role = "ev_owner",
                Status = "active",
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow
            };

            await users.InsertOneAsync(user);

            // Create EV Owner profile
            var evOwner = new EVOwner
            {
                UserId = user.Id,
                NIC = registerDto.NIC,
                Name = registerDto.Name,
                Email = registerDto.Email,
                Phone = registerDto.Phone,
                Address = registerDto.Address,
                VehicleModel = registerDto.VehicleModel,
                VehicleNumber = registerDto.VehicleNumber.ToUpper(),
                Status = "active"
            };

            await evOwners.InsertOneAsync(evOwner);

            var token = _authService.GenerateJwtToken(user);

            return Created(string.Empty, new
            {
                message = "Registration successful",
                token,
                user = new
                {
                    user.Id,
                    user.Email,
                    user.Name,
                    user.Role,
                    user.Status
                }
            });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Registration failed", error = ex.Message });
        }
    }

    // GET: api/auth/verify
    [Authorize]
    [HttpGet("verify")]
    public async Task<IActionResult> VerifyToken()
    {
        try
        {
            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (userId == null)
            {
                return Unauthorized(new { message = "Invalid token" });
            }

            var users = _mongoService.GetCollection<User>("users");
            var user = await users.Find(u => u.Id == userId).FirstOrDefaultAsync();

            if (user == null)
            {
                return NotFound(new { message = "User not found" });
            }

            if (user.Status != "active")
            {
                return Unauthorized(new { message = "Account is inactive" });
            }

            return Ok(new
            {
                user = new
                {
                    user.Id,
                    user.Email,
                    user.Name,
                    user.Role,
                    user.Status
                }
            });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Token verification failed", error = ex.Message });
        }
    }
}