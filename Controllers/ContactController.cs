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
        public IActionResult Index() => View(new ContactViewModel());

        [HttpPost]
        [ValidateAntiForgeryToken]
        public IActionResult Index(ContactViewModel model)
        {
            if (!ModelState.IsValid)
                return View(model);

            _logger.LogInformation($"Inquiry from {model.Name} ({model.Email}) - {model.Subject}");
            TempData["SuccessMessage"] = "Thank you for contacting us. We'll respond within 24 hours.";
            return RedirectToAction("Index");
        }
    }
}