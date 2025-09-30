using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace EV_WebApp_Backend.Models;

public class ScheduleSlot
{
    [BsonElement("day")]
    public string Day { get; set; } = null!;

    [BsonElement("startTime")]
    public string StartTime { get; set; } = null!;

    [BsonElement("endTime")]
    public string EndTime { get; set; } = null!;

    [BsonElement("availableSlots")]
    public int AvailableSlots { get; set; }

    [BsonElement("isActive")]
    public bool IsActive { get; set; } = true;
}

public class Station
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? Id { get; set; }

    [BsonElement("name")]
    public string Name { get; set; } = null!;

    [BsonElement("address")]
    public string Address { get; set; } = null!;

    [BsonElement("latitude")]
    public double Latitude { get; set; }

    [BsonElement("longitude")]
    public double Longitude { get; set; }

    [BsonElement("type")]
    public string Type { get; set; } = null!;

    [BsonElement("totalSlots")]
    public int TotalSlots { get; set; }

    [BsonElement("availableSlots")]
    public int AvailableSlots { get; set; }

    [BsonElement("operatorId")]
    [BsonRepresentation(BsonType.ObjectId)]  // Add this attribute
    public string OperatorId { get; set; } = null!;

    [BsonElement("operatorName")]
    public string OperatorName { get; set; } = null!;

    [BsonElement("status")]
    public string Status { get; set; } = "active";

    [BsonElement("pricePerHour")]
    public decimal PricePerHour { get; set; }

    [BsonElement("amenities")]
    public List<string> Amenities { get; set; } = new List<string>();

    [BsonElement("schedule")]
    public List<ScheduleSlot> Schedule { get; set; } = new List<ScheduleSlot>();

    [BsonElement("createdAt")]
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

    [BsonElement("updatedAt")]
    public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;

    [BsonElement("__v")]
    public int Version { get; set; }
}