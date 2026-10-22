using System.Net.Http.Json;
using SisterLoveSA.Models;

namespace SisterLoveSA.Services
{
    public class ContactService : IContactService
    {
        private readonly HttpClient _httpClient;
        private readonly ILogger<ContactService> _logger;

        public ContactService(
            IHttpClientFactory httpClientFactory,
            ILogger<ContactService> logger)
        {
            _httpClient = httpClientFactory.CreateClient("NodeApi");
            _logger = logger;
        }

        public async Task<bool> SubmitEnquiryAsync(ContactViewModel model)
        {
            try
            {
                var payload = new
                {
                    name = model.Name,
                    email = model.Email,
                    phone = model.Phone,
                    subject = model.Subject.ToString(),
                    message = model.Message
                };

                // Updated to include the full path correctly
                var response = await _httpClient.PostAsJsonAsync("/api/contacts", payload);

                if (response.IsSuccessStatusCode)
                {
                    _logger.LogInformation("Contact enquiry successfully sent to Node API for {Email}", model.Email);
                    return true;
                }

                _logger.LogWarning("Failed to send contact enquiry to Node API. Status Code: {StatusCode}", response.StatusCode);
                return false;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error communicating with Node API for contact email {Email}", model.Email);
                return false;
            }
        }
    }
}