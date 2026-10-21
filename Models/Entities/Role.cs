using System.ComponentModel.DataAnnotations;

namespace Sisterlove.Models.Entities
{
    public class Role
    {
        public int Id { get; set; }

        [Required]
        [MaxLength(50)]
        public string RoleName { get; set; } = string.Empty;

        [MaxLength(250)]
        public string? Description { get; set; }

        public ICollection<User> Users { get; set; } = new List<User>();
    }
}