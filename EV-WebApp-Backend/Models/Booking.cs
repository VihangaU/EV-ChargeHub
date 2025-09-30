using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace EV_WebApp_Backend.Models;

public class Booking
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string Id { get; set; } = null!;

    [BsonElement("evOwnerId")]
    public string EVOwnerId { get; set; } = null!;

    [BsonElement("evOwnerName")]
    public string EVOwnerName { get; set; } = null!;

    [BsonElement("evOwnerNIC")]
    public string EVOwnerNIC { get; set; } = null!;

    [BsonElement("stationId")]
    public string StationId { get; set; } = null!;

    [BsonElement("stationName")]
    public string StationName { get; set; } = null!;

    [BsonElement("stationAddress")]
    public string StationAddress { get; set; } = null!;

    [BsonElement("reservationDate")]
    public string ReservationDate { get; set; } = null!;

    [BsonElement("startTime")]
    public string StartTime { get; set; } = null!;

    [BsonElement("endTime")]
    public string EndTime { get; set; } = null!;

    [BsonElement("duration")]
    public int Duration { get; set; }

    [BsonElement("totalCost")]
    public decimal TotalCost { get; set; }

    [BsonElement("status")]
    public string Status { get; set; } = "pending";

    [BsonElement("notes")]
    public string? Notes { get; set; }

    [BsonElement("createdAt")]
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

    [BsonElement("updatedAt")]
    public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;
}