using SisterLoveSA.Models;

namespace SisterLoveSA.Services
{
    public interface IContactService
    {
        Task<bool> SubmitEnquiryAsync(ContactViewModel model);
    }
}