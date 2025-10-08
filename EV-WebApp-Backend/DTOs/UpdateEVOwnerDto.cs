using System.ComponentModel.DataAnnotations;

namespace EV_WebApp_Backend.DTOs;

public class UpdateEVOwnerDto
{

    [Required]
    public string Phone { get; set; } = null!;

    [Required]
    public string Address { get; set; } = null!;

    [Required]
    public string VehicleModel { get; set; } = null!;

    [Required]
    public string VehicleNumber { get; set; } = null!;
}