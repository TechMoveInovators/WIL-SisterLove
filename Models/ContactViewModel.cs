using System.ComponentModel.DataAnnotations;

namespace SisterLoveSA.Models
{
    public class ContactViewModel
    {
        [Required]
        [Display(Name = "Full Name")]
        public string Name { get; set; }

        [Required]
        [EmailAddress]
        [Display(Name = "Email Address")]
        public string Email { get; set; }

        [Phone]
        [Display(Name = "Phone Number")]
        public string Phone { get; set; }

        [Required]
        [Display(Name = "Subject")]
        public InquiryType Subject { get; set; }

        [Required]
        [StringLength(1000)]
        [Display(Name = "Message")]
        public string Message { get; set; }

        public bool IsVolunteer { get; set; }
    }

    public enum InquiryType
    {
        Workshop,
        Partnership,
        Volunteering,
        General
    }
}