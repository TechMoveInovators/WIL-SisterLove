using System.ComponentModel.DataAnnotations;

namespace Sisterlove.Models.Entities
{
    public class Partnership
    {
        public int Id { get; set; }

        [Required]
        [MaxLength(150)]
        public string OrganisationName { get; set; } = string.Empty;

        [Required]
        [MaxLength(1000)]
        public string Description { get; set; } = string.Empty;

        [MaxLength(200)]
        public string? Website { get; set; }

        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        public int? CreatedByUserId { get; set; }

        public User? CreatedByUser { get; set; }
    }
}