using SisterLoveSA.Models;

namespace SisterLoveSA.Services
{
    public interface IDonationService
    {
        Task<DonationResult> ProcessDonation(DonationViewModel model);
        Task<bool> SendConfirmationEmail(DonationResult result);
    }
}