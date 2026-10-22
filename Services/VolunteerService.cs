using Sisterlove.Services;
using SisterLoveSA.Models;
using System.Net.Http.Json;

namespace SisterLoveSA.Services
{
    public class VolunteerService : IVolunteerService
    {
        private readonly HttpClient _httpClient;
        private readonly ILogger<VolunteerService> _logger;

        public VolunteerService(
            IHttpClientFactory httpClientFactory,
            ILogger<VolunteerService> logger)
        {
            _httpClient = httpClientFactory.CreateClient("NodeApi");
            _logger = logger;
        }

        public async Task<bool> SubmitVolunteerApplicationAsync(VolunteerViewModel model)
        {
            try
            {
                var payload = new
                {
                    name = model.Name,
                    email = model.Email,
                    phone = model.Phone,
                    areaOfInterest = model.AreaOfInterest,
                    motivation = model.Motivation
                };

                // Calls http://localhost:4000/api/volunteers
                var response = await _httpClient.PostAsJsonAsync("/api/volunteers", payload);

                if (response.IsSuccessStatusCode)
                {
                    _logger.LogInformation("Volunteer application successfully sent to Node API for {Email}", model.Email);
                    return true;
                }

                _logger.LogWarning("Failed to send volunteer application to Node API. Status Code: {StatusCode}", response.StatusCode);
                return false;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error communicating with Node API for volunteer email {Email}", model.Email);
                return false;
            }
        }
    }
}