using Microsoft.AspNetCore.Mvc;

namespace SisterLoveSA.Controllers
{
    public class LegalController : Controller
    {
        public IActionResult Terms()
        {
            return View();
        }

        public IActionResult Privacy()
        {
            return View();
        }

        public IActionResult Accessibility()
        {
            return View();
        }
    }
}