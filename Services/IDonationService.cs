using Sisterlove.Models;

namespace Sisterlove.Services
    {
        public interface IDonationService
        {
            Task<DonationResult> ProcessDonation(DonationViewModel model);
            Task<bool> SendConfirmationEmail(DonationResult result);
        }
    }
