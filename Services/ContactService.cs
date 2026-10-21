using Microsoft.EntityFrameworkCore;
using SisterLoveSA.Models;
using Sisterlove.Data;
using Sisterlove.Models.Entities;

namespace SisterLoveSA.Services
{
    public class ContactService : IContactService
    {
        private readonly ApplicationDbContext _context;
        private readonly ILogger<ContactService> _logger;

        public ContactService(
            ApplicationDbContext context,
            ILogger<ContactService> logger)
        {
            _context = context;
            _logger = logger;
        }

        public async Task<bool> SubmitEnquiryAsync(
            ContactViewModel model)
        {
            try
            {
                var enquiry = new ContactEnquiry
                {
                    FullName = model.Name,
                    Email = model.Email,
                    PhoneNumber = model.Phone,
                    Subject = model.Subject.ToString(),
                    Message = model.Message,
                    SubmittedAt = DateTime.UtcNow
                };

                _context.ContactEnquiries.Add(enquiry);

                await _context.SaveChangesAsync();

                _logger.LogInformation(
                    "Contact enquiry saved successfully. " +
                    "Enquiry ID: {EnquiryId}, Email: {Email}, Subject: {Subject}",
                    enquiry.Id,
                    enquiry.Email,
                    enquiry.Subject);

                return true;
            }
            catch (Exception ex)
            {
                _logger.LogError(
                    ex,
                    "Error saving contact enquiry from {Email}",
                    model.Email);

                return false;
            }
        }
    }
}