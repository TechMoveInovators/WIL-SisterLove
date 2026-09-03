using Sisterlove.Models;
using Sisterlove.Services;
using SisterLoveSA.Models;

namespace SisterLoveSA.Services
{
    public class DonationService : IDonationService
    {
        private readonly ILogger<DonationService> _logger;

        public DonationService(ILogger<DonationService> logger)
        {
            _logger = logger;
        }

        public async Task<DonationResult> ProcessDonation(DonationViewModel model)
        {
            try
            {
                // Simulate payment processing
                await Task.Delay(1000);

                // Generate a transaction ID (in production, this would come from the payment gateway)
                var transactionId = GenerateTransactionId();

                var result = new DonationResult
                {
                    Success = true,
                    TransactionId = transactionId,
                    DonorName = model.DonorName,
                    Amount = model.Amount.Value
                };

                // Send confirmation email
                await SendConfirmationEmail(result);

                return result;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error processing donation");
                return new DonationResult
                {
                    Success = false,
                    ErrorMessage = "Unable to process donation. Please try again."
                };
            }
        }

        public async Task<bool> SendConfirmationEmail(DonationResult result)
        {
            try
            {
                // Simulate sending email
                await Task.Delay(500);
                _logger.LogInformation($"Confirmation email sent to donor {result.DonorName} for transaction #{result.TransactionId}");
                return true;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error sending confirmation email");
                return false;
            }
        }

        private string GenerateTransactionId()
        {
            // Generate a unique transaction ID (e.g., SL-2024-XXXX)
            var random = new Random();
            var number = random.Next(1000, 9999);
            return $"SL-{DateTime.Now.Year}-{number}";
        }
    }
}