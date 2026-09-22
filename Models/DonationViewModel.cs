using System.ComponentModel.DataAnnotations;

namespace SisterLoveSA.Models
{
    public class DonationViewModel
    {
        [Required(ErrorMessage = "Please enter your name")]
        [Display(Name = "Full Name")]
        public string DonorName { get; set; } = string.Empty;

        [Required(ErrorMessage = "Please enter your email")]
        [EmailAddress(ErrorMessage = "Please enter a valid email")]
        [Display(Name = "Email Address")]
        public string Email { get; set; } = string.Empty;

        [Required(ErrorMessage = "Please select or enter an amount")]
        [Range(10, 1000000, ErrorMessage = "Amount must be at least R10")]
        [Display(Name = "Donation Amount")]
        public decimal? Amount { get; set; }

        [Display(Name = "Message (Optional)")]
        [StringLength(500)]
        public string? Message { get; set; }

        public DonationType DonationType { get; set; }

        public List<decimal> PresetAmounts => new List<decimal> { 50, 100, 250, 500 };
    }

    public enum DonationType
    {
        OneTime,
        Monthly
    }
}