using System.ComponentModel.DataAnnotations;

namespace EV_WebApp_Backend.DTOs;

public class UpdateBookingDto
{
    public string? StationId { get; set; }

    public string? ReservationDate { get; set; }

    public string? StartTime { get; set; }

    public string? EndTime { get; set; }

    public int Duration { get; set; }

    public decimal TotalCost { get; set; }

    public string? Notes { get; set; }
}