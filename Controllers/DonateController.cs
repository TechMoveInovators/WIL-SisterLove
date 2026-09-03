using Microsoft.AspNetCore.Mvc;
using Sisterlove.Models;
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
        public IActionResult Index()
        {
            var model = new DonationViewModel();
            return View(model);
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Index(DonationViewModel model)
        {
            if (!ModelState.IsValid)
            {
                return View(model);
            }

            try
            {
                var result = await _donationService.ProcessDonation(model);

                if (result.Success)
                {
                    TempData["DonorName"] = result.DonorName;
                    TempData["Amount"] = result.Amount;
                    TempData["TransactionId"] = result.TransactionId;

                    return RedirectToAction("ThankYou");
                }
                else
                {
                    ModelState.AddModelError("", result.ErrorMessage);
                    return View(model);
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error processing donation");
                ModelState.AddModelError("", "An error occurred while processing your donation. Please try again.");
                return View(model);
            }
        }

        public IActionResult ThankYou()
        {
            var donorName = TempData["DonorName"]?.ToString() ?? "Donor";
            var amount = TempData["Amount"]?.ToString() ?? "0";
            var transactionId = TempData["TransactionId"]?.ToString() ?? "1000";

            ViewBag.DonorName = donorName;
            ViewBag.Amount = amount;
            ViewBag.TransactionId = transactionId;

            return View();
        }
    }
}