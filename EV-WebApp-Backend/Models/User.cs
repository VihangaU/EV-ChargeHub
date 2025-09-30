using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace EV_WebApp_Backend.Models;

public class User
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string Id { get; set; } = null!;

    [BsonElement("email")]
    public string Email { get; set; } = null!;

    [BsonElement("password")]
    public string Password { get; set; } = null!;

    [BsonElement("name")]
    public string Name { get; set; } = null!;

    [BsonElement("role")]
    public string Role { get; set; } = null!;

    [BsonElement("status")]
    public string Status { get; set; } = "active";

    [BsonElement("createdAt")]
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

    [BsonElement("updatedAt")]
    public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;

    [BsonElement("__v")]
    public int Version { get; set; }
}