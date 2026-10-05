using System.ComponentModel.DataAnnotations;
using Microsoft.EntityFrameworkCore;

namespace Sisterlove.Models.Entities
{
    [Index(nameof(Email), IsUnique = true)]
    public class User
    {
        public int Id { get; set; }

        [Required]
        [MaxLength(150)]
        public string FullName { get; set; } = string.Empty;

        [Required]
        [MaxLength(150)]
        [EmailAddress]
        public string Email { get; set; } = string.Empty;

        [MaxLength(30)]
        public string? PhoneNumber { get; set; }

        public DateTime MemberSince { get; set; } = DateTime.UtcNow;

        [Required]
        public int RoleId { get; set; }

        public Role? Role { get; set; }

        public ICollection<Donation> Donations { get; set; } = new List<Donation>();
    }
}