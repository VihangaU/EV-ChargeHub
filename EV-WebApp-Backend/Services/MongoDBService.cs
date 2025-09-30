using MongoDB.Driver;
using MongoDB.Bson;  // Add this namespace
using Microsoft.Extensions.Options;

namespace EV_WebApp_Backend.Services;

public class MongoDBService
{
    private readonly IMongoDatabase _database;
    private readonly ILogger<MongoDBService> _logger;
    private readonly MongoClient _client;

    public MongoDBService(IOptions<MongoDbSettings> mongoSettings, ILogger<MongoDBService> logger)
    {
        _logger = logger;
        try
        {
            _logger.LogInformation("Attempting to connect to MongoDB...");
            _logger.LogInformation($"Connection String: {mongoSettings.Value.ConnectionString}");
            _logger.LogInformation($"Database Name: {mongoSettings.Value.DatabaseName}");

            var settings = MongoClientSettings.FromConnectionString(mongoSettings.Value.ConnectionString);
            settings.ServerApi = new ServerApi(ServerApiVersion.V1);
            settings.ConnectTimeout = TimeSpan.FromSeconds(30);

            _client = new MongoClient(settings);
            _database = _client.GetDatabase(mongoSettings.Value.DatabaseName);

            // Test the connection
            var pingCommand = new BsonDocument("ping", 1);
            _database.RunCommand<BsonDocument>(pingCommand);
            
            _logger.LogInformation("Successfully connected to MongoDB.");
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to connect to MongoDB: {Message}", ex.Message);
            throw;
        }
    }

    public IMongoCollection<T> GetCollection<T>(string name)
    {
        try
        {
            return _database.GetCollection<T>(name);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error getting collection {CollectionName}: {Message}", name, ex.Message);
            throw;
        }
    }

    public bool IsConnected()
    {
        try
        {
            var pingCommand = new BsonDocument("ping", 1);
            _database.RunCommand<BsonDocument>(pingCommand);
            return true;
        }
        catch
        {
            return false;
        }
    }
}

public class MongoDbSettings
{
    public string ConnectionString { get; set; } = null!;
    public string DatabaseName { get; set; } = null!;
}