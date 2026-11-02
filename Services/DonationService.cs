using System.Net.Http.Json;
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

        public async Task<DonationResult> ProcessDonation(
            DonationViewModel model)
        {
            try
            {
                var payload = new
                {
                    name = model.DonorName,
                    email = model.Email,
                    amount = model.Amount ?? 0,
                    message = model.Message
                };

                var response = await _httpClient.PostAsJsonAsync(
                    "/api/donations",
                    payload);

                if (!response.IsSuccessStatusCode)
                {
                    _logger.LogWarning(
                        "Donation submission failed. Status Code: {StatusCode}",
                        response.StatusCode);

                    return new DonationResult
                    {
                        Success = false,
                        ErrorMessage =
                            "Unable to submit your donation details. Please try again."
                    };
                }

                _logger.LogInformation(
                    "Donation record saved for {Email}. " +
                    "No payment was processed by this service.",
                    model.Email);

                return new DonationResult
                {
                    Success = true,
                    TransactionId = string.Empty,
                    DonorName = model.DonorName,
                    Amount = model.Amount ?? 0
                };
            }
            catch (Exception ex)
            {
                _logger.LogError(
                    ex,
                    "Error submitting donation to Node API");

                return new DonationResult
                {
                    Success = false,
                    ErrorMessage =
                        "Unable to submit your donation details. Please try again."
                };
            }
        }

        public Task<bool> SendConfirmationEmail(DonationResult result)
        {
            _logger.LogWarning(
                "Confirmation email was not sent because email delivery " +
                "has not been configured.");

            return Task.FromResult(false);
        }
    }
}