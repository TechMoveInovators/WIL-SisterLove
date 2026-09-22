namespace SisterLoveSA.Models
{
    public class DonationResult
    {
        public bool Success { get; set; }
        public string TransactionId { get; set; }
        public string DonorName { get; set; }
        public decimal Amount { get; set; }
        public string ErrorMessage { get; set; }
    }
}