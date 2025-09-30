namespace EV_WebApp_Backend.DTOs;

public class RegisterDto
{
    public string Email { get; set; } = string.Empty;
    public string Password { get; set; } = string.Empty;
    public string Name { get; set; } = string.Empty;
    public string NIC { get; set; } = string.Empty;
    public string Phone { get; set; } = string.Empty;
    public string Address { get; set; } = string.Empty;
    public string VehicleModel { get; set; } = string.Empty;
    public string VehicleNumber { get; set; } = string.Empty;
}