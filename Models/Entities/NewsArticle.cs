using System.ComponentModel.DataAnnotations;

namespace Sisterlove.Models.Entities
{
    public class NewsArticle
    {
        public int Id { get; set; }

        [Required]
        [MaxLength(200)]
        public string Title { get; set; } = string.Empty;

        [Required]
        [MaxLength(5000)]
        public string Content { get; set; } = string.Empty;

        public DateTime PublishedAt { get; set; } = DateTime.UtcNow;

        public int? CreatedByUserId { get; set; }

        public User? CreatedByUser { get; set; }

        public ICollection<Media> Media { get; set; } = new List<Media>();
    }
}