using System.ComponentModel.DataAnnotations;

namespace Sisterlove.Models.Entities
{
    public class FundingEnquiry
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
        [MaxLength(2000)]
        public string Enquiry { get; set; } = string.Empty;

        public DateTime SubmittedAt { get; set; } = DateTime.UtcNow;

        public int? UserId { get; set; }

        public User? User { get; set; }
    }
}