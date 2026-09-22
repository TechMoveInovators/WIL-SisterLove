using Microsoft.AspNetCore.Mvc;
using SisterLoveSA.Models;
using SisterLoveSA.Services;

namespace SisterLoveSA.Controllers
{
    public class DonateController : Controller
    {
        private readonly IDonationService _donationService;
        private readonly ILogger<DonateController> _logger;

        public DonateController(IDonationService donationService, ILogger<DonateController> logger)
        {
            _donationService = donationService;
            _logger = logger;
        }

        [HttpGet]
        public IActionResult Index() => View(new DonationViewModel());

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Index(DonationViewModel model)
        {
            if (!ModelState.IsValid)
                return View(model);

            try
            {
                var result = await _donationService.ProcessDonation(model);

                if (result.Success)
                {
                    TempData["DonorName"] = result.DonorName;
                    TempData["Amount"] = result.Amount.ToString("N2");
                    TempData["TransactionId"] = result.TransactionId;
                    return RedirectToAction("ThankYou");
                }

                ModelState.AddModelError("", result.ErrorMessage ?? "Donation failed.");
                return View(model);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error processing donation");
                ModelState.AddModelError("", "An error occurred. Please try again.");
                return View(model);
            }
        }

        public IActionResult ThankYou()
        {
            ViewBag.DonorName = TempData["DonorName"]?.ToString() ?? "Donor";
            ViewBag.Amount = TempData["Amount"]?.ToString() ?? "0";
            ViewBag.TransactionId = TempData["TransactionId"]?.ToString() ?? "1000";
            return View();
        }
    }
}