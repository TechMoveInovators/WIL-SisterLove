using Microsoft.AspNetCore.Mvc;
using SisterLoveSA.Models;

namespace SisterLoveSA.Controllers
{
    public class ContactController : Controller
    {
        private readonly ILogger<ContactController> _logger;

        public ContactController(ILogger<ContactController> logger)
        {
            _logger = logger;
        }

        [HttpGet]
        public IActionResult Index()
        {
            var model = new ContactViewModel();
            return View(model);
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Index(ContactViewModel model)
        {
            if (!ModelState.IsValid)
            {
                return View(model);
            }

            try
            {
                // Simulate sending inquiry
                _logger.LogInformation($"Inquiry received from {model.Name} ({model.Email}) - Subject: {model.Subject}");

                // In production, you would send email or save to database
                // await _contactService.SendInquiry(model);

                TempData["SuccessMessage"] = "Thank you for contacting us. We will respond within 24 hours.";
                return RedirectToAction("Index");
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error processing contact form");
                ModelState.AddModelError("", "An error occurred. Please try again.");
                return View(model);
            }
        }
    }
}