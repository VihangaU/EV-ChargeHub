using System.ComponentModel.DataAnnotations;

namespace EV_WebApp_Backend.DTOs;

public class CreateBookingDto
{
    [Required]
    public string StationId { get; set; } = null!;

    [Required]
    public string ReservationDate { get; set; } = null!;

    [Required]
    public string StartTime { get; set; } = null!;

    [Required]
    public string EndTime { get; set; } = null!;

    [Required]
    public int Duration { get; set; }

    [Required]
    public decimal TotalCost { get; set; }

    public string? Notes { get; set; }
}