using System.ComponentModel.DataAnnotations;

namespace SisterLoveSA.Models
{
    public class UserProfileViewModel
    {
        [Required(ErrorMessage = "Please enter your name")]
        [Display(Name = "Full Name")]
        public string FullName { get; set; } = string.Empty;

        [Required]
        [EmailAddress]
        [Display(Name = "Email Address")]
        public string Email { get; set; } = string.Empty;

        [Phone(ErrorMessage = "Please enter a valid phone number")]
        [Display(Name = "Phone Number")]
        public string? PhoneNumber { get; set; }

        [Display(Name = "Member Since")]
        public DateTime MemberSince { get; set; }
    }
}