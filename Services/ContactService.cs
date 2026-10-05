using Microsoft.Extensions.Logging;
using SisterLoveSA.Models;

namespace SisterLoveSA.Services
{
    public class ContactService : IContactService
    {
        private readonly ILogger<ContactService> _logger;

        public ContactService(
            ILogger<ContactService> logger)
        {
            _logger = logger;
        }

        public Task<bool> SubmitEnquiryAsync(
            ContactViewModel model)
        {
            try
            {
                _logger.LogInformation(
                    "Contact enquiry received. Name: {Name}, Email: {Email}, Subject: {Subject}, Volunteer: {IsVolunteer}",
                    model.Name,
                    model.Email,
                    model.Subject,
                    model.IsVolunteer);

                return Task.FromResult(true);
            }
            catch (Exception ex)
            {
                _logger.LogError(
                    ex,
                    "Error processing contact enquiry from {Email}",
                    model.Email);

                return Task.FromResult(false);
            }
        }
    }
}