using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using Microsoft.IdentityModel.Tokens;
using EV_WebApp_Backend.Models;

namespace EV_WebApp_Backend.Services;

public class AuthService
{
    private readonly string _jwtSecret;
    private readonly int _jwtExpiryDays;

    public AuthService(IConfiguration configuration)
    {
        _jwtSecret = configuration["JwtSettings:Secret"] ?? throw new ArgumentNullException(nameof(configuration));
        _jwtExpiryDays = configuration.GetValue<int>("JwtSettings:ExpiryDays");
    }

    public string GenerateJwtToken(User user)
    {
        var tokenHandler = new JwtSecurityTokenHandler();
        var key = Encoding.ASCII.GetBytes(_jwtSecret);
        
        var tokenDescriptor = new SecurityTokenDescriptor
        {
            Subject = new ClaimsIdentity(new[]
            {
                new Claim(ClaimTypes.NameIdentifier, user.Id),
                new Claim(ClaimTypes.Email, user.Email),
                new Claim(ClaimTypes.Role, user.Role)
            }),
            Expires = DateTime.UtcNow.AddDays(_jwtExpiryDays),
            SigningCredentials = new SigningCredentials(
                new SymmetricSecurityKey(key),
                SecurityAlgorithms.HmacSha256Signature)
        };

        var token = tokenHandler.CreateToken(tokenDescriptor);
        return tokenHandler.WriteToken(token);
    }
}