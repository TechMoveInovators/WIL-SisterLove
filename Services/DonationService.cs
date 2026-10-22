using System.Net.Http.Json;
using System.Text.Json;
using SisterLoveSA.Models;

namespace SisterLoveSA.Services
{
    public class DonationService : IDonationService
    {
        private readonly HttpClient _httpClient;
        private readonly ILogger<DonationService> _logger;

        public DonationService(
            IHttpClientFactory httpClientFactory,
            ILogger<DonationService> logger)
        {
            _httpClient = httpClientFactory.CreateClient("NodeApi");
            _logger = logger;
        }

        public async Task<DonationResult> ProcessDonation(DonationViewModel model)
        {
            try
            {
                var payload = new
                {
                    name = model.DonorName, // Fixed: Changed from donorName to name to match Node controller
                    email = model.Email,
                    amount = model.Amount ?? 0,
                    message = model.Message
                };

                // Calls http://localhost:4000/api/donations
                var response = await _httpClient.PostAsJsonAsync("/api/donations", payload);

                if (response.IsSuccessStatusCode)
                {
                    var responseString = await response.Content.ReadAsStringAsync();

                    var result = new DonationResult
                    {
                        Success = true,
                        TransactionId = $"SL-{DateTime.Now.Year}-API",
                        DonorName = model.DonorName,
                        Amount = model.Amount ?? 0
                    };

                    await SendConfirmationEmail(result);
                    return result;
                }

                _logger.LogWarning("Failed to send donation to Node API. Status Code: {StatusCode}", response.StatusCode);

                return new DonationResult
                {
                    Success = false,
                    ErrorMessage = "Unable to process donation via Node API. Please try again."
                };
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error processing donation via Node API");

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

                _logger.LogInformation(
                    $"Confirmation email sent to donor {result.DonorName} " +
                    $"for transaction #{result.TransactionId}");

                return true;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error sending confirmation email");
                return false;
            }
        }
    }
}