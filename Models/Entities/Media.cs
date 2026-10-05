using System.ComponentModel.DataAnnotations;

namespace Sisterlove.Models.Entities
{
    public class Media
    {
        public int Id { get; set; }

        [Required]
        [MaxLength(200)]
        public string FileName { get; set; } = string.Empty;

        [Required]
        [MaxLength(500)]
        public string FilePath { get; set; } = string.Empty;

        [MaxLength(50)]
        public string? MediaType { get; set; }

        public DateTime UploadedAt { get; set; } = DateTime.UtcNow;

        public int? ProgrammeId { get; set; }

        public Programme? Programme { get; set; }

        public int? NewsArticleId { get; set; }

        public NewsArticle? NewsArticle { get; set; }

        public int? LegacyProjectId { get; set; }

        public LegacyProject? LegacyProject { get; set; }
    }
}