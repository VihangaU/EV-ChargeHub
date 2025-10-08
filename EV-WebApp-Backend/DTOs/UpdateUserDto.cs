using System.ComponentModel.DataAnnotations;

namespace EV_WebApp_Backend.DTOs;

public class UpdateUserDto
{
    public string? Name { get; set; }

    public string? Email { get; set; }

    public string? Password { get; set; }

    public string? Role { get; set; }  // Only for backoffice

    public string? Status { get; set; }  // Only for backoffice
}