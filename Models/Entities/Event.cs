using System.ComponentModel.DataAnnotations;

namespace Sisterlove.Models.Entities
{
    public class Event
    {
        public int Id { get; set; }

        [Required]
        [MaxLength(150)]
        public string EventName { get; set; } = string.Empty;

        [Required]
        [MaxLength(1000)]
        public string Description { get; set; } = string.Empty;

        [Required]
        public DateTime EventDate { get; set; }

        [Required]
        [MaxLength(200)]
        public string Venue { get; set; } = string.Empty;

        public int? CreatedByUserId { get; set; }

        public User? CreatedByUser { get; set; }

        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    }
}