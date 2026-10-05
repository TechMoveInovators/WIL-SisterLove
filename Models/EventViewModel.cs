using System.ComponentModel.DataAnnotations;

namespace SisterLoveSA.Models
{
    public class EventViewModel
    {
        public int Id { get; set; }

        [Required(ErrorMessage = "Event name is required.")]
        [Display(Name = "Event Name")]
        [MaxLength(150)]
        public string EventName { get; set; } = string.Empty;

        [Required(ErrorMessage = "Description is required.")]
        [MaxLength(1000)]
        public string Description { get; set; } = string.Empty;

        [Required(ErrorMessage = "Event date is required.")]
        [Display(Name = "Event Date")]
        public DateTime EventDate { get; set; }

        [Required(ErrorMessage = "Venue is required.")]
        [MaxLength(200)]
        public string Venue { get; set; } = string.Empty;

        [Display(Name = "Created By")]
        [MaxLength(150)]
        public string? CreatedBy { get; set; }
    }
}