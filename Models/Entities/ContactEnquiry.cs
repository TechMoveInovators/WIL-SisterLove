using System.ComponentModel.DataAnnotations;

namespace Sisterlove.Models.Entities
{
    public class ContactEnquiry
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

        [Required]
        [MaxLength(200)]
        public string Subject { get; set; } = string.Empty;

        [Required]
        [MaxLength(3000)]
        public string Message { get; set; } = string.Empty;

        public DateTime SubmittedAt { get; set; } = DateTime.UtcNow;

        public int? UserId { get; set; }

        public User? User { get; set; }
    }
}