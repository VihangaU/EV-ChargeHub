using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace EV_WebApp_Backend.Models;

public class EVOwner
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string Id { get; set; } = null!;

    [BsonElement("userId")]
    [BsonRepresentation(BsonType.ObjectId)]
    public string UserId { get; set; } = null!;

    [BsonElement("nic")]
    public string NIC { get; set; } = null!;

    [BsonElement("name")]
    public string Name { get; set; } = null!;

    [BsonElement("email")]
    public string Email { get; set; } = null!;

    [BsonElement("phone")]
    public string Phone { get; set; } = null!;

    [BsonElement("address")]
    public string Address { get; set; } = null!;

    [BsonElement("vehicleModel")]
    public string VehicleModel { get; set; } = null!;

    [BsonElement("vehicleNumber")]
    public string VehicleNumber { get; set; } = null!;

    [BsonElement("status")]
    public string Status { get; set; } = "active";

    [BsonElement("createdAt")]
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

    [BsonElement("updatedAt")]
    public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;

    [BsonElement("__v")]
    public int Version { get; set; }
}