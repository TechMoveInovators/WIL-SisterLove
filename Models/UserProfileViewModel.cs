using System.ComponentModel.DataAnnotations;

namespace Sisterlove.Models
{
    public class UserProfileViewModel
    {
        [Required]
        [Display(Name = "Full Name")]
        public string FullName { get; set; }

        [Required]
        [EmailAddress]
        [Display(Name = "Email Address")]
        public string Email { get; set; }

        [Phone]
        [Display(Name = "Phone Number")]
        public string PhoneNumber { get; set; }

        [Display(Name = "Member Since")]
        public DateTime MemberSince { get; set; }
    }
}
