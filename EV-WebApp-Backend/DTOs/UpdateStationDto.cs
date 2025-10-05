using System.ComponentModel.DataAnnotations;

namespace EV_WebApp_Backend.DTOs;

public class UpdateStationDto
{
    [Required]
    public string Name { get; set; } = null!;

    [Required]
    public string Address { get; set; } = null!;

    public double Latitude { get; set; }

    public double Longitude { get; set; }

    [Required]
    public decimal PricePerHour { get; set; }
}