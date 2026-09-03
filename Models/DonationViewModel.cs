using System.ComponentModel.DataAnnotations;

namespace Sisterlove.Models
{
    public class DonationViewModel
    {
        [Required]
        [Display(Name = "Full Name")]
        public string DonorName { get; set; }

        [Required]
        [EmailAddress]
        [Display(Name = "Email Address")]
        public string Email { get; set; }

        [Required]
        [Display(Name = "Donation Amount")]
        public decimal? Amoount {  get; set; }

        [Display(Name = "Message (Optional)")]
        [StringLength(500)]
        public string Message { get; set; }

        public DonationType DonationType { get; set; }

        // Preset amounts for quick selection
        public List<decimal> PresetAmounts => new List<decimal> { 50, 100, 250, 500, 1000 };
    }

    public enum DonationType
    {
        OneTime,
        Monthly
    }

    public class DonationResult
        {
            public bool Success { get; set; }
            public string DonorName { get; set; }
            public decimal Amount { get; set; }
            public string ErrorMessage { get; set; }
        }
    }