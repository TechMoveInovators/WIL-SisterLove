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
                await Task.Delay(1000);
                var transactionId = GenerateTransactionId();

                var result = new DonationResult
                {
                    Success = true,
                    TransactionId = transactionId,
                    DonorName = model.DonorName,
                    Amount = model.Amount ?? 0
                };

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
            var random = new Random();
            var number = random.Next(1000, 9999);
            return $"SL-{DateTime.Now.Year}-{number}";
        }
    }
}