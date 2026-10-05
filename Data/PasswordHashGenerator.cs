using BCrypt.Net;

namespace Sisterlove.Data
{
    public static class PasswordHashGenerator
    {
        public static string Generate(string password)
        {
            return BCrypt.Net.BCrypt.HashPassword(password);
        }
    }
}