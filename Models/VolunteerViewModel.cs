using System.ComponentModel.DataAnnotations;

namespace SisterLoveSA.Models
{
    public class VolunteerViewModel
    {
        [Required(ErrorMessage = "Full Name is required.")]
        [Display(Name = "Full Name")]
        public string Name { get; set; } = string.Empty;

        [Required(ErrorMessage = "Email address is required.")]
        [EmailAddress(ErrorMessage = "Invalid email address.")]
        public string Email { get; set; } = string.Empty;

        [Required(ErrorMessage = "Phone number is required.")]
        [Phone(ErrorMessage = "Invalid phone number.")]
        public string Phone { get; set; } = string.Empty;

        [Required(ErrorMessage = "Area of interest is required.")]
        [Display(Name = "Area of Interest")]
        public string AreaOfInterest { get; set; } = string.Empty;

        [Required(ErrorMessage = "Motivation is required.")]
        public string Motivation { get; set; } = string.Empty;
    }
}