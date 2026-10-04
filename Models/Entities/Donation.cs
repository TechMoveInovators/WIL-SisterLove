using System.ComponentModel.DataAnnotations;

namespace Sisterlove.Models.Entities
{
    public class Donation
    {
        public int Id { get; set; }

        [Required]
        [MaxLength(100)]
        public string DonorName { get; set; } = string.Empty;

        [Required]
        [MaxLength(150)]
        public string Email { get; set; } = string.Empty;

        [Required]
        public decimal Amount { get; set; }

        [MaxLength(50)]
        public string? PaymentMethod { get; set; }

        [MaxLength(100)]
        public string? TransactionId { get; set; }

        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    }
}