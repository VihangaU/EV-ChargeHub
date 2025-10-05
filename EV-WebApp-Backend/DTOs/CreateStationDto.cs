using System.ComponentModel.DataAnnotations;

namespace EV_WebApp_Backend.DTOs;

public class CreateStationDto
{
    [Required]
    public string Name { get; set; } = null!;

    [Required]
    public string Address { get; set; } = null!;

    public double Latitude { get; set; }

    public double Longitude { get; set; }

    [Required]
    public string Type { get; set; } = null!;

    [Required]
    public int TotalSlots { get; set; }

    [Required]
    public decimal PricePerHour { get; set; }

    public List<string> Amenities { get; set; } = new List<string>();
}