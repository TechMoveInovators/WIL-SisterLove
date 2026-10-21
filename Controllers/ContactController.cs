using Microsoft.AspNetCore.Mvc;
using SisterLoveSA.Models;
using SisterLoveSA.Services;

namespace SisterLoveSA.Controllers
{
    public class ContactController : Controller
    {
        private readonly IContactService _contactService;
        private readonly ILogger<ContactController> _logger;

        public ContactController(
            IContactService contactService,
            ILogger<ContactController> logger)
        {
            _contactService = contactService;
            _logger = logger;
        }

        [HttpGet]
        public IActionResult Index()
        {
            return View(new ContactViewModel());
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Index(
            ContactViewModel model)
        {
            if (!ModelState.IsValid)
            {
                return View(model);
            }

            var success =
                await _contactService.SubmitEnquiryAsync(model);

            if (!success)
            {
                ModelState.AddModelError(
                    "",
                    "We could not process your enquiry. Please try again.");

                return View(model);
            }

            TempData["SuccessMessage"] =
                "Thank you for contacting SisterLove South Africa. " +
                "Your enquiry has been received and a member of our team will respond.";

            return RedirectToAction(nameof(Index));
        }
    }
}