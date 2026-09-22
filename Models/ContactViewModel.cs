using System.ComponentModel.DataAnnotations;

namespace SisterLoveSA.Models
{
    public class ContactViewModel
    {
        [Required(ErrorMessage = "Please enter your name")]
        [Display(Name = "Full Name")]
        public string Name { get; set; } = string.Empty;

        [Required(ErrorMessage = "Please enter your email")]
        [EmailAddress(ErrorMessage = "Please enter a valid email")]
        [Display(Name = "Email Address")]
        public string Email { get; set; } = string.Empty;

        [Phone(ErrorMessage = "Please enter a valid phone number")]
        [Display(Name = "Phone Number")]
        public string? Phone { get; set; }

        [Required(ErrorMessage = "Please select a subject")]
        [Display(Name = "Subject")]
        public InquiryType Subject { get; set; }

        [Required(ErrorMessage = "Please enter a message")]
        [StringLength(1000, MinimumLength = 10, ErrorMessage = "Message must be between 10 and 1000 characters")]
        [Display(Name = "Message")]
        public string Message { get; set; } = string.Empty;

        [Display(Name = "I'd like to volunteer")]
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