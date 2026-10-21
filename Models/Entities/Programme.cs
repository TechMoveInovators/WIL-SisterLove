using System.ComponentModel.DataAnnotations;

namespace Sisterlove.Models.Entities
{
    public class Programme
    {
        public int Id { get; set; }

        [Required]
        [MaxLength(150)]
        public string ProgrammeName { get; set; } = string.Empty;

        [Required]
        [MaxLength(1000)]
        public string Description { get; set; } = string.Empty;

        [MaxLength(200)]
        public string? Location { get; set; }

        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        public int? CreatedByUserId { get; set; }

        public User? CreatedByUser { get; set; }

        public ICollection<Media> Media { get; set; } = new List<Media>();
    }
}